package com.hbm.world.feature;

import com.google.common.base.Predicate;
import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DepthDeposit extends AbstractPhasedStructure {

    private final int size;
    private final double fill;
    private final Block oreBlock;
    private final Block filler;
    private final Predicate<IBlockState> matcher;

    private DepthDeposit(int size, double fill, Block oreBlock, Block genTarget, Block filler) {
        this.size = size;
        this.fill = fill;
        this.oreBlock = oreBlock;
        this.filler = filler;
        this.matcher = state -> {
            if (state == null) return false;
            Block block = state.getBlock();
            return block == genTarget || block == Blocks.BEDROCK;
        };
    }

    public static void generateConditionOverworld(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand,
                                                  int chance) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        new DepthDeposit(size, fill, block, Blocks.STONE, ModBlocks.stone_depth).generate(world, rand, new BlockPos(cx, cy, cz));
    }

    public static void generateConditionNether(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand,
                                               int chance) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        new DepthDeposit(size, fill, block, Blocks.NETHERRACK, ModBlocks.stone_depth_nether).generate(world, rand, new BlockPos(cx, cy, cz));
    }

    public static void generateCondition(World world, int x, int yMin, int yDev, int z, int size, double fill, Block block, Random rand, int chance,
                                         Block genTarget, Block filler) {
        if (rand.nextInt(chance) != 0) return;

        int cx = x + rand.nextInt(16);
        int cy = yMin + rand.nextInt(yDev);
        int cz = z + rand.nextInt(16);

        new DepthDeposit(size, fill, block, genTarget, filler).generate(world, rand, new BlockPos(cx, cy, cz));
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    @Override
    public @NotNull List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
        int r = this.size;
        return Arrays.asList(origin.add(-r, 0, -r), origin.add(r, 0, -r), origin.add(-r, 0, r), origin.add(r, 0, r));
    }

    @Override
    public @NotNull Optional<PhasedStructureGenerator.ReadyToGenerateStructure> validate(@NotNull World world,
                                                                                         @NotNull PhasedStructureGenerator.PendingValidationStructure pending) {
        BlockPos realOrigin = pending.origin;
        if (checkSpawningConditions(world, realOrigin)) {
            return Optional.of(new PhasedStructureGenerator.ReadyToGenerateStructure(pending, realOrigin));
        }
        return Optional.empty();
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        generateSphere(world, finalOrigin.getX(), finalOrigin.getY(), finalOrigin.getZ(), rand);
    }

    private void generateSphere(World world, int cx, int cy, int cz, Random rand) {
        if (world.isRemote) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int ix = cx - size; ix <= cx + size; ix++) {
            int dx = ix - cx;
            for (int jy = cy - size; jy <= cy + size; jy++) {
                if (jy < 1 || jy > 126) continue;

                int dy = jy - cy;
                for (int kz = cz - size; kz <= cz + size; kz++) {
                    int dz = kz - cz;

                    pos.setPos(ix, jy, kz);

                    IBlockState state = world.getBlockState(pos);
                    Block current = state.getBlock();

                    //yes you've heard right, bedrock
                    if (!current.isReplaceableOreGen(state, world, pos, matcher)) {
                        continue;
                    }

                    double len = Math.sqrt(dx * (double) dx + dy * (double) dy + dz * (double) dz);

                    if (len + rand.nextInt(2) < size * fill) {
                        world.setBlockState(pos, oreBlock.getDefaultState(), 2 | 16);
                    } else if (len + rand.nextInt(2) <= size) {
                        world.setBlockState(pos, filler.getDefaultState(), 2 | 16);
                    }
                }
            }
        }
    }
}
