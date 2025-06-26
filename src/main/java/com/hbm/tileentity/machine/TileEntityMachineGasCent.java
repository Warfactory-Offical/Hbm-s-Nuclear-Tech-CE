package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.GasCentrifugeRecipes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityMachineGasCent extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiver, IFFtoNTMF {

	
	public long power;
	public int progress;
	public boolean isProgressing;
	public static final int maxPower = 100000;
	public static final int processingSpeed = 150;
	public boolean needsUpdate = false;
	
	public FluidTankNTM tankNew;
	public FluidTank tank;
	public Fluid oldFluid;
	public PseudoFluidTank inputTank;
	public PseudoFluidTank outputTank;

	private static boolean converted = false;
	
	public TileEntityMachineGasCent() {
		super(9);
		tank = new FluidTank(8000);
		tankNew = new FluidTankNTM(Fluids.UF6, 2000);
		inputTank = new PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType.NUF6, 8000);
		outputTank = new PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType.LEUF6, 8000);

		converted = true;
	}
	
	public String getName() {
		return "container.gasCentrifuge";
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		power = nbt.getLong("power");
		progress = nbt.getShort("progress");
		if (!converted) {
			tank.readFromNBT(nbt);
			oldFluid = tank.getFluid() != null ? tank.getFluid().getFluid() : ModForgeFluids.none;
		} else tankNew.readFromNBT(nbt, "tank");
		inputTank.readFromNBT(nbt, "inputTank");
		outputTank.readFromNBT(nbt, "outputTank");
		if(nbt.hasKey("inventory"))
			inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
		
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setShort("progress", (short) progress);
		if(!converted) tank.writeToNBT(nbt); else tankNew.writeToNBT(nbt, "tank");
		inputTank.writeToNBT(nbt, "inputTank");
		outputTank.writeToNBT(nbt, "outputTank");
		nbt.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(nbt);
	}
	
	public int getCentrifugeProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}
	
	public long getPowerRemainingScaled(int i) {
		return (power * i) / maxPower;
	}

	private boolean canEnrich() {
		if(power > 0 && this.inputTank.getFill() >= inputTank.getTankType().getFluidConsumed() && this.outputTank.getFill() + this.inputTank.getTankType().getFluidProduced() <= outputTank.getMaxFill()) {

			ItemStack[] list = inputTank.getTankType().getOutput();

			if(list == null)
				return false;

			if(list.length < 1)
				return false;

			if(InventoryUtil.doesArrayHaveSpace(inventory, 0, 3, list))
				return true;
		}

		return false;
	}

	private void enrich() {
		ItemStack[] output = inputTank.getTankType().getOutput();

		this.progress = 0;
		inputTank.setFill(inputTank.getFill() - inputTank.getTankType().getFluidConsumed());
		outputTank.setFill(outputTank.getFill() + inputTank.getTankType().getFluidProduced());

		for(byte i = 0; i < output.length; i++)
			InventoryUtil.tryAddItemToInventory(inventory, 0, 3, output[i].copy()); //reference types almost got me again
	}

	private void attemptConversion() {
		if(inputTank.getFill() < inputTank.getMaxFill() && tankNew.getFill() > 0) {
			int fill = Math.min(inputTank.getMaxFill() - inputTank.getFill(), tankNew.getFill());

			tankNew.setFill(tankNew.getFill() - fill);
			inputTank.setFill(inputTank.getFill() + fill);
		}
	}

	private boolean attemptTransfer(TileEntity te) {
		if(te instanceof TileEntityMachineGasCent) {
			TileEntityMachineGasCent cent = (TileEntityMachineGasCent) te;

			if(cent.tankNew.getFill() == 0 && cent.tankNew.getTankType() == tankNew.getTankType()) {
				if(cent.inputTank.getTankType() != outputTank.getTankType() && outputTank.getTankType() != GasCentrifugeRecipes.PseudoFluidType.NONE) {
					cent.inputTank.setTankType(outputTank.getTankType());
					cent.outputTank.setTankType(outputTank.getTankType().getOutputType());
				}

				//God, why did I forget about the entirety of the fucking math library?
				if(cent.inputTank.getFill() < cent.inputTank.getMaxFill() && outputTank.getFill() > 0) {
					int fill = Math.min(cent.inputTank.getMaxFill() - cent.inputTank.getFill(), outputTank.getFill());

					outputTank.setFill(outputTank.getFill() - fill);
					cent.inputTank.setFill(cent.inputTank.getFill() + fill);
				}

				return true;
			}
		}

		return false;
	}
	
	@Override
	public void update() {
		
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluid(oldFluid, tank, tankNew);
				converted = true;
			}
			updateConnections();

			power = Library.chargeTEFromItems(inventory, 4, power, maxPower);
			setTankType(5);

			if (GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				attemptConversion();
			}

			if (canEnrich()) {

				isProgressing = true;
				this.progress++;
				this.power -= 200;

				if (this.power < 0) {
					power = 0;
					this.progress = 0;
				}

				if (progress >= processingSpeed)
					enrich();

			} else {
				isProgressing = false;
				this.progress = 0;
			}

			if (world.getTotalWorldTime() % 10 == 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				TileEntity te = world.getTileEntity(pos.add(-dir.offsetX, 0, -dir.offsetZ));

				//*AT THE MOMENT*, there's not really any need for a dedicated method for this. Yet.
				if (!attemptTransfer(te) && this.inputTank.getTankType() == GasCentrifugeRecipes.PseudoFluidType.LEUF6) {
					ItemStack[] converted = new ItemStack[]{new ItemStack(ModItems.nugget_uranium_fuel, 6), new ItemStack(ModItems.fluorite)};

					if (this.outputTank.getFill() >= 600 && InventoryUtil.doesArrayHaveSpace(inventory, 0, 3, converted)) {
						this.outputTank.setFill(this.outputTank.getFill() - 600);
						for (ItemStack stack : converted)
							InventoryUtil.tryAddItemToInventory(inventory, 0, 3, stack);
					}
				}
			}

			this.networkPackNT(50);
		}

		
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeBoolean(isProgressing);
		//pseudofluids can be refactored another day
		buf.writeInt(inputTank.getFill());
		buf.writeInt(outputTank.getFill());
		BufferUtil.writeString(buf, inputTank.getTankType().name); //cough cough
		BufferUtil.writeString(buf, outputTank.getTankType().name);

		tankNew.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		isProgressing = buf.readBoolean();

		inputTank.setFill(buf.readInt());
		outputTank.setFill(buf.readInt());
		inputTank.setTankType(GasCentrifugeRecipes.PseudoFluidType.types.get(BufferUtil.readString(buf)));
		outputTank.setTankType(GasCentrifugeRecipes.PseudoFluidType.types.get(BufferUtil.readString(buf)));

		tankNew.deserialize(buf);
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());

			if(GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				this.trySubscribe(tankNew.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
		}
	}

	private DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
		};
	}

	public void setTankType(int in) {

		if(inventory.getStackInSlot(in) != ItemStack.EMPTY && inventory.getStackInSlot(in).getItem() instanceof IItemFluidIdentifier) {
			IItemFluidIdentifier id = (IItemFluidIdentifier) inventory.getStackInSlot(in).getItem();
			FluidType newType = id.getType(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(in));

			if(tankNew.getTankType() != newType) {
				GasCentrifugeRecipes.PseudoFluidType pseudo = GasCentrifugeRecipes.fluidConversions.get(newType);

				if(pseudo != null) {
					inputTank.setTankType(pseudo);
					outputTank.setTankType(pseudo.getOutputType());
					tankNew.setTankType(newType);
				}
			}

		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot > 3;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return new int[]{0, 3, 4, 5, 6, 7, 8};
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 4, 1));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public void setPower(long i) {
		power = i;
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
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tankNew};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tankNew};
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
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), null)
			);
		}
		return super.getCapability(capability, facing);
	}

	public class PseudoFluidTank {
		GasCentrifugeRecipes.PseudoFluidType type;
		int fluid;
		int maxFluid;

		public PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType type, int maxFluid) {
			this.type = type;
			this.maxFluid = maxFluid;
		}

		public void setFill(int i) {
			fluid = i;
		}

		public void setTankType(GasCentrifugeRecipes.PseudoFluidType type) {

			if(this.type.equals(type))
				return;

			if(type == null)
				this.type = GasCentrifugeRecipes.PseudoFluidType.NONE;
			else
				this.type = type;

			this.setFill(0);
		}

		public GasCentrifugeRecipes.PseudoFluidType getTankType() {
			return type;
		}

		public int getFill() {
			return fluid;
		}

		public int getMaxFill() {
			return maxFluid;
		}

		//Called by TE to save fillstate
		public void writeToNBT(NBTTagCompound nbt, String s) {
			nbt.setInteger(s, fluid);
			nbt.setInteger(s + "_max", maxFluid);
			nbt.setString(s + "_type", type.name);
		}

		//Called by TE to load fillstate
		public void readFromNBT(NBTTagCompound nbt, String s) {
			fluid = nbt.getInteger(s);
			int max = nbt.getInteger(s + "_max");
			if(max > 0) maxFluid = nbt.getInteger(s + "_max");
			type = GasCentrifugeRecipes.PseudoFluidType.types.get(nbt.getString(s + "_type"));
			if(type == null) type = GasCentrifugeRecipes.PseudoFluidType.NONE;
		}

		/*        ______      ______
		 *       _I____I_    _I____I_
		 *      /      \\\  /      \\\
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{    || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{	   || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{	   || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *    _|_______||_||_______||_|_
		 *   |                          |
		 *   |                          |
		 *   |       |==========|       |
		 *   |       |NESTED    |       |
		 *   |       |IF  (:    |       |
		 *   |       |STATEMENTS|       |
		 *   |       |==========|       |
		 *   |                          |
		 *   |                          |
		 *   ----------------------------
		 *
		 *that's sick af..
		 *
		 */
	}
}
