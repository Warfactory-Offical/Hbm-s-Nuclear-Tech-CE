package com.hbm.packet.toserver;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.items.weapon.ItemCrucible;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.RailgunCallbackPacket;
import com.hbm.packet.toclient.RailgunFirePacket;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.bomb.TileEntityLaunchTable;
import com.hbm.tileentity.bomb.TileEntityRailgun;
import com.hbm.tileentity.machine.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AuxButtonPacket implements IMessage {

    int x;
    int y;
    int z;
    int value;
    int id;

    public AuxButtonPacket() {

    }

    public AuxButtonPacket(int x, int y, int z, int value, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
        this.id = id;
    }

    public AuxButtonPacket(BlockPos pos, int value, int id) {
        this(pos.getX(), pos.getY(), pos.getZ(), value, id);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        value = buf.readInt();
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(value);
        buf.writeInt(id);
    }

    public static class Handler implements IMessageHandler<AuxButtonPacket, IMessage> {

        @Override
        public IMessage onMessage(AuxButtonPacket m, MessageContext ctx) {
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                EntityPlayer p = ctx.getServerHandler().player;
                BlockPos pos = new BlockPos(m.x, m.y, m.z);

                //why make new packets when you can just abuse and uglify the existing ones?
                if (m.value == 999) {
                    if (GeneralConfig.duckButton) {
                        NBTTagCompound perDat = p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                        if (!perDat.getBoolean("hasDucked")) {
                            EntityDuck ducc = new EntityDuck(p.world);
                            ducc.setPosition(p.posX, p.posY + p.eyeHeight, p.posZ);

                            Vec3d vec = p.getLookVec();
                            ducc.motionX = vec.x;
                            ducc.motionY = vec.y;
                            ducc.motionZ = vec.z;

                            p.world.spawnEntity(ducc);
                            p.world.playSound(null, p.posX, p.posY, p.posZ, HBMSoundHandler.ducc, SoundCategory.PLAYERS, 1.0F, 1.0F);

                            perDat.setBoolean("hasDucked", true);

                            p.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, perDat);
                        }
                    }
                    return;
                }
                if (m.id == 1000) {
                    boolean clicked = m.value > 0;
                    if (ctx.getServerHandler().player.getHeldItemMainhand().getItem() instanceof ItemCrucible) {
                        ItemCrucible.doSpecialClick = clicked;
                    }
                    return;
                }
				/*if(m.value == 1000){
					NBTTagCompound perDat = p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
					int lightning = perDat.getInteger("lightningCharge");
					if(lightning == 0){
						perDat.setInteger("lightningCharge", 1);
					}
				}*/

                if (!p.world.isBlockLoaded(pos))
                    return;
                //try {
                TileEntity te = p.world.getTileEntity(pos);
					/*if (te instanceof TileEntityRadioRec) {
						TileEntityRadioRec radio = (TileEntityRadioRec)te;
						
						if(m.id == 0) {
							radio.isOn = (m.value == 1);
						}
						
						if(m.id == 1) {
							radio.freq = ((double)m.value) / 10D;
						}
					}
					
					*/
                if (te instanceof TileEntityForceField) {
                    TileEntityForceField field = (TileEntityForceField) te;

                    field.isOn = !field.isOn;
                }


                if (te instanceof TileEntityMachineMissileAssembly) {
                    TileEntityMachineMissileAssembly assembly = (TileEntityMachineMissileAssembly) te;

                    assembly.construct();
                }

                if (te instanceof TileEntityLaunchTable) {
                    TileEntityLaunchTable launcher = (TileEntityLaunchTable) te;

                    launcher.padSize = PartSize.values()[m.value];
                }

                if (te instanceof TileEntityRailgun) {
                    TileEntityRailgun gun = (TileEntityRailgun) te;

                    if (m.id == 0) {
                        if (gun.setAngles(false)) {
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.buttonYes, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.railgunOrientation, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            PacketDispatcher.wrapper.sendToAll(new RailgunCallbackPacket(m.x, m.y, m.z, gun.pitch, gun.yaw));
                        } else {
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.buttonNo, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }

                    if (m.id == 1) {
                        if (gun.canFire()) {
                            gun.fireDelay = TileEntityRailgun.cooldownDurationTicks;
                            PacketDispatcher.wrapper.sendToAll(new RailgunFirePacket(m.x, m.y, m.z));
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.buttonYes, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.railgunCharge, SoundCategory.BLOCKS, 10.0F, 1.0F);
                        } else {
                            p.world.playSound(null, m.x, m.y, m.z, HBMSoundHandler.buttonNo, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
                if (te instanceof TileEntityBarrel) {
                    TileEntityBarrel barrel = (TileEntityBarrel) te;

                    barrel.mode = (short) ((barrel.mode + 1) % TileEntityBarrel.modes);
                    barrel.markDirty();
                }
                if (te instanceof TileEntityCoreEmitter) {
                    TileEntityCoreEmitter core = (TileEntityCoreEmitter) te;

                    if (m.id == 0) {
                        core.watts = m.value;
                    }
                    if (m.id == 1) {
                        core.isOn = !core.isOn;
                    }
                }

                if (te instanceof TileEntityCoreStabilizer) {
                    TileEntityCoreStabilizer core = (TileEntityCoreStabilizer) te;

                    if (m.id == 0) {
                        core.watts = m.value;
                    }
                }

                if (te instanceof TileEntitySoyuzLauncher) {
                    TileEntitySoyuzLauncher launcher = (TileEntitySoyuzLauncher) te;

                    if (m.id == 0)
                        launcher.mode = (byte) m.value;
                    if (m.id == 1)
                        launcher.startCountdown();
                }
                if (te instanceof TileEntityMachineBattery) {
                    TileEntityMachineBattery bat = (TileEntityMachineBattery) te;

                    if (m.id == 0) {
                        bat.redLow = (short) ((bat.redLow + 1) % 4);
                        bat.markDirty();
                    }

                    if (m.id == 1) {
                        bat.redHigh = (short) ((bat.redHigh + 1) % 4);
                        bat.markDirty();
                    }

                    if (m.id == 2) {
                        switch (bat.priority) {
                            case LOW:
                                bat.priority = IEnergyReceiverMK2.ConnectionPriority.NORMAL;
                                break;
                            case NORMAL:
                                bat.priority = IEnergyReceiverMK2.ConnectionPriority.HIGH;
                                break;
                            case HIGH:
                                bat.priority = IEnergyReceiverMK2.ConnectionPriority.LOW;
                                break;
                        }
                        bat.markDirty();
                    }
                }
                if (te instanceof TileEntityMachineMiningLaser) {
                    TileEntityMachineMiningLaser laser = (TileEntityMachineMiningLaser) te;

                    laser.isOn = !laser.isOn;
                }

                if (te instanceof TileEntityMachineRadarNT) {
                    TileEntityMachineRadarNT radar = (TileEntityMachineRadarNT) te;
                    radar.handleButtonPacket(m.value, m.id);
                }
                /// yes ///
                if (te instanceof TileEntityMachineBase) {
                    TileEntityMachineBase base = (TileEntityMachineBase) te;
                    base.handleButtonPacket(m.value, m.id);
                }

                //} catch (Exception x) { }
            });


            return null;
        }
    }

}
