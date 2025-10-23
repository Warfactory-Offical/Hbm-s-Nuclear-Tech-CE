package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

@AutoRegister
public class TileEntityMachineCompressor extends TileEntityMachineCompressorBase {

    public float fanSpin;
    public float prevFanSpin;
    public float piston;
    public float prevPiston;
    public boolean pistonDir;
    private float randSpeed = 0.1F;

    @Override
    public void update() {
        super.update();

        if(world.isRemote) {

            this.prevFanSpin = this.fanSpin;
            this.prevPiston = this.piston;

            if(this.isOn) {
                this.fanSpin += 15;

                if(this.fanSpin >= 360) {
                    this.prevFanSpin -= 360;
                    this.fanSpin -= 360;
                }

                if(this.pistonDir) {
                    this.piston -= randSpeed;
                    if(this.piston <= 0) {
                        MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.boltgun, SoundCategory.BLOCKS, this.getVolume(0.5F), 0.75F);
                        this.pistonDir = !this.pistonDir;
                    }
                } else {
                    this.piston += 0.05F;
                    if(this.piston >= 1) {
                        this.randSpeed = 0.085F + world.rand.nextFloat() * 0.03F;
                        this.pistonDir = !this.pistonDir;
                    }
                }

                this.piston = MathHelper.clamp(this.piston, 0F, 1F);
            }
        }
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - rot.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
        };
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 9,
                    pos.getZ() + 3
            );
        }

        return bb;
    }
}