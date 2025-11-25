package com.hbm.handler;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import li.cil.oc.api.Items;
import li.cil.oc.api.fs.FileSystem;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static com.hbm.main.CraftingManager.addShapelessAuto;
import static li.cil.oc.api.FileSystem.asReadOnly;
import static li.cil.oc.api.FileSystem.fromClass;
import static net.minecraft.item.EnumDyeColor.*;

/**
 * General handler for OpenComputers compatibility.
 * @author BallOfEnergy (Microwave), implemented in 1.12 by Th3_Sl1ze
 */
public class CompatHandler {

    /**
     * Used for converting a steam type to an integer (compression levels).
     * @param type Steam type.
     * @return Object[] array containing an int with the "compression level"
     */
    public static Object[] steamTypeToInt(FluidType type) {
        return switch (type.getID()) {
            case (4) -> // Fluids.HOTSTEAM
                    new Object[]{1};
            case (5) -> // Fluids.SUPERHOTSTEAM
                    new Object[]{2};
            case (6) -> // Fluids.ULTRAHOTSTEAM
                    new Object[]{3};
            default -> new Object[]{0};
        };
    }

    /**
     * Used for converting a compression level to a steam type.
     * @param arg Steam compression level.
     * @return FluidType of the steam type based on the compression level.
     */
    public static FluidType intToSteamType(int arg) {
        return switch (arg) {
            case (1) -> Fluids.HOTSTEAM;
            case (2) -> Fluids.SUPERHOTSTEAM;
            case (3) -> Fluids.ULTRAHOTSTEAM;
            default -> Fluids.STEAM;
        };
    }

    /**
     * Allows for easy creation of read-only filesystems. Primarily for floppy disks.
     * (Though maybe reading directly from VOTV drives as filesystems could be implemented. :3)
     **/
    private static class ReadOnlyFileSystem implements Callable<FileSystem> {

        private final String name;

        ReadOnlyFileSystem(String name) {
            this.name = name;
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public li.cil.oc.api.fs.FileSystem call() throws Exception {
            return asReadOnly(fromClass(MainRegistry.class, Tags.MODID, "disks/" + FloppyDisk.sanitizeName(name)));
        }
    }

    // Floppy disk class.
    public static class FloppyDisk {
        // Specifies the callable ReadOnlyFileSystem to allow OC to access the floppy.
        public final ReadOnlyFileSystem fs;
        // Specifies the color of the floppy disk (0-16 colors defined by OC).
        public final EnumDyeColor color;
        // Set after loading the disk; allows for adding a recipe to the item.
        public ItemStack item;

        FloppyDisk(String name, EnumDyeColor color) {
            this.fs = new ReadOnlyFileSystem(FloppyDisk.sanitizeName(name));
            this.color = color;
        }

        // Disk names will be sanitized before the FileSystem is created.
        // This only affects the location/directory, not the display name.
        // (Prevents filesystems from breaking/crashing due to having file separators, wildcards, etc.
        public static String sanitizeName(String input) {
            return input.toLowerCase().replaceAll("\\W", "");
        }
    }

    /**
     * Simple enum for mapping OC color ordinals to a nicer format for adding new disks.
     */
    public static EnumDyeColor[] OCColors = new EnumDyeColor[] {
        BLACK, //0x444444
        RED, //0xB3312C
        GREEN, //0x339911
        BROWN, //0x51301A
        BLUE, //0x6666FF
        PURPLE, //0x7B2FBE
        CYAN, //0x66FFFF
        SILVER, //0xABABAB
        GRAY, //0x666666
        PINK, //0xD88198
        LIME, //0x66FF66
        YELLOW, //0xFFFF66
        LIGHT_BLUE, //0xAAAAFF
        MAGENTA, //0xC354CD
        ORANGE, //0xEB8844
        WHITE //0xF0F0F0
    };

    // Where all disks are stored with their name and `FloppyDisk` class.
    public static HashMap<String, FloppyDisk> disks = new HashMap<>();

