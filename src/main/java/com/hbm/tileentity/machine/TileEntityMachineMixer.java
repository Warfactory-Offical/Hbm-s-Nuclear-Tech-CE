package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.MixerRecipes;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.container.ContainerMixer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMixer;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.INBTPacketReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityMachineMixer extends TileEntityMachineBase implements ITickable, IGUIProvider, IFluidStandardTransceiver, IEnergyReceiverMK2, INBTPacketReceiver, IFFtoNTMF
{

	public long power;
	public static final long maxPower = 10_000;
	public int progress;
	public int processTime;
	public int recipeIndex;

	public float rotation;
	public float prevRotation;
	public boolean wasOn = false;

	private int consumption = 50;

	public FluidTankNTM[] tanksNew;
	public FluidTank[] tanks;
	public Fluid outputFluid;
	public Fluid[] fluids;
	private final UpgradeManager upgradeManager = new UpgradeManager();
	private static boolean converted = false;

	public TileEntityMachineMixer() {
		super(5);
		this.tanksNew = new FluidTankNTM[3];
		this.tanksNew[0] = new FluidTankNTM(Fluids.NONE, 16_000);
		this.tanksNew[1] = new FluidTankNTM(Fluids.NONE, 16_000);
		this.tanksNew[2] = new FluidTankNTM(Fluids.NONE, 24_000);

		this.tanks = new FluidTank[3];
		this.outputFluid = null;
		this.tanks[0] = new FluidTank(16_000); //Input 1
		this.tanks[1] = new FluidTank(16_000); //Input 2
		this.tanks[2] = new FluidTank(24_000); //Output

		this.fluids = new Fluid[3];
		this.fluids[0] = ModForgeFluids.none;
		this.fluids[1] = ModForgeFluids.none;
		this.fluids[2] = ModForgeFluids.none;

		converted = true;
	}

	@Override
	public String getName() {
		return "container.machineMixer";
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluids(fluids, tanks, tanksNew);
				converted = true;
			}
			this.power = Library.chargeTEFromItems(inventory, 0, power, getMaxPower());
			tanksNew[2].setType(2, inventory);
			
			upgradeManager.eval(inventory, 3, 4);
			int speedLevel = Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3);
			int powerLevel = Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3);
			int overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			this.consumption = getConsumption();

			this.consumption *= (speedLevel+1);
			this.consumption /= (powerLevel+1);
			this.consumption *= (overLevel * 3 + 1);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				if(tanksNew[0].getTankType() != Fluids.NONE) this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				if(tanksNew[1].getTankType() != Fluids.NONE) this.trySubscribe(tanksNew[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
			
			this.wasOn = this.canProcess();
			
			if(this.wasOn) {
				this.progress++;
				this.power -= this.getConsumption();
				
				this.processTime -= this.processTime * speedLevel / 4;
				this.processTime /= (overLevel + 1);
				
				if(processTime <= 0) this.processTime = 1;
				
				if(this.progress >= this.processTime) {
					this.process();
					this.progress = 0;
				}
				
			} else {
				this.progress = 0;
			}

			for(DirPos pos : getConPos()) {
				if(tanksNew[2].getFill() > 0) this.sendFluid(tanksNew[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
			
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			data.setInteger("processTime", processTime);
			data.setInteger("progress", progress);
			data.setInteger("recipe", recipeIndex);
			data.setBoolean("wasOn", wasOn);
			for(int i = 0; i < 3; i++) {
				tanksNew[i].writeToNBT(data, i + "");
			}
			this.networkPackNT(50);
			
		} else {
			
			this.prevRotation = this.rotation;

			if(this.wasOn) {
				this.rotation += 20F;
			}
			
			if(this.rotation >= 360) {
				this.rotation -= 360;
				this.prevRotation -= 360;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(processTime);
		buf.writeInt(progress);
		buf.writeInt(recipeIndex);
		buf.writeBoolean(wasOn);

		for(int i = 0; i < tanksNew.length; i++) tanksNew[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		processTime = buf.readInt();
		progress = buf.readInt();
		recipeIndex = buf.readInt();
		wasOn = buf.readBoolean();

		for(int i = 0; i < tanksNew.length; i++) tanksNew[i].deserialize(buf);
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		super.networkUnpack(nbt);

		this.power = nbt.getLong("power");
		this.processTime = nbt.getInteger("processTime");
		this.progress = nbt.getInteger("progress");
		this.recipeIndex = nbt.getInteger("recipe");
		this.wasOn = nbt.getBoolean("wasOn");
		if(!converted){
			if(nbt.hasKey("f")) {
				if(nbt.getString("f").equals("None"))
					this.outputFluid = null;
				else
					this.outputFluid = FluidRegistry.getFluid(nbt.getString("f"));
			}
			if(nbt.hasKey("tanks")){
				FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
			}
		} else {
			for (int i = 0; i < 3; i++) {
				tanksNew[i].readFromNBT(nbt, i + "");
			}
		}
	}

	public boolean canProcess() {

		MixerRecipes.MixerRecipe[] recipes = MixerRecipes.getOutput(tanksNew[2].getTankType());
		if(recipes == null || recipes.length <= 0) {
			this.recipeIndex = 0;
			return false;
		}

		this.recipeIndex = this.recipeIndex % recipes.length;
		MixerRecipes.MixerRecipe recipe = recipes[this.recipeIndex];
		if(recipe == null) {
			this.recipeIndex = 0;
			return false;
		}

		tanksNew[0].setTankType(recipe.input1 != null ? recipe.input1.type : Fluids.NONE);
		tanksNew[1].setTankType(recipe.input2 != null ? recipe.input2.type : Fluids.NONE);

		if(recipe.input1 != null && tanksNew[0].getFill() < recipe.input1.fill) return false;
		if(recipe.input2 != null && tanksNew[1].getFill() < recipe.input2.fill) return false;

		/* simplest check would usually go first, but fluid checks also do the setup and we want that to happen even without power */
		if(this.power < getConsumption()) return false;

		if(recipe.output + tanksNew[2].getFill() > tanksNew[2].getMaxFill()) return false;

		if(recipe.solidInput != null) {

			if(inventory.getStackInSlot(1) == ItemStack.EMPTY) return false;

			if(!recipe.solidInput.matchesRecipe(inventory.getStackInSlot(1), true) || recipe.solidInput.getStack().getCount() > inventory.getStackInSlot(1).getCount()) return false;
		}

		this.processTime = recipe.processTime;
		return true;
	}

	protected void process() {

		MixerRecipes.MixerRecipe[] recipes = MixerRecipes.getOutput(tanksNew[2].getTankType());
		MixerRecipes.MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];

		if(recipe.input1 != null) tanksNew[0].setFill(tanksNew[0].getFill() - recipe.input1.fill);
		if(recipe.input2 != null) tanksNew[1].setFill(tanksNew[1].getFill() - recipe.input2.fill);
		if(recipe.solidInput != null) this.inventory.getStackInSlot(1).shrink(recipe.solidInput.getStack().getCount());
		tanksNew[2].setFill(tanksNew[2].getFill() + recipe.output);
	}
	
	public int getConsumption() {
		return consumption;
	}
	
	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.add(0, -1, 0), Library.NEG_Y),
				new DirPos(pos.add(1, 0, 0), Library.POS_X),
				new DirPos(pos.add(-1, 0, 0), Library.NEG_X),
				new DirPos(pos.add(0, 0, 1), Library.POS_Z),
				new DirPos(pos.add(0, 0, -1), Library.NEG_Z),
		};
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return new int[] { 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		MixerRecipes.MixerRecipe[] recipes = MixerRecipes.getOutput(tanksNew[2].getTankType());
		if(recipes == null || recipes.length <= 0) return false;

		MixerRecipes.MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];
		if(recipe == null || recipe.solidInput == null) return false;

		return recipe.solidInput.matchesRecipe(itemStack, true);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.progress = nbt.getInteger("progress");
		this.processTime = nbt.getInteger("processTime");
		this.recipeIndex = nbt.getInteger("recipe");
		if(!converted){
			if(nbt.hasKey("f")) {
				if(nbt.getString("f").equals("None"))
					this.outputFluid = null;
				else
					this.outputFluid = FluidRegistry.getFluid(nbt.getString("f"));
			}
			if(nbt.hasKey("tanks")){
				FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
			}
			this.fluids = new Fluid[3];
			this.fluids[0] = tanks[0].getFluid() != null ? tanks[0].getFluid().getFluid() : ModForgeFluids.none;
			this.fluids[1] = tanks[1].getFluid() != null ? tanks[0].getFluid().getFluid() : ModForgeFluids.none;
			this.fluids[2] = outputFluid != null ? outputFluid : ModForgeFluids.none;
		} else {
			for (int i = 0; i < 3; i++) this.tanksNew[i].readFromNBT(nbt, i + "");
			if(nbt.hasKey("f")){
				nbt.removeTag("f");
				nbt.removeTag("tanks");
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setInteger("progress", progress);
		nbt.setInteger("processTime", processTime);
		nbt.setInteger("recipe", recipeIndex);
		if(!converted){
			if(outputFluid != null){
				nbt.setString("f", outputFluid.getName());
			} else {
				if(tanks[2].getFluid() != null){
					nbt.setString("f", tanks[2].getFluid().getFluid().getName());
				} else {
					nbt.setString("f", "None");
				}
			}
			nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		} else {
			for (int i = 0; i < 3; i++) this.tanksNew[i].writeToNBT(nbt, i + "");
		}
		return super.writeToNBT(nbt);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMixer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMixer(player.inventory, this);
	}
	
	AxisAlignedBB aabb;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(aabb != null)
			return aabb;
		
		aabb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
		return aabb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanksNew;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanksNew[2]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanksNew[0], tanksNew[1]};
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
