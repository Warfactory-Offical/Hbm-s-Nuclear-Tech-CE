package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineSchrabidiumTransmutator;
import com.hbm.inventory.gui.GUIMachineSchrabidiumTransmutator;
import com.hbm.inventory.recipes.NuclearTransmutationRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCapacitor;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineSchrabidiumTransmutator extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider {

	public long power = 0;
	public int process = 0;
	public static final long maxPower = 50000000;
	public static final int processSpeed = 600;

	private AudioWrapper audio;
	
	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 3, 2 };

	public TileEntityMachineSchrabidiumTransmutator() {
		super(4);
	}
	
	@Override
	public String getDefaultName() {
		return "container.machine_schrabidium_transmutator";
	}
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		switch (i) {
		case 0:
			if(NuclearTransmutationRecipes.getOutput(stack) != null)
				return true;
			break;
		case 2:
			if(stack.getItem() == ModItems.redcoil_capacitor || stack.getItem() == ModItems.euphemium_capacitor)
				return true;
			break;
		case 3:
			if(Library.isItemCanStoreEnergy(stack))
				return true;
			break;
		}
		return false;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		int i = e.ordinal();
		return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
	}
	
	@Override
	public boolean canExtractItem(int i, ItemStack stack, int amount) {
		if(i == 2 && stack.getItem() == ModItems.redcoil_capacitor && ItemCapacitor.getDura(stack) <= 0) {
			return true;
		}
		if(i == 1) {
			return true;
		}

		if(i == 3) {
			return Library.isItemEmptyEnergy(stack);
		}

		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		power = compound.getLong("power");
		process = compound.getInteger("process");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setInteger("process", process);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			power = Library.chargeTEFromItems(inventory, 3, power, maxPower);

			if(canProcess()) {
				process();
			} else {
				process = 0;
			}

			networkPackNT(50);
			detectAndSendChanges();
		} else {

			if(process > 0) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.updateVolume(getVolume(1F));
			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.tauChargeLoop, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F, 1.0F);
	}
	
	@Override
	public void onChunkUnload() {
		if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(process);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.process = buf.readInt();
	}
	
	private long detectPower;
	
	private void detectAndSendChanges(){
		boolean mark = false;
		if(detectPower != power){
			mark = true;
			detectPower = power;
		}
		if(mark)
			markDirty();
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (process * i) / processSpeed;
	}

	public boolean hasCoil(){
		if(inventory.getStackInSlot(2).getItem() == ModItems.redcoil_capacitor && ItemCapacitor.getDura(inventory.getStackInSlot(2)) > 0)
			return true;
		if(inventory.getStackInSlot(2).getItem() == ModItems.euphemium_capacitor)
			return true;
		return false;
	}

	public boolean canProcess() {
		if(!hasCoil())
			return false;
		if(inventory.getStackInSlot(0) == null || inventory.getStackInSlot(0).isEmpty())
			return false;
		long recipePower = NuclearTransmutationRecipes.getEnergy(inventory.getStackInSlot(0));

		if(recipePower < 0)
			return false;

		if(recipePower > power)
			return false;

		ItemStack outputItem = NuclearTransmutationRecipes.getOutput(inventory.getStackInSlot(0));
		if(inventory.getStackInSlot(1) == null || inventory.getStackInSlot(1).isEmpty() || (inventory.getStackInSlot(1).getItem() == outputItem.getItem()
			&& inventory.getStackInSlot(1).getCount() < inventory.getStackInSlot(1).getMaxStackSize())) {
			return true;
		}
		return false;
	}

	public boolean isProcessing() {
		return process > 0;
	}

	public void process() {
		process++;

		if(process >= processSpeed) {
			
			power -= NuclearTransmutationRecipes.getEnergy(inventory.getStackInSlot(0));
			if(power < 0)
				power = 0;
			process = 0;
			
			if(inventory.getStackInSlot(1).isEmpty()) {
				inventory.setStackInSlot(1, NuclearTransmutationRecipes.getOutput(inventory.getStackInSlot(0)).copy());
			} else {
				inventory.getStackInSlot(1).grow(1);
			}
			if(!inventory.getStackInSlot(2).isEmpty() && inventory.getStackInSlot(2).getItem() == ModItems.redcoil_capacitor) {
				ItemCapacitor.setDura(inventory.getStackInSlot(2), ItemCapacitor.getDura(inventory.getStackInSlot(2)) - 1);
			}

			inventory.getStackInSlot(0).shrink(1);
			if(inventory.getStackInSlot(0).getCount() == 0)
				inventory.setStackInSlot(0, ItemStack.EMPTY);

			this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.BLOCKS, 10000.0F, 0.8F + world.rand.nextFloat() * 0.2F);
		}
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
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSchrabidiumTransmutator(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSchrabidiumTransmutator(player.inventory, this);
	}
}
