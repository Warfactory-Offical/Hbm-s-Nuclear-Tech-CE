package com.hbm.tileentity.turret;

import com.hbm.handler.CasingEjector;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretFriendly;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretFriendly extends TileEntityTurretChekhov implements IGUIProvider {

	static List<Integer> configs = new ArrayList<>();
	
	static {
		configs.add(XFactory556mm.r556_sp.id);
		configs.add(XFactory556mm.r556_fmj.id);
		configs.add(XFactory556mm.r556_jhp.id);
		configs.add(XFactory556mm.r556_ap.id);
	}
	
	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getName() {
		return "container.turretFriendly";
	}

	@Override
	public int getDelay() {
		return 5;
	}

	protected static CasingEjector ejector = new CasingEjector().setMotion(-0.3, 0.6, 0).setAngleRange(0.02F, 0.05F);

	@Override
	protected CasingEjector getEjector() {
		return ejector;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretFriendly(player.inventory, this);
	}
}
