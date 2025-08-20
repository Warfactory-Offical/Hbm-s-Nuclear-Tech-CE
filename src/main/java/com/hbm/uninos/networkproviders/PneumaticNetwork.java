package com.hbm.uninos.networkproviders;

import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class PneumaticNetwork extends NodeNet<IInventory, TileEntityPneumoTube, TileEntityPneumoTube.PneumaticNode, PneumaticNetwork> {

    public static final byte SEND_FIRST = 0;
    public static final byte SEND_LAST = 1;
    public static final byte SEND_RANDOM = 2;
    public static final byte RECEIVE_ROBIN = 0;
    public static final byte RECEIVE_RANDOM = 1;

    public static final INetworkProvider<PneumaticNetwork> THE_PNEUMATIC_PROVIDER = PneumaticNetwork::new;

    public Random rand = new Random();
    public int nextReceiver = 0;

    protected static final int timeout = 1_000;
    public static final int ITEMS_PER_TRANSFER = 64;

    // while the system has parts that expects IInventires to be TileEntities to work properly (mostly range checks),
    // it can actually handle non-TileEntities just fine.
    public HashMap<IInventory, Pair<ForgeDirection, Long>> receivers = new HashMap<>();

    public void addReceiver(IInventory inventory, ForgeDirection pipeDir) {
        receivers.put(inventory, new Pair<>(pipeDir, System.currentTimeMillis()));
    }

    @Override public void update() {

        // weeds out invalid targets
        // technically not necessary since that step is taken during the send operation,
        // but we still want to reap garbage data that would otherwise accumulate
        long timestamp = System.currentTimeMillis();
        receivers.entrySet().removeIf(x -> (timestamp - x.getValue().getValue() > timeout) || NodeNet.isBadLink(x.getKey()));
    }

    public boolean send(IInventory source, TileEntityPneumoTube tube, ForgeDirection accessDir, int sendOrder, int receiveOrder, int maxRange) {

        // turns out there may be a short time window where the cleanup hasn't happened yet, but chunkloading has already caused tiles to go invalid
        // so we just run it again here, just to be sure.
        long timestamp = System.currentTimeMillis();
        receivers.entrySet().removeIf(x -> (timestamp - x.getValue().getValue() > timeout) || NodeNet.isBadLink(x.getKey()));

        if(receivers.isEmpty()) return false;

        int[] sourceSlotAccess = getSlotAccess(source, accessDir.toEnumFacing());

        if(sendOrder == SEND_LAST) BobMathUtil.reverseIntArray(sourceSlotAccess);
        if(sendOrder == SEND_RANDOM) BobMathUtil.shuffleIntArray(sourceSlotAccess);

        // for round robin, receivers are ordered by proximity to the source
        ReceiverComparator comparator = new ReceiverComparator(tube);
        List<Map.Entry<IInventory, Pair<ForgeDirection, Long>>> receiverList = new ArrayList<>(receivers.size());
        receiverList.addAll(receivers.entrySet());
        receiverList.sort(comparator);

        Map.Entry<IInventory, Pair<ForgeDirection, Long>> chosenReceiverEntry = null;

        if(receiveOrder == RECEIVE_ROBIN) {
            int index = nextReceiver % receivers.size();
            chosenReceiverEntry = receiverList.get(index);
        } else if(receiveOrder == RECEIVE_RANDOM) {
            chosenReceiverEntry = receiverList.get(rand.nextInt(receiverList.size()));
        }

        if(chosenReceiverEntry == null) return false;

        IInventory dest = chosenReceiverEntry.getKey();
        ISidedInventory sidedDest = dest instanceof ISidedInventory ? (ISidedInventory) dest : null;
        ISidedInventory sidedSource = source instanceof ISidedInventory ? (ISidedInventory) source : null;

        TileEntity tile1 = source instanceof TileEntity ? (TileEntity) source : null;
        TileEntity tile2 = dest instanceof TileEntity ? (TileEntity) dest : null;

        // range check for our compression level, skip if either source or dest aren't tile entities
        if(tile1 != null && tile2 != null) {
            int sq = (tile1.getPos().getX() - tile2.getPos().getX()) * (tile1.getPos().getX() - tile2.getPos().getX()) + (tile1.getPos().getY() - tile2.getPos().getY()) * (tile1.getPos().getY() - tile2.getPos().getY()) + (tile1.getPos().getZ() - tile2.getPos().getZ()) * (tile1.getPos().getZ() - tile2.getPos().getZ());
            if(sq > maxRange * maxRange) {
                return false;
            }
        }

        EnumFacing destSide = chosenReceiverEntry.getValue().getKey().getOpposite().toEnumFacing();
        int[] destSlotAccess = getSlotAccess(dest, destSide);
        int itemsLeftToSend = ITEMS_PER_TRANSFER; // not actually individual items, but rather the total "mass", based on max stack size
        int itemHardCap = dest instanceof TileEntityMachineAutocrafter ? 1 : ITEMS_PER_TRANSFER;
        boolean didSomething = false;

        for(int sourceIndex : sourceSlotAccess) {
            ItemStack sourceStack = source.getStackInSlot(sourceIndex);
            if(sourceStack == ItemStack.EMPTY) continue;
            if(sidedSource != null && !sidedSource.canExtractItem(sourceIndex, sourceStack, Objects.requireNonNull(accessDir.toEnumFacing()))) continue;
            boolean match = tube.matchesFilter(sourceStack);
            if((match && !tube.whitelist) || (!match && tube.whitelist)) continue;
            // the "mass" of an item. something that only stacks to 4 has a "mass" of 16. max transfer mass is 64, i.e. one standard stack, or one single unstackable item
            int proportionalValue = MathHelper.clamp(64 / sourceStack.getMaxStackSize(), 1, 64);

            // try to fill partial stacks first
            for(int destIndex : destSlotAccess) {
                ItemStack destStack = dest.getStackInSlot(destIndex);
                if(destStack == ItemStack.EMPTY) continue;
                if(!ItemStackUtil.areStacksCompatible(sourceStack, destStack)) continue;
                int toMove = BobMathUtil.min(sourceStack.getCount(), destStack.getMaxStackSize() - destStack.getCount(), dest.getInventoryStackLimit() - destStack.getCount(), itemsLeftToSend / proportionalValue, itemHardCap);
                if(toMove <= 0) continue;

                ItemStack checkStack = destStack.copy();
                checkStack.grow(toMove);
                if(!dest.isItemValidForSlot(destIndex, checkStack)) continue;
                if(sidedDest != null && !sidedDest.canInsertItem(destIndex, checkStack, Objects.requireNonNull(destSide))) continue;

                sourceStack.shrink(toMove);
                if(sourceStack.getCount() <= 0) source.setInventorySlotContents(sourceIndex, ItemStack.EMPTY);
                destStack.grow(toMove);
                itemsLeftToSend -= toMove * proportionalValue;
                didSomething = true;
                if(itemsLeftToSend <= 0) break;
            }

            // if there's stuff left to send, occupy empty slots
            if(itemsLeftToSend > 0 && sourceStack.getCount() > 0) for(int destIndex : destSlotAccess) {
                if(dest.getStackInSlot(destIndex) != ItemStack.EMPTY) continue;
                int toMove = BobMathUtil.min(sourceStack.getCount(), dest.getInventoryStackLimit(), itemsLeftToSend / proportionalValue, itemHardCap);
                if(toMove <= 0) continue;

                ItemStack checkStack = sourceStack.copy();
                checkStack.setCount(toMove);
                if(!dest.isItemValidForSlot(destIndex, checkStack)) continue;
                if(sidedDest != null && !sidedDest.canInsertItem(destIndex, checkStack, Objects.requireNonNull(destSide))) continue;

                ItemStack newStack = sourceStack.copy();
                newStack.setCount(toMove);
                sourceStack.shrink(toMove);
                if(sourceStack.getCount() <= 0) source.setInventorySlotContents(sourceIndex, ItemStack.EMPTY);
                dest.setInventorySlotContents(destIndex, newStack);
                itemsLeftToSend -= toMove * proportionalValue;
                didSomething = true;
                if(itemsLeftToSend <= 0) break;
            }

            if(itemsLeftToSend <= 0) break;
        }

        // make sure both parties are saved to disk and increment the counter for round robin
        if(didSomething) {
            source.markDirty();
            dest.markDirty();

            if(receiveOrder == RECEIVE_ROBIN) {
                nextReceiver++;
            }
        }

        return didSomething;
    }

    /** Returns an array of accessible slots from the given side of an IInventory. If it's an ISidedInventory, uses the sided restrictions instead. */
    public static int[] getSlotAccess(IInventory inventory, EnumFacing dir) {

        if(inventory instanceof ISidedInventory) {
            int[] slotAccess = ((ISidedInventory) inventory).getSlotsForFace(dir);
            return Arrays.copyOf(slotAccess, slotAccess.length); //we mess with the order, so better not use the original array
        } else {
            int[] slotAccess = new int[inventory.getSizeInventory()];
            for(int i = 0; i < inventory.getSizeInventory(); i++) slotAccess[i] = i;
            return slotAccess;
        }
    }

    /** Compares IInventory by distance, going off the assumption that they are TileEntities. Uses positional data for tie-breaking if the distance is the same. */
    public static class ReceiverComparator implements Comparator<Map.Entry<IInventory, Pair<ForgeDirection, Long>>> {

        private final TileEntityPneumoTube origin;

        public ReceiverComparator(TileEntityPneumoTube origin) {
            this.origin = origin;
        }

        @Override
        public int compare(Map.Entry<IInventory, Pair<ForgeDirection, Long>> o1, Map.Entry<IInventory, Pair<ForgeDirection, Long>> o2) {

            TileEntity tile1 = o1.getKey() instanceof TileEntity ? (TileEntity) o1.getKey() : null;
            TileEntity tile2 = o2.getKey() instanceof TileEntity ? (TileEntity) o2.getKey() : null;

            // prioritize actual TileEntities
            if(tile1 == null && tile2 != null) return 1;
            if(tile1 != null && tile2 == null) return -1;
            if(tile1 == null) return 0;

            // calculate distances from origin
            int dist1 = (tile1.getPos().getX() - origin.getPos().getX()) * (tile1.getPos().getX() - origin.getPos().getX()) + (tile1.getPos().getY() - origin.getPos().getY()) * (tile1.getPos().getY() - origin.getPos().getY()) + (tile1.getPos().getZ() - origin.getPos().getZ()) * (tile1.getPos().getZ() - origin.getPos().getZ());
            int dist2 = (tile2.getPos().getX() - origin.getPos().getX()) * (tile2.getPos().getX() - origin.getPos().getX()) + (tile2.getPos().getY() - origin.getPos().getY()) * (tile2.getPos().getY() - origin.getPos().getY()) + (tile2.getPos().getZ() - origin.getPos().getZ()) * (tile2.getPos().getZ() - origin.getPos().getZ());

            // tier-breaker: use hash value instead
            if(dist1 == dist2) {
                return TileEntityPneumoTube.getIdentifier(tile1.getPos().getX(), tile1.getPos().getY(), tile1.getPos().getZ()) - TileEntityPneumoTube.getIdentifier(tile2.getPos().getX(), tile2.getPos().getY(), tile2.getPos().getZ());
            }

            // no tie? return difference of the distances
            return dist1 - dist2;
        }
    }
}
