package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.BreederRecipes;
import com.hbm.inventory.BreederRecipes.BreederRecipe;
import com.hbm.inventory.FusionRecipes;
import com.hbm.inventory.container.ContainerITER;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIITER;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemFusionShield;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.AdvancementManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityITER extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IFFtoNTMF {

	public long power;
	public static final long maxPower = 1000000000;
	public static final int powerReq = 500000;
	public int age = 0;
	public FluidTankNTM[] tanksNew;
	public FluidTankNTM plasmaNew;

	public FluidTank[] tanks;
	public Fluid[] types;
	public FluidTank plasma;
	public Fluid plasmaType;
	
	public int progress;
	public static final int duration = 100;

	@SideOnly(Side.CLIENT)
	public int blanket;

	public float rotor;
	public float lastRotor;
	public boolean isOn;
	private static boolean converted = false;

	public TileEntityITER() {
		super(5);
		tanksNew = new FluidTankNTM[2];
		tanksNew[0] = new FluidTankNTM(Fluids.WATER, 1280000, 0);
		tanksNew[1] = new FluidTankNTM(Fluids.ULTRAHOTSTEAM, 128000, 1);
		plasmaNew = new FluidTankNTM(Fluids.PLASMA_DT, 16000, 2);

		tanks = new FluidTank[2];
		types = new Fluid[2];
		tanks[0] = new FluidTank(12800000);
		types[0] = FluidRegistry.WATER;
		tanks[1] = new FluidTank(1280000);
		types[1] = ModForgeFluids.ultrahotsteam;
		plasma = new FluidTank(16000);

		converted = true;
	}

	@Override
	public String getName() {
		return "container.machineITER";
	}

	@Override
	public void update() {
		if (!converted){
			convertAndSetFluids(types, tanks, tanksNew);
			convertAndSetFluid(plasmaType, plasma, plasmaNew);
			converted = true;
		}
		if(!world.isRemote) {
			age++;
			if(age >= 20) {
				age = 0;
			}

			this.updateConnections();
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			/// START Processing part ///

			if(!isOn) {
				plasmaNew.setFill(0);	//jettison plasma if the thing is turned off
			}

			//explode either if there's plasma that is too hot or if the reactor is turned on but the magnets have no power
			if(plasmaNew.getFill() > 0 && (this.plasmaNew.getTankType().temperature >= this.getShield() || (this.isOn && this.power < this.powerReq))) {
				this.disassemble();
				Vec3 vec = Vec3.createVectorHelper(5.5, 0, 0);
				vec.rotateAroundY(world.rand.nextFloat() * (float) Math.PI * 2F);

				world.newExplosion(null, pos.getX() + 0.5 + vec.xCoord, pos.getY() + 0.5 + world.rand.nextGaussian() * 1.5D, pos.getZ() + 0.5 + vec.zCoord, 2.5F, true, true);
				RadiationSavedData.incrementRad(world, pos, 2000F, 10000F);
			}

			if(isOn && power >= powerReq) {
				power -= powerReq;

				if(plasmaNew.getFill() > 0) {

					int chance = FusionRecipes.getByproductChance(plasmaNew.getTankType());

					if(chance > 0 && world.rand.nextInt(chance) == 0)
						produceByproduct();
				}

				if(plasmaNew.getFill() > 0 && this.getShield() != 0) {

					ItemFusionShield.setShieldDamage(inventory.getStackInSlot(3), ItemFusionShield.getShieldDamage(inventory.getStackInSlot(3)) + 1);

					if(ItemFusionShield.getShieldDamage(inventory.getStackInSlot(3)) > ((ItemFusionShield)inventory.getStackInSlot(3).getItem()).maxDamage){
						inventory.setStackInSlot(3, ItemStack.EMPTY);
						world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.shutdown, SoundCategory.BLOCKS, 5F, 1F);
						this.isOn = false;
						this.markDirty();
					}
				}

				int prod = FusionRecipes.getSteamProduction(plasmaNew.getTankType());

				for(int i = 0; i < 20; i++) {

					if(plasmaNew.getFill() > 0) {

						if(tanksNew[0].getFill() >= prod * 10) {
							tanksNew[0].setFill(tanksNew[0].getFill() - prod * 10);
							tanksNew[1].setFill(tanksNew[1].getFill() + prod);

							if(tanksNew[1].getFill() > tanksNew[1].getMaxFill())
								tanksNew[1].setFill(tanksNew[1].getMaxFill());
						}

						plasmaNew.setFill(plasmaNew.getFill() - 1);
					}
				}
			}
			doBreederStuff();
			/// END Processing part ///

			/// START Notif packets ///
			for(int i = 0; i < tanksNew.length; i++)
				tanksNew[i].updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());
			plasmaNew.updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());

			for(DirPos pos : getConPos()) {
				if(tanksNew[1].getFill() > 0) {
					this.sendFluid(tanksNew[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("isOn", isOn);
			data.setLong("power", power);
			data.setInteger("progress", progress);

			if(inventory.getStackInSlot(3).isEmpty()) {
				data.setInteger("blanket", 0);
			} else if(inventory.getStackInSlot(3).getItem() == ModItems.fusion_shield_tungsten) {
				data.setInteger("blanket", 1);
			} else if(inventory.getStackInSlot(3).getItem() == ModItems.fusion_shield_desh) {
				data.setInteger("blanket", 2);
			} else if(inventory.getStackInSlot(3).getItem() == ModItems.fusion_shield_chlorophyte) {
				data.setInteger("blanket", 3);
			} else if(inventory.getStackInSlot(3).getItem() == ModItems.fusion_shield_vaporwave) {
				data.setInteger("blanket", 4);
			}

			this.networkPack(data, 250);
		} else {

			this.lastRotor = this.rotor;

			if(this.isOn && this.power >= powerReq) {

				this.rotor += 15F;

				if(this.rotor >= 360) {
					this.rotor -= 360;
					this.lastRotor -= 360;
				}
			}
		}
	}

	protected List<DirPos> connections;

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	protected List<DirPos> getConPos() {
		if(connections != null && !connections.isEmpty())
			return connections;

		connections = new ArrayList();

		connections.add(new DirPos(pos.getX(), pos.getY() + 3, pos.getZ(), ForgeDirection.UP));
		connections.add(new DirPos(pos.getX(), pos.getY() - 3, pos.getZ(), ForgeDirection.DOWN));

		Vec3 vec = Vec3.createVectorHelper(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {
			vec.rotateAroundY((float) (Math.PI / 8));
			connections.add(new DirPos(pos.getX() + (int)vec.xCoord, pos.getY() + 3, pos.getZ() + (int)vec.zCoord, ForgeDirection.UP));
			connections.add(new DirPos(pos.getX() + (int)vec.xCoord, pos.getY() - 3, pos.getZ() + (int)vec.zCoord, ForgeDirection.DOWN));
		}

		return connections;
	}
	
	private void doBreederStuff() {

		if(plasmaNew.getFill() == 0) {
			this.progress = 0;
			return;
		}

		BreederRecipe out = BreederRecipes.getOutput(inventory.getStackInSlot(1));
		
		if(inventory.getStackInSlot(1).getItem() == ModItems.meteorite_sword_irradiated)
			out = new BreederRecipe(ModItems.meteorite_sword_fused, 1);

		if(inventory.getStackInSlot(1).getItem() == ModItems.meteorite_sword_fused)
			out = new BreederRecipe(ModItems.meteorite_sword_baleful, 4);

		if(out == null) {
			this.progress = 0;
			return;
		}

		if(!inventory.getStackInSlot(2).isEmpty() && inventory.getStackInSlot(2).getCount() >= inventory.getStackInSlot(2).getMaxStackSize()) {
			this.progress = 0;
			return;
		}

		int level = FusionRecipes.getBreedingLevel(plasmaNew.getTankType());

		if(out.flux > level) {
			this.progress = 0;
			return;
		}

		progress++;

		if(progress > duration) {

			this.progress = 0;

			if(!inventory.getStackInSlot(2).isEmpty()) {
				inventory.getStackInSlot(2).grow(1);
			} else {
				inventory.setStackInSlot(2, out.output.copy());
			}

			inventory.getStackInSlot(1).shrink(1);

			if(inventory.getStackInSlot(1).isEmpty())
				inventory.setStackInSlot(1, ItemStack.EMPTY);

			this.markDirty();
		}
	}

	private void produceByproduct() {

		ItemStack by = FusionRecipes.getByproduct(plasmaNew.getTankType());

		if(by == null)
			return;

		if(inventory.getStackInSlot(4).isEmpty()) {
			inventory.setStackInSlot(4, by);
			return;
		}

		if(inventory.getStackInSlot(4).getItem() == by.getItem() && inventory.getStackInSlot(4).getItemDamage() == by.getItemDamage() && inventory.getStackInSlot(4).getCount() < inventory.getStackInSlot(4).getMaxStackSize()) {
			inventory.getStackInSlot(4).grow(1);
		}
	}

	public int getShield() {

		if(inventory.getStackInSlot(3).isEmpty() || !(inventory.getStackInSlot(3).getItem() instanceof ItemFusionShield))
			return 0;

		return ((ItemFusionShield) inventory.getStackInSlot(3).getItem()).maxTemp + 273;
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		this.isOn = data.getBoolean("isOn");
		this.power = data.getLong("power");
		this.blanket = data.getInteger("blanket");
		this.progress = data.getInteger("progress");
	}

	@Override
	public void handleButtonPacket(int value, int meta) {

		if(meta == 0) {
			this.isOn = !this.isOn;
		}
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public long getProgressScaled(long i) {
		return (progress * i) / duration;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		if(!converted){
			tanks[0].readFromNBT(nbt.getCompoundTag("water"));
			tanks[1].readFromNBT(nbt.getCompoundTag("steam"));
			plasma.readFromNBT(nbt.getCompoundTag("plasma"));
			plasmaType = FluidRegistry.getFluid(nbt.getString("plasma_type"));
		} else {
			tanksNew[0].readFromNBT(nbt, "water");
			tanksNew[1].readFromNBT(nbt, "steam");
			if(nbt.hasKey("water")){
				nbt.removeTag("water");
				nbt.removeTag("steam");
				nbt.removeTag("plasma");
				nbt.removeTag("plasma_type");
			}
		}
		plasmaNew.readFromNBT(nbt, "plasma");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", isOn);
		if(!converted){
			nbt.setTag("water", tanks[0].writeToNBT(new NBTTagCompound()));
			nbt.setTag("steam", tanks[1].writeToNBT(new NBTTagCompound()));
			nbt.setTag("plasma", plasma.writeToNBT(new NBTTagCompound()));
			if(plasmaType != null)
				nbt.setString("plasma_type", plasmaType.getName());
		} else {
			tanksNew[0].writeToNBT(nbt, "water");
			tanksNew[1].writeToNBT(nbt, "steam");
			plasmaNew.writeToNBT(nbt, "plasma");
		}
		return super.writeToNBT(nbt);
	}

	public void disassemble() {
		
		MachineITER.drop = false;

		int[][][] layout = TileEntityITERStruct.layout;

		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < layout[0].length; x++) {
				for(int z = 0; z < layout[0][0].length; z++) {

					int ly = y > 2 ? 4 - y : y;

					int width = 7;

					if(x == width && y == 0 && z == width)
						continue;

					int b = layout[ly][x][z];

					switch(b) {
					case 1:
						world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_conductor.getDefaultState());
						break;
					case 2:
						world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_center.getDefaultState());
						break;
					case 3:
						world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_motor.getDefaultState());
						break;
					case 4:
						world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.reinforced_glass.getDefaultState());
						break;
					}
				}
			}
		}

		world.setBlockState(new BlockPos(pos.getX(), pos.getY() - 2, pos.getZ()), ModBlocks.struct_iter_core.getDefaultState());
		
		MachineITER.drop = true;
		
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
				new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).grow(50, 10, 50));

		for(EntityPlayer player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.achMeltdown);
		}
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanksNew[1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanksNew[0]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanksNew;
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return true;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 2, 4 };
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = new AxisAlignedBB(pos.getX() + 0.5 - 8, pos.getY() + 0.5 - 3, pos.getZ() + 0.5 - 8, pos.getX() + 0.5 + 8, pos.getY() + 0.5 + 3, pos.getZ() + 0.5 + 8);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerITER(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIITER(player.inventory, this);
	}


	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), this.getSendingTanks())
			);
		}
		return super.getCapability(capability, facing);
	}
}
