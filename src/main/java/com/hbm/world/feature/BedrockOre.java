package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BedrockOre extends AbstractPhasedStructure {

    private final ItemStack resourceStack;
    private final FluidStack acidRequirement;
    private final int color;
    private final int tier;
    private final Block depthRock;

    private BedrockOre(ItemStack stack, FluidStack acid, int color, int tier, Block depthRock) {
        this.resourceStack = stack.copy();
        this.acidRequirement = acid;
        this.color = color;
        this.tier = tier;
        this.depthRock = depthRock;
    }

    public static void generate(World world, int x, int z, ItemStack stack, FluidStack acid, int color, int tier, Block depthRock) {
        BedrockOre oreTask = new BedrockOre(stack, acid, color, tier, depthRock);
        BlockPos position = new BlockPos(x, 0, z);
        oreTask.generate(world, world.rand, position);
    }

    public static void generate(World world, int x, int z, ItemStack stack, FluidStack acid, int color, int tier) {
        generate(world, x, z, stack, acid, color, tier, ModBlocks.stone_depth);
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    @NotNull
    @Override
    public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
        return Arrays.asList(
                origin.add(-3, 0, -3),
                origin.add(3, 0, -3),
                origin.add(-3, 0, 3),
                origin.add(3, 0, 3)
        );
    }

    @NotNull
    @Override
    public Optional<PhasedStructureGenerator.ReadyToGenerateStructure> validate(World world, PhasedStructureGenerator.PendingValidationStructure pending) {
        if (checkSpawningConditions(world, pending.origin)) {
            return Optional.of(new PhasedStructureGenerator.ReadyToGenerateStructure(pending, pending.origin));
        }
        return Optional.empty();
    }

    @Override
    public boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos origin) {
        return world.getBlockState(origin).getBlock() == Blocks.BEDROCK;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        this.executeOriginalLogic(world, rand, finalOrigin);
    }

    private void executeOriginalLogic(World world, Random rand, BlockPos finalOrigin) {
        int x = finalOrigin.getX();
        int z = finalOrigin.getZ();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int ix = x - 1; ix <= x + 1; ix++) {
            for (int iz = z - 1; iz <= z + 1; iz++) {
                pos.setPos(ix, 0, iz);
                IBlockState state = world.getBlockState(pos);
                Block b = state.getBlock();

                if (b == Blocks.BEDROCK) {
                    if ((ix == x && iz == z) || rand.nextBoolean()) {
                        world.setBlockState(pos, ModBlocks.ore_bedrock_block.getDefaultState(), 3);

                        TileEntity tile = world.getTileEntity(pos);
                        if (tile instanceof BlockBedrockOreTE.TileEntityBedrockOre) {
                            BlockBedrockOreTE.TileEntityBedrockOre ore = (BlockBedrockOreTE.TileEntityBedrockOre) tile;
                            ore.resource = this.resourceStack;
                            ore.color = this.color;
                            ore.shape = rand.nextInt(10);
                            ore.acidRequirement = this.acidRequirement;
                            ore.tier = this.tier;
                            ore.markDirty();
                            world.notifyBlockUpdate(pos, state, world.getBlockState(pos), 3);
                        }
                    }
                }
            }
        }

        for (int ix = x - 3; ix <= x + 3; ix++) {
            for (int iz = z - 3; iz <= z + 3; iz++) {
                for (int iy = 1; iy < 7; iy++) {
                    pos.setPos(ix, iy, iz);
                    IBlockState state = world.getBlockState(pos);
                    Block b = state.getBlock();

                    if (iy < 3 || b == Blocks.BEDROCK) {
                        if (b == Blocks.STONE || b == Blocks.BEDROCK) {
                            world.setBlockState(pos, this.depthRock.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

}
