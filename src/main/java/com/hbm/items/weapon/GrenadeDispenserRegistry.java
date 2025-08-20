package com.hbm.items.weapon;

import com.hbm.api.entity.EntityGrenadeFactory;
import com.hbm.entity.grenade.*;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFertilizer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GrenadeDispenserRegistry {
    private static final Map<Item, EntityGrenadeFactory> GRENADE_FACTORIES = new HashMap<>();

    static {
        GRENADE_FACTORIES.put(ModItems.grenade_generic, (w, p) -> new EntityGrenadeGeneric(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_strong, (w, p) -> new EntityGrenadeStrong(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_frag, (w, p) -> new EntityGrenadeFrag(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_fire, (w, p) -> new EntityGrenadeFire(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_shrapnel, (w, p) -> new EntityGrenadeShrapnel(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_cluster, (w, p) -> new EntityGrenadeCluster(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_flare, (w, p) -> new EntityGrenadeFlare(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_electric, (w, p) -> new EntityGrenadeElectric(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_poison, (w, p) -> new EntityGrenadePoison(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_gas, (w, p) -> new EntityGrenadeGas(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_cloud, (w, p) -> new EntityGrenadeCloud(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_pink_cloud, (w, p) -> new EntityGrenadePC(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_smart, (w, p) -> new EntityGrenadeSmart(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_mirv, (w, p) -> new EntityGrenadeMIRV(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_breach, (w, p) -> new EntityGrenadeBreach(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_burst, (w, p) -> new EntityGrenadeBurst(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_pulse, (w, p) -> new EntityGrenadePulse(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_plasma, (w, p) -> new EntityGrenadePlasma(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_tau, (w, p) -> new EntityGrenadeTau(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_schrabidium, (w, p) -> new EntityGrenadeSchrabidium(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_nuke, (w, p) -> new EntityGrenadeNuke(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_lemon, (w, p) -> new EntityGrenadeLemon(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_gascan, (w, p) -> new EntityGrenadeGascan(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_mk2, (w, p) -> new EntityGrenadeMk2(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_aschrab, (w, p) -> new EntityGrenadeASchrab(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_nuclear, (w, p) -> new EntityGrenadeNuclear(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_zomg, (w, p) -> new EntityGrenadeZOMG(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_solinium, (w, p) -> new EntityGrenadeSolinium(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_black_hole, (w, p) -> new EntityGrenadeBlackHole(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_generic, (w, p) -> new EntityGrenadeIFGeneric(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_he, (w, p) -> new EntityGrenadeIFHE(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_bouncy, (w, p) -> new EntityGrenadeIFBouncy(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_sticky, (w, p) -> new EntityGrenadeIFSticky(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_impact, (w, p) -> new EntityGrenadeIFImpact(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_incendiary, (w, p) -> new EntityGrenadeIFIncendiary(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_toxic, (w, p) -> new EntityGrenadeIFToxic(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_concussion, (w, p) -> new EntityGrenadeIFConcussion(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_brimstone, (w, p) -> new EntityGrenadeIFBrimstone(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_mystery, (w, p) -> new EntityGrenadeIFMystery(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_spark, (w, p) -> new EntityGrenadeIFSpark(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_hopwire, (w, p) -> new EntityGrenadeIFHopwire(w, p.getX(), p.getY(), p.getZ()));
        GRENADE_FACTORIES.put(ModItems.grenade_if_null, (w, p) -> new EntityGrenadeIFNull(w, p.getX(), p.getY(), p.getZ()));
    }

    public static void registerDispenserBehaviors() {
        for (Map.Entry<Item, EntityGrenadeFactory> entry : GRENADE_FACTORIES.entrySet()) {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(entry.getKey(), new BehaviorProjectileDispense() {
                @Override
                protected @NotNull IProjectile getProjectileEntity(@NotNull World world, @NotNull IPosition pos, @NotNull ItemStack stack) {
                    return entry.getValue().create(world, pos);
                }
            });
        }
    }

    public static void registerDispenserBehaviorFertilizer(){
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ModItems.powder_fertilizer, new BehaviorDefaultDispenseItem() {

            private boolean dispenseSound = true;

            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                EnumFacing facing = (EnumFacing) source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos targetPos = source.getBlockPos().offset(facing);
                this.dispenseSound = ItemFertilizer.useFertillizer(stack, world, targetPos.getX(), targetPos.getY(), targetPos.getZ());
                return stack;
            }

            @Override
            protected void playDispenseSound(IBlockSource source) {
                World world = source.getWorld();
                BlockPos pos = source.getBlockPos();
                if (this.dispenseSound) {
                    world.playEvent(1000, pos, 0);
                } else {
                    world.playEvent(1001, pos, 0);
                }
            }
        });
    }
}
