package com.hbm.uninos.networkproviders;

import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.ItemStackUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

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

    public Map<ReceiverTarget, Long> receivers = new HashMap<>();

    public void addReceiver(BlockPos pos, ForgeDirection pipeDir) {
        receivers.put(new ReceiverTarget(pos, pipeDir), System.currentTimeMillis());
    }

    @Override
    public void update() {
        long timestamp = System.currentTimeMillis();
        receivers.entrySet().removeIf(entry -> timestamp - entry.getValue() > timeout);
    }

    public boolean send(TileEntity sourceTile, IItemHandler sourceHandler, TileEntityPneumoTube tube, ForgeDirection accessDir, int sendOrder, int receiveOrder, int maxRange) {
        if(sourceTile == null || sourceHandler == null) return false;
        if(receivers.isEmpty()) return false;
        World world = tube.getWorld();
        if(world == null) return false;

        long timestamp = System.currentTimeMillis();
        receivers.entrySet().removeIf(entry -> timestamp - entry.getValue() > timeout);

        List<ReceiverCandidate> candidates = collectCandidates(world);
        if(candidates.isEmpty()) return false;

        candidates.sort(new ReceiverComparator(tube));
        ReceiverCandidate chosen = selectCandidate(candidates, receiveOrder);
        if(chosen == null) return false;

        TileEntity destTile = chosen.tile;
        if(destTile != null) {
            int dx = sourceTile.getPos().getX() - destTile.getPos().getX();
            int dy = sourceTile.getPos().getY() - destTile.getPos().getY();
            int dz = sourceTile.getPos().getZ() - destTile.getPos().getZ();
            int sq = dx * dx + dy * dy + dz * dz;
            if(sq > maxRange * maxRange) {
                return false;
            }
        }

        IItemHandler destHandler = chosen.handler;
        int[] sourceSlotOrder = buildSlotOrder(sourceHandler);
        if(sendOrder == SEND_LAST) BobMathUtil.reverseIntArray(sourceSlotOrder);
        if(sendOrder == SEND_RANDOM) BobMathUtil.shuffleIntArray(sourceSlotOrder);

        int itemsLeftToSend = ITEMS_PER_TRANSFER;
        int itemHardCap = chosen.autocrafter ? 1 : ITEMS_PER_TRANSFER;
        boolean didSomething = false;

        for(int sourceSlot : sourceSlotOrder) {
            ItemStack sourceStack = sourceHandler.getStackInSlot(sourceSlot);
            if(sourceStack.isEmpty()) continue;

            boolean match = tube.matchesFilter(sourceStack);
            if((match && !tube.whitelist) || (!match && tube.whitelist)) continue;

            int proportionalValue = MathHelper.clamp(64 / sourceStack.getMaxStackSize(), 1, 64);
            if(itemsLeftToSend < proportionalValue) break;

            // fill existing stacks first
            for(int destSlot = 0; destSlot < destHandler.getSlots(); destSlot++) {
                if(itemsLeftToSend < proportionalValue) break;
                ItemStack destStack = destHandler.getStackInSlot(destSlot);
                if(destStack.isEmpty()) continue;
                if(!ItemStackUtil.areStacksCompatible(sourceStack, destStack)) continue;

                int capacity = Math.min(destHandler.getSlotLimit(destSlot), destStack.getMaxStackSize());
                int space = capacity - destStack.getCount();
                if(space <= 0) continue;

                int maxByMass = Math.min(itemsLeftToSend / proportionalValue, itemHardCap);
                if(maxByMass <= 0) break;

                ItemStack currentSource = sourceHandler.getStackInSlot(sourceSlot);
                if(currentSource.isEmpty()) break;
                int attempt = Math.min(Math.min(space, currentSource.getCount()), maxByMass);
                if(attempt <= 0) continue;

                int moved = transferItems(sourceHandler, sourceSlot, destHandler, destSlot, attempt);
                if(moved > 0) {
                    itemsLeftToSend -= moved * proportionalValue;
                    didSomething = true;
                    if(itemsLeftToSend < proportionalValue) break;
                    sourceStack = sourceHandler.getStackInSlot(sourceSlot);
                    if(sourceStack.isEmpty()) break;
                }
            }

            if(itemsLeftToSend < proportionalValue) break;
            ItemStack updatedSource = sourceHandler.getStackInSlot(sourceSlot);
            if(updatedSource.isEmpty()) continue;

            for(int destSlot = 0; destSlot < destHandler.getSlots(); destSlot++) {
                if(itemsLeftToSend < proportionalValue) break;
                ItemStack destStack = destHandler.getStackInSlot(destSlot);
                if(!destStack.isEmpty()) continue;

                ItemStack currentSource = sourceHandler.getStackInSlot(sourceSlot);
                if(currentSource.isEmpty()) break;

                int slotLimit = destHandler.getSlotLimit(destSlot);
                int maxByMass = Math.min(itemsLeftToSend / proportionalValue, itemHardCap);
                int attempt = Math.min(Math.min(slotLimit, currentSource.getMaxStackSize()), Math.min(currentSource.getCount(), maxByMass));
                if(attempt <= 0) continue;

                int moved = transferItems(sourceHandler, sourceSlot, destHandler, destSlot, attempt);
                if(moved > 0) {
                    itemsLeftToSend -= moved * proportionalValue;
                    didSomething = true;
                    if(itemsLeftToSend < proportionalValue) break;
                }
            }

            if(itemsLeftToSend < proportionalValue) break;
        }

        if(didSomething) {
            if(receiveOrder == RECEIVE_ROBIN) {
                nextReceiver++;
            }
            sourceTile.markDirty();
            if(destTile != null) destTile.markDirty();
        }

        return didSomething;
    }

    private List<ReceiverCandidate> collectCandidates(World world) {
        List<ReceiverCandidate> list = new ArrayList<>(receivers.size());
        Iterator<Map.Entry<ReceiverTarget, Long>> iterator = receivers.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<ReceiverTarget, Long> entry = iterator.next();
            ReceiverTarget target = entry.getKey();
            TileEntity tile = Compat.getTileStandard(world, target.pos.getX(), target.pos.getY(), target.pos.getZ());
            if(tile == null || tile.isInvalid()) {
                iterator.remove();
                continue;
            }
            IItemHandler handler = resolveItemHandler(tile, target.pipeDir.getOpposite());
            if(handler == null || handler.getSlots() <= 0) {
                iterator.remove();
                continue;
            }
            list.add(new ReceiverCandidate(target, tile, handler));
        }
        return list;
    }

    private ReceiverCandidate selectCandidate(List<ReceiverCandidate> candidates, int receiveOrder) {
        if(candidates.isEmpty()) return null;
        if(receiveOrder == RECEIVE_RANDOM) {
            return candidates.get(rand.nextInt(candidates.size()));
        }
        int index = nextReceiver % candidates.size();
        return candidates.get(index);
    }

    private static int[] buildSlotOrder(IItemHandler handler) {
        int[] order = new int[handler.getSlots()];
        for(int i = 0; i < order.length; i++) order[i] = i;
        return order;
    }

    private static int transferItems(IItemHandler source, int sourceSlot, IItemHandler dest, int destSlot, int maxAmount) {
        if(maxAmount <= 0) return 0;
        ItemStack simulatedExtraction = source.extractItem(sourceSlot, maxAmount, true);
        if(simulatedExtraction.isEmpty()) return 0;

        ItemStack simulatedInsertion = dest.insertItem(destSlot, simulatedExtraction, true);
        int accepted = simulatedExtraction.getCount() - simulatedInsertion.getCount();
        if(accepted <= 0) return 0;

        ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
        if(extracted.isEmpty()) return 0;

        ItemStack leftover = dest.insertItem(destSlot, extracted, false);
        int inserted = extracted.getCount() - leftover.getCount();

        if(!leftover.isEmpty()) {
            ItemStack remainder = ItemHandlerHelper.insertItem(source, leftover, false);
            if(!remainder.isEmpty()) {
                remainder = source.insertItem(sourceSlot, remainder, false);
                if(!remainder.isEmpty()) {
                    inserted -= remainder.getCount();
                    if(inserted < 0) inserted = 0;
                }
            }
        }

        return inserted;
    }

    public static IItemHandler resolveItemHandler(TileEntity tile, ForgeDirection direction) {
        if(tile == null || tile.isInvalid()) return null;
        EnumFacing facing = direction != ForgeDirection.UNKNOWN ? direction.toEnumFacing() : null;
        if(facing != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            if(handler != null && handler.getSlots() > 0) return handler;
        }
        if(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(handler != null && handler.getSlots() > 0) return handler;
        }
        if(tile instanceof ISidedInventory sided && facing != null) {
            IItemHandler handler = new SidedInvWrapper(sided, facing);
            if(handler.getSlots() > 0) return handler;
        }
        if(tile instanceof IInventory inventory) {
            IItemHandler handler = new InvWrapper(inventory);
            if(handler.getSlots() > 0) return handler;
        }
        return null;
    }

    public static boolean hasItemHandler(TileEntity tile, ForgeDirection direction) {
        return resolveItemHandler(tile, direction) != null;
    }

    private static final class ReceiverTarget {
        private final BlockPos pos;
        private final ForgeDirection pipeDir;

        private ReceiverTarget(BlockPos pos, ForgeDirection pipeDir) {
            this.pos = pos.toImmutable();
            this.pipeDir = pipeDir;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof ReceiverTarget other)) return false;
            return Objects.equals(pos, other.pos) && pipeDir == other.pipeDir;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, pipeDir);
        }
    }

    private static final class ReceiverCandidate {
        private final ReceiverTarget target;
        private final TileEntity tile;
        private final IItemHandler handler;
        private final boolean autocrafter;

        private ReceiverCandidate(ReceiverTarget target, TileEntity tile, IItemHandler handler) {
            this.target = target;
            this.tile = tile;
            this.handler = handler;
            this.autocrafter = tile instanceof TileEntityMachineAutocrafter;
        }
    }

    private static final class ReceiverComparator implements Comparator<ReceiverCandidate> {

        private final TileEntityPneumoTube origin;

        private ReceiverComparator(TileEntityPneumoTube origin) {
            this.origin = origin;
        }

        @Override
        public int compare(ReceiverCandidate c1, ReceiverCandidate c2) {
            TileEntity tile1 = c1.tile;
            TileEntity tile2 = c2.tile;

            if(tile1 == null && tile2 != null) return 1;
            if(tile1 != null && tile2 == null) return -1;
            if(tile1 == null) return 0;

            int dist1 = squaredDistance(tile1, origin);
            int dist2 = squaredDistance(tile2, origin);

            if(dist1 == dist2) {
                return TileEntityPneumoTube.getIdentifier(tile1.getPos().getX(), tile1.getPos().getY(), tile1.getPos().getZ()) - TileEntityPneumoTube.getIdentifier(tile2.getPos().getX(), tile2.getPos().getY(), tile2.getPos().getZ());
            }
            return dist1 - dist2;
        }

        private static int squaredDistance(TileEntity tile, TileEntityPneumoTube origin) {
            int dx = tile.getPos().getX() - origin.getPos().getX();
            int dy = tile.getPos().getY() - origin.getPos().getY();
            int dz = tile.getPos().getZ() - origin.getPos().getZ();
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
