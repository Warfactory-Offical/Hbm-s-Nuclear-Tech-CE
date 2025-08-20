package com.hbm.capability;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.armor.ItemModShield;
import com.hbm.main.MainRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

// TODO: Port stuff from 1.7, this is very outdated
@SuppressWarnings("DataFlowIssue")
public class HbmCapability {

	public static final int dashCooldownLength = 5;
	private static final int plinkCooldownLength = 10;

	/**
	 * Only add getter/setter to this interface. Do not add any additional default methods unless you really have to.
	 * This work differently from upstream! If you need an example, compare the shield mechanism here with the one from 1.7.
	 */
	public interface IHBMData {
		float shieldCap = 100;

		boolean getKeyPressed(EnumKeybind key);
		void setKeyPressed(EnumKeybind key, boolean pressed);
		boolean getEnableBackpack();
		boolean getEnableHUD();
		@Deprecated
		@ApiStatus.ScheduledForRemoval(inVersion = "0.4")
		boolean getOnLadder();
		float getShield();
		float getMaxShield();
		int getLastDamage();
		int getDashCooldown();
		int getStamina();
		int getDashCount();
		int getPlinkCooldown();
		void setEnableBackpack(boolean b);
		void setEnableHUD(boolean b);
		@Deprecated
		@ApiStatus.ScheduledForRemoval(inVersion = "0.4")
		void setOnLadder(boolean b);
		void setShield(float f);
		void setMaxShield(float f);
		void setLastDamage(int i);
		void setDashCooldown(int cooldown);
		void setStamina(int stamina);
		void setDashCount(int count);
		void setPlinkCooldown(int cooldown);
		default float getEffectiveMaxShield(EntityPlayer player){
			float max = this.getMaxShield();
			if(!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()) {
				ItemStack[] mods = ArmorModHandler.pryMods(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
				if(mods[ArmorModHandler.kevlar] != null && mods[ArmorModHandler.kevlar].getItem() instanceof ItemModShield mod) {
					max += mod.shield;
				}
			}
			return max;
		}
        default boolean isJetpackActive() {
			return getEnableBackpack() && getKeyPressed(EnumKeybind.JETPACK);
		}
		default void serialize(ByteBuf buf) {
//			buf.writeBoolean(this.hasReceivedBook);
			buf.writeFloat(this.getShield());
			buf.writeFloat(this.getMaxShield());
			buf.writeBoolean(this.getEnableBackpack());
			buf.writeBoolean(this.getEnableHUD());
//			buf.writeInt(this.reputation);
			buf.writeBoolean(this.getOnLadder());
//			buf.writeBoolean(this.enableMagnet);
		}
		default void deserialize(ByteBuf buf) {
			if(buf.readableBytes() > 0) {
//				this.hasReceivedBook = buf.readBoolean();
				this.setShield(buf.readFloat());
				this.setMaxShield(buf.readFloat());
				this.setEnableBackpack(buf.readBoolean());
				this.setEnableHUD(buf.readBoolean());
//				this.reputation = buf.readInt();
				this.setOnLadder(buf.readBoolean());
//				this.enableMagnet = buf.readBoolean();
			}
		}
	}
	
	public static class HBMData implements IHBMData {

		public static final Callable<IHBMData> FACTORY = HBMData::new;
		
		private final boolean[] keysPressed = new boolean[EnumKeybind.values().length];
		
		public boolean enableBackpack = true;
		public boolean enableHUD = true;
		public boolean isOnLadder = false;
		public boolean dashActivated = true;

		public int dashCooldown = 0;

		public int totalDashCount = 0;
		public int stamina = 0;
		public int plinkCooldown = 0;

		public float shield = 0;
		public float maxShield = 0;
		/**
		 * mlbv: figure out what the fuck this is, there is a {@link EntityLivingBase#lastDamage} already
		 * so what is its purpose?
		 */
		public int lastDamage = 0;
		
		@Override
		public boolean getKeyPressed(EnumKeybind key) {
			return keysPressed[key.ordinal()];
		}

		@Override
		public void setKeyPressed(EnumKeybind key, boolean pressed) {
			if(!getKeyPressed(key) && pressed) {
				
				if(key == EnumKeybind.TOGGLE_JETPACK) {
					this.enableBackpack = !this.enableBackpack;
					
					if(this.enableBackpack)
						MainRegistry.proxy.displayTooltip(TextFormatting.GREEN + "Jetpack ON");
					else
						MainRegistry.proxy.displayTooltip(TextFormatting.RED + "Jetpack OFF");
				}
				if(key == EnumKeybind.TOGGLE_HEAD) {
					this.enableHUD = !this.enableHUD;
					
					if(this.enableHUD)
						MainRegistry.proxy.displayTooltip(TextFormatting.GREEN + "HUD ON");
					else
						MainRegistry.proxy.displayTooltip(TextFormatting.RED + "HUD OFF");
				}
			}
			keysPressed[key.ordinal()] = pressed;
		}
		
		@Override
		public boolean getEnableBackpack(){
			return enableBackpack;
		}

		@Override
		public boolean getEnableHUD(){
			return enableHUD;
		}

		@Override
		public void setEnableBackpack(boolean b){
			enableBackpack = b;
		}

		@Override
		public void setEnableHUD(boolean b){
			enableHUD = b;
		}

		@Override
		public boolean getOnLadder() {return isOnLadder;}