    /**
     * Called in the FML PostLoad stage, after the OC API loads.
     * <br>
     * Loads various parts of OC compatibility.
     */
    public static void init() {
        if(Loader.isModLoaded("opencomputers")) {
            /*
            For anyone wanting to add their own floppy disks,
            read the README found in assets.hbm.disks.
            */

            // Idea/Code by instantnootles
            disks.put("PWRangler", new FloppyDisk("PWRangler", OCColors[6]));

            // begin registering disks
            Logger logger = LogManager.getLogger("HBM");
            logger.info("Loading OpenComputers disks...");
            if(disks.isEmpty()) {
                logger.warn("No disks registered; see com.hbm.handler.CompatHandler.disks");
                return;
            }
            disks.forEach((s, disk) -> {

                // Test if the disk path even exists.
                FileSystem fs = fromClass(MainRegistry.class, Tags.MODID, "disks/" + disk.fs.name);

                if (fs == null) { // Disk path does NOT exist, and it should not be loaded.

                    logger.error("Error loading disk: {} at /assets/" + Tags.MODID + "/disks/{}", s, disk.fs.name);
                    logger.error("This is likely due to the path to the disk being non-existent.");

                } else { // Disk path DOES exist, and it should be loaded.

                    disk.item = Items.registerFloppy(s, disk.color, disk.fs, true); // The big part, actually registering the floppies!
                    logger.info("Registered disk: {} at /assets/" + Tags.MODID + "/disks/{}", s, disk.fs.name);

                }
            });
            logger.info("OpenComputers disks registered.");
            logger.info("What we have: {}", disks.keySet());
            // OC disk recipes!
            List<ItemStack> floppyDisks = new RecipesCommon.OreDictStack("oc:floppy").toStacks();

            if(!floppyDisks.isEmpty()) { //check that floppy disks even exist in oredict.
                // Recipes must be initialized here, since if they were initialized in `CraftingManager` then the disk item would not be created yet.
                // FIXME: This didn't add the correct recipes for the PWRangler disk.
                addShapelessAuto(disks.get("PWRangler").item, new Object[] {"oc:floppy", new ItemStack(ModBlocks.block_bismuth)});

                logger.info("OpenComputers disk recipe added for PWRangler.");
            } else {
                logger.info("OpenComputers floppy disk oredict not found, recipes cannot be loaded!");
            }

            // boom, OC disks loaded
            logger.info("OpenComputers disks loaded.");
        }
    }

    // Null component name, default to this if broken to avoid NullPointerExceptions.
    public static final String nullComponent = "ntm_null";

    /**
     * This is an interface made specifically for adding OC compatibility to NTM machines. The {@link li.cil.oc.api.network.SimpleComponent} interface must also be implemented in the TE.
     * <br>
     * This interface is not required to be defined as an optional interface, though the {@link li.cil.oc.api.network.SimpleComponent} interface must be declared as an optional interface.
     * <br>
     * Pseudo multiblocks will automatically receive compatibility with their ports by proxying their `methods()` and `invoke()` functions. This is the only time they need to be defined.
     *
     **/
    @Optional.InterfaceList({
            @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers"),
            @Optional.Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "opencomputers"),
            @Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "opencomputers"),
    })
    @SimpleComponent.SkipInjection // make sure OC doesn't inject this shit into the interface and crash
    public interface OCComponent extends SimpleComponent, SidedComponent, ManagedPeripheral {

        /**
         * Must be overridden in the implemented TE, or it will default to "ntm_null".
         * <br>
         * Dictates the component name exposed to the computer.
         * @return String
         */
        @Override
        @Optional.Method(modid = "opencomputers")
        default String getComponentName() {
            return nullComponent;
        }

        /**
         * Tells OC which sides of the block cables should connect to.
         * @param side Side to check
         * @return If the side should be able to connect.
         */
        @Override
        @Optional.Method(modid = "opencomputers")
        default boolean canConnectNode(EnumFacing side) {
            return true;
        }

        /**
         * Standard methods array from {@link li.cil.oc.api.network.ManagedPeripheral} extending {@link li.cil.oc.api.network.SimpleComponent}.
         * @return Array of methods to expose to the computer.
         */
        @Override
        @Optional.Method(modid = "opencomputers")
        default String[] methods() {return new String[0];}

        /**
         * Standard invoke function from {@link li.cil.oc.api.network.ManagedPeripheral} extending {@link li.cil.oc.api.network.SimpleComponent}.
         * @return Data to the computer as a return from the function.
         */
        @Override
        @Optional.Method(modid = "opencomputers")
        default Object[] invoke(String method, Context context, Arguments args) throws Exception {return null;}
    }
}
