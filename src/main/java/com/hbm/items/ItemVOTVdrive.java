package com.hbm.items;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.interfaces.Untested;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Untested
//@Slize What the fuck is this shit?
public class ItemVOTVdrive extends ItemEnumMulti {

    public ItemVOTVdrive(String registryName) {
        super(registryName, SolarSystem.Body.class, false, true);
        this.setMaxStackSize(1);
        this.canRepair = false;
    }

    public static Destination getDestination(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        SolarSystem.Body body = SolarSystem.Body.values()[stack.getItemDamage()];
        int x = stack.getTagCompound().getInteger("x");
        int z = stack.getTagCompound().getInteger("z");
        return new Destination(body, x, z);
    }

    public static Target getTarget(ItemStack stack, World world) {
        if (stack == null) {
            return new Target(null, false, false);
        }

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        Destination destination = getDestination(stack);

        if (destination.body == SolarSystem.Body.ORBIT) {
            if (world.isRemote) {
                CelestialBody body = CelestialBody.getBody(stack.getTagCompound().getInteger("sDim"));
                boolean hasStation = stack.getTagCompound().getBoolean("sHas");

                return new Target(body, true, hasStation);
            }

            OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);
            if (!station.hasStation) station.orbiting = CelestialBody.getBody(world);

            // The client can't get this information, so any time the server grabs it, serialize it to the itemstack
            stack.getTagCompound().setString("stationName", station.name);
            stack.getTagCompound().setInteger("sDim", station.orbiting.dimensionId);
            stack.getTagCompound().setBoolean("sHas", station.hasStation);

            return new Target(station.orbiting, true, station.hasStation);
        } else {
            return new Target(destination.body.getBody(), false, true);
        }
    }

    public static void setCoordinates(ItemStack stack, int x, int z) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("x", x);
        stack.getTagCompound().setInteger("z", z);
    }

    public static int getProcessingTier(ItemStack stack) {
        SolarSystem.Body body = SolarSystem.Body.values()[stack.getItemDamage()];
        return body.getProcessingLevel();
    }

    public static boolean getProcessed(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        return stack.getTagCompound().getBoolean("Processed");
    }

    public static void setProcessed(ItemStack stack, boolean processed) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setBoolean("Processed", processed);
    }

    // Returns an area for the Stardar to draw, so the player can pick a safe spot to land
    public static Destination getApproximateDestination(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        SolarSystem.Body body = SolarSystem.Body.values()[stack.getItemDamage()];
        if (!stack.getTagCompound().hasKey("ax") || !stack.getTagCompound().hasKey("az")) {
            stack.getTagCompound().setInteger("ax", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
            stack.getTagCompound().setInteger("az", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
        }
        int ax = stack.getTagCompound().getInteger("ax");
        int az = stack.getTagCompound().getInteger("az");
        return new Destination(body, ax, az);
    }

    public static void markCopied(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setBoolean("copied", true);
    }

    public static boolean wasCopied(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        return stack.getTagCompound().getBoolean("copied");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        super.addInformation(stack, world, list, flagIn);

        Destination destination = getDestination(stack);

        if (destination.body == SolarSystem.Body.ORBIT) {
            String identifier = stack.getTagCompound().getString("stationName");

            if (identifier.equals(""))
                identifier = "0x" + Integer.toHexString(new ChunkPos(destination.x, destination.z).hashCode()).toUpperCase();

            list.add("Destination: ORBITAL STATION");
            list.add("Station: " + identifier);
            return;
        } else {/*

            int processingLevel = destination.body.getProcessingLevel();

            list.add("Destination: " + ChatFormatting.AQUA + I18nUtil.resolveKey("body." + destination.body.name));

            if (destination.x == 0 && destination.z == 0) {
                list.add(ChatFormatting.GOLD + "Needs destination coordinates!");
            } else if (!getProcessed(stack)) {
                // Display processing level info if not processed
                list.add("Process requirement: Level " + processingLevel);
                list.add(ChatFormatting.GOLD + "Needs processing!");
                list.add("Target coordinates: " + destination.x + ", " + destination.z);
            } else {
                // Display destination info if processed
                list.add(ChatFormatting.GREEN + "Processed!");
                list.add("Target coordinates: " + destination.x + ", " + destination.z);
            }*/
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setInteger("x", 1);
            stackTag.setInteger("ax", 1);
            stackTag.setBoolean("Processed", true);
            for (int i = 0; i < theEnum.getEnumConstants().length; i++) {
                ItemStack stack = new ItemStack(this, 1, i);
                stack.setTagCompound(stackTag);
                items.add(stack);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean isProcessed = getProcessed(stack);
        boolean onDestination = world.provider.getDimension() == getDestination(stack).body.getDimensionId();

        // If we're on the body (or in creative), immediately process
        if (!isProcessed && (player.capabilities.isCreativeMode || onDestination)) {
            isProcessed = true;
            setProcessed(stack, true);
        }

        ItemStack newStack = stack;

        if (isProcessed && player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityRideableRocket) {
            EntityRideableRocket rocket = (EntityRideableRocket) player.getRidingEntity();

            if (rocket.getRocket().stages.size() > 0 || world.provider.getDimension() == SpaceConfig.orbitDimension || rocket.isReusable()) {
                if (rocket.getState() == EntityRideableRocket.RocketState.LANDED || rocket.getState() == EntityRideableRocket.RocketState.AWAITING) {
                    // Replace our held stack with the rocket drive and place our held drive into the rocket
                    if (rocket.navDrive != null) {
                        newStack = rocket.navDrive;
                    } else {
                        newStack.setCount(0);
                    }

                    rocket.navDrive = stack.copy();
                    rocket.navDrive.setCount(1);

                    if (!world.isRemote) {
                        rocket.setState(EntityRideableRocket.RocketState.AWAITING);
                    }

                    world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!player.inventory.addItemStackToInventory(newStack)) {
                        player.dropItem(newStack, true, false);
                    }
                }
            }
        }

        return super.onItemRightClick(world, player, hand);
    }


    @SuppressWarnings("deprecation")
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        Destination destination = getDestination(stack);
        if (destination.body == SolarSystem.Body.ORBIT)
            return EnumActionResult.FAIL;

        boolean onDestination = world.provider.getDimension() == destination.body.getDimensionId();
        if (!onDestination)
            return EnumActionResult.FAIL;

        setCoordinates(stack, (int) hitX, (int) hitZ);
        setProcessed(stack, true);

        if (!world.isRemote)
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Set landing coordinates to: " + (int) hitX + ", " + (int) hitZ));

        return EnumActionResult.SUCCESS;
    }

    public static class Destination {

        public int x;
        public int z;
        public SolarSystem.Body body;

        public Destination(SolarSystem.Body body, int x, int z) {
            this.body = body;
            this.x = x;
            this.z = z;
        }

        public ChunkPos getChunk() {
            return new ChunkPos(x >> 4, z >> 4);
        }

    }

    public static class Target {

        public CelestialBody body;
        public boolean inOrbit;
        public boolean isValid;

        public Target(CelestialBody body, boolean inOrbit, boolean isValid) {
            this.body = body;
            this.inOrbit = inOrbit;
            this.isValid = isValid;
        }

    }

}