		@Override
		public float getShield() {
			return shield;
		}

		@Override
		public float getMaxShield() {
			return maxShield;
		}

		@Override
		public int getLastDamage() {
			return lastDamage;
		}

		@Override
		public void setOnLadder(boolean b){isOnLadder = b;}

		@Override
		public void setDashCooldown(int cooldown) {
			dashCooldown = cooldown;
        }

		@Override
		public int getDashCooldown() {
			return dashCooldown;
		}

		@Override
		public void setStamina(int stamina) {
			this.stamina = stamina;
        }

		@Override
		public int getStamina() {
			return this.stamina;
		}

		@Override
		public void setDashCount(int count) {
			this.totalDashCount = count;
        }

		@Override
		public int getDashCount() {
			return this.totalDashCount;
		}

		@Override
		public void setPlinkCooldown(int cooldown) {
			this.plinkCooldown = cooldown;
        }

		@Override
		public int getPlinkCooldown() {
			return this.plinkCooldown;
		}

		@Override
		public void setShield(float f) {
			shield = f;
		}

		@Override
		public void setMaxShield(float f) {
			maxShield = f;
		}

		@Override
		public void setLastDamage(int i) {
			lastDamage = i;
		}
	}
	
	public static class HBMDataStorage implements IStorage<IHBMData>{

		@Override
		public NBTBase writeNBT(Capability<IHBMData> capability, IHBMData instance, EnumFacing side) {
			NBTTagCompound tag = new NBTTagCompound();
			for(EnumKeybind key : EnumKeybind.values()){
				tag.setBoolean(key.name(), instance.getKeyPressed(key));
			}
			tag.setBoolean("enableBackpack", instance.getEnableBackpack());
			tag.setBoolean("enableHUD", instance.getEnableHUD());
			tag.setBoolean("isOnLadder", instance.getOnLadder());
			tag.setFloat("shield", instance.getShield());
			tag.setFloat("maxShield", instance.getMaxShield());
			return tag;
		}

		@Override
		public void readNBT(Capability<IHBMData> capability, IHBMData instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound tag){
                for(EnumKeybind key : EnumKeybind.values()){
					instance.setKeyPressed(key, tag.getBoolean(key.name()));
				}
				instance.setEnableBackpack(tag.getBoolean("enableBackpack"));
				instance.setEnableHUD(tag.getBoolean("enableHUD"));
				instance.setOnLadder(tag.getBoolean("isOnLadder"));
				instance.setShield(tag.getFloat("shield"));
				instance.setMaxShield(tag.getFloat("maxShield"));
			}
		}
		
	}
	
	public static class HBMDataProvider implements ICapabilitySerializable<NBTBase> {

		public static final IHBMData DUMMY = new IHBMData(){

			@Override
			public boolean getKeyPressed(EnumKeybind key) {
				return false;
			}

			@Override
			public void setKeyPressed(EnumKeybind key, boolean pressed) {
			}

			@Override
			public boolean getEnableBackpack(){
				return false;
			}

			@Override
			public boolean getEnableHUD(){
				return false;
			}

			@Override
			public void setEnableBackpack(boolean b){
			}

			@Override
			public void setEnableHUD(boolean b){
			}

			@Override
			public boolean getOnLadder() {
				return false;
			}

			@Override
			public float getShield() {
				return 0;
			}

			@Override
			public float getMaxShield() {
				return 0;
			}

			@Override
			public int getLastDamage() {
				return 0;
			}

			@Override
			public int getDashCooldown() {
				return 0;
			}

			@Override
			public int getStamina() {
				return 0;
			}

			@Override
			public int getDashCount() {
				return 0;
			}

			@Override
			public int getPlinkCooldown() {
				return 0;
			}

			@Override
			public void setOnLadder(boolean b){
			}

			@Override
			public void setShield(float f) {
			}

			@Override
			public void setMaxShield(float f) {
			}

			@Override
			public void setLastDamage(int i) {
			}

			@Override
			public void setDashCooldown(int cooldown) {
			}

			@Override
			public void setStamina(int stamina) {
			}

			@Override
			public void setDashCount(int count) {
			}

			@Override
			public void setPlinkCooldown(int cooldown) {
			}
		};
		
		@CapabilityInject(IHBMData.class)
		public static final Capability<IHBMData> HBM_CAP = null;

		private final IHBMData instance = HBM_CAP.getDefaultInstance();

		@Override
		public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
			return capability == HBM_CAP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HBM_CAP ? HBM_CAP.cast(this.instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return HBM_CAP.getStorage().writeNBT(HBM_CAP, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			HBM_CAP.getStorage().readNBT(HBM_CAP, instance, null, nbt);
		}
		
	}
	
	public static IHBMData getData(Entity e){
		if(e.hasCapability(HBMDataProvider.HBM_CAP, null))
			return e.getCapability(HBMDataProvider.HBM_CAP, null);
		return HBMDataProvider.DUMMY;
	}

	public static void plink(@NotNull EntityPlayer player, @NotNull SoundEvent sound, float volume, float pitch) {
		HbmCapability.IHBMData props = HbmCapability.getData(player);
		if(props.getPlinkCooldown() <= 0) {
			player.world.playSound(player, player.getPosition(), sound, SoundCategory.PLAYERS, volume, pitch);
			props.setPlinkCooldown(plinkCooldownLength);
		}
	}
}
