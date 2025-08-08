package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.Nodespace.PowerNode;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.util.ITickable;

@AutoRegister
public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2, ITickable {
	protected PowerNode node;

	@Override
	public void update() {

		if(!world.isRemote) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = Nodespace.getNode(world, pos);

					if(this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(world, this.node);
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!world.isRemote) {
			if(this.node != null) {
				Nodespace.destroyNode(world, pos);
			}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}
}
