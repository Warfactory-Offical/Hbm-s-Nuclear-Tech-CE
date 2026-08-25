package com.hbm.tileentity.machine;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.interfaces.AutoRegister;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.TileEntityTickingBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_satlink")
public class TileEntityMachineSatLink extends TileEntityTickingBase implements ITickable, IRORValueProvider, IRORInteractive {

	public boolean connected;
	public int freq;

	public float rot = INACTIVE_ROT;
	public float prevRot = INACTIVE_ROT;
	public float lift = INACTIVE_LIFT;
	public float prevLift = INACTIVE_LIFT;

	public static final float SPEED = 0.25F;
	public static final float ACTIVE_ROT = -15F;
	public static final float ACTIVE_LIFT = -45F;
	public static final float INACTIVE_ROT = 0F;
	public static final float INACTIVE_LIFT = -85F;

	public ITextComponent[] info = new ITextComponent[0];

	@Override
	public String getInventoryName() {
		return "tile.machine_satlink.name";
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			this.connected = false;

			if(world.getHeight(pos.getX(), pos.getZ()) <= pos.getY()) {
				SatelliteSavedData dat = SatelliteSavedData.getData(world);
				this.connected = dat.isFreqTaken(freq);
			}

			this.updateInfo(connected);
			this.networkPackNT(150);

		} else {

			this.prevRot = this.rot;
			this.prevLift = this.lift;

			float targetR = this.connected ? ACTIVE_ROT : INACTIVE_ROT;
			float targetL = this.connected ? ACTIVE_LIFT : INACTIVE_LIFT;

			if(Math.abs(rot - targetR) <= SPEED) rot = targetR;
			else if(rot < targetR) rot += SPEED;
			else if(rot > targetR) rot -= SPEED;

			if(Math.abs(lift - targetL) <= SPEED) lift = targetL;
			else if(lift < targetL) lift += SPEED;
			else if(lift > targetL) lift -= SPEED;
		}
	}

	protected void updateInfo(boolean canConnect) {

		if(!canConnect) {
			if(this.info.length > 0) this.info = new ITextComponent[0];
			return;
		}

		SatelliteSavedData dat = SatelliteSavedData.getData(world);
		Satellite sat = dat.getSatFromFreq(freq);

		if(sat != null) {
			this.info = sat.getInfo(world);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(connected);
		buf.writeInt(freq);
		buf.writeInt(info.length);
		for(ITextComponent comp : info) ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(comp));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.connected = buf.readBoolean();
		this.freq = buf.readInt();
		this.info = new ITextComponent[buf.readInt()];
		for(int i = 0; i < info.length; i++) {
			ITextComponent comp = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
			this.info[i] = comp != null ? comp : new TextComponentString("");
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.freq = nbt.getInteger("freq");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("freq", freq);
		return super.writeToNBT(nbt);
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "connected",
				PREFIX_VALUE + "freq",
				PREFIX_VALUE + "rx",
				PREFIX_VALUE + "type",
				PREFIX_FUNCTION + "setfreq" + NAME_SEPARATOR + "freq",
				PREFIX_FUNCTION + "tx" + NAME_SEPARATOR + "payload"
		};
	}

	@Override
	public String provideRORValue(String name) {

		if(name.equals(PREFIX_VALUE + "connected")) {
			return this.connected ? "TRUE" : "FALSE";
		}

		if(name.equals(PREFIX_VALUE + "freq")) {
			return "" + this.freq;
		}

		if(name.equals(PREFIX_VALUE + "type")) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			return sat != null ? sat.getType() : "";
		}

		if(name.equals(PREFIX_VALUE + "rx")) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			return sat != null ? sat.tx : "";
		}

		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if(name.equals(PREFIX_FUNCTION + "setfreq") && params.length == 1) {
			this.freq = IRORInteractive.parseInt(params[0], 0, 100_000);
			this.markChanged();
		}

		if(name.equals(PREFIX_FUNCTION + "tx")) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			String[] cmd = String.join(IRORInteractive.PARAM_SEPARATOR, params).split(" ");
			if(sat != null) {
				sat.onCommand(world, cmd);
				dat.markDirty();
			}
			this.markChanged();
		}

		return null;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 4, pos.getZ() + 3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
