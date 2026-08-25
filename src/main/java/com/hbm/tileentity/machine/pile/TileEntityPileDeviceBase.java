package com.hbm.tileentity.machine.pile;

import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityTickingBase;

import io.netty.buffer.ByteBuf;

public abstract class TileEntityPileDeviceBase extends TileEntityTickingBase {

	public int chanNum;

	@Override
	public String getInventoryName() {
		return null;
	}

	public ForgeDirection getOrientation() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() % 4 + 2);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.chanNum);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.chanNum = buf.readInt();
	}
}
