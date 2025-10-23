package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.List;

public class ItemCassette extends Item {

	public enum TrackType {

		NULL(" ", null, SoundType.SOUND, 0, 0), HATCH("Hatch Siren", HBMSoundHandler.alarmHatch, SoundType.LOOP, 3358839, 250), ATUOPILOT("Autopilot Disconnected", HBMSoundHandler.alarmAutopilot, SoundType.LOOP, 11908533, 50), AMS_SIREN("AMS Siren", HBMSoundHandler.alarmAMSSiren, SoundType.LOOP, 15055698, 50), BLAST_DOOR("Blast Door Alarm", HBMSoundHandler.alarmBlastDoor, SoundType.LOOP, 11665408, 50), APC_LOOP("APC Siren", HBMSoundHandler.alarmAPCLoop, SoundType.LOOP, 3565216, 50), KLAXON("Klaxon", HBMSoundHandler.alarmKlaxon, SoundType.LOOP, 8421504, 50), KLAXON_A("Vault Door Alarm", HBMSoundHandler.alarmFoKlaxonA, SoundType.LOOP, 0x8c810b, 50), KLAXON_B("Security Alert", HBMSoundHandler.alarmFoKlaxonB, SoundType.LOOP, 0x76818e, 50), SIREN("Standard Siren", HBMSoundHandler.alarmRegular, SoundType.LOOP, 6684672, 100), CLASSIC("Classic Siren", HBMSoundHandler.alarmClassic, SoundType.LOOP, 0xc0cfe8, 100), BANK_ALARM("Bank Alarm", HBMSoundHandler.alarmBank, SoundType.LOOP, 3572962,
				100), BEEP_SIREN("Beep Siren", HBMSoundHandler.alarmBeep, SoundType.LOOP, 13882323, 100), CONTAINER_ALARM("Container Alarm", HBMSoundHandler.alarmContainer, SoundType.LOOP, 14727839, 100), SWEEP_SIREN("Sweep Siren", HBMSoundHandler.alarmSweep, SoundType.LOOP, 15592026, 500), STRIDER_SIREN("Missile Silo Siren", HBMSoundHandler.alarmStrider, SoundType.LOOP, 11250586, 500), AIR_RAID("Air Raid Siren", HBMSoundHandler.alarmAirRaid, SoundType.LOOP, 0xDF3795, 500), NOSTROMO_SIREN("Nostromo Self Destruct", HBMSoundHandler.alarmNostromo, SoundType.LOOP, 0x5dd800, 100), EAS_ALARM("EAS Alarm Screech", HBMSoundHandler.alarmEas, SoundType.LOOP, 0xb3a8c1, 50), APC_PASS("APC Pass", HBMSoundHandler.alarmAPCPass, SoundType.PASS, 3422163, 50), RAZORTRAIN("Razortrain Horn", HBMSoundHandler.alarmRazorTrain, SoundType.SOUND, 7819501, 250);

		// Name of the track shown in GUI
		private String title;
		// Location of the sound
		private SoundEvent location;
		// Sound type, whether the sound should be repeated or not
		private SoundType type;
		// Color of the cassette
		private int color;
		// Range where the sound can be heard
		private int volume;

		private TrackType(String name, SoundEvent loc, SoundType sound, int msa, int intensity) {
			title = name;
			location = loc;
			type = sound;
			color = msa;
			volume = intensity;
		}

		public String getTrackTitle() {
			return title;
		}

		public SoundEvent getSoundLocation() {
			return location;
		}

		public SoundType getType() {
			return type;
		}

		public int getColor() {
			return color;
		}

		public int getVolume() {
			return volume;
		}

		public static TrackType getEnum(int i) {
			if(i < TrackType.values().length)
				return TrackType.values()[i];
			else
				return TrackType.NULL;
		}
	};

	public enum SoundType {
		LOOP, PASS, SOUND;
	};

	public ItemCassette(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
			for(int i = 1; i < TrackType.values().length; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(!(stack.getItem() instanceof ItemCassette))
			return;

		tooltip.add("[CREATED USING TEMPLATE FOLDER]");
		tooltip.add("");

		tooltip.add("Siren sound cassette:");
		tooltip.add("   Name: " + TrackType.getEnum(stack.getItemDamage()).getTrackTitle());
		tooltip.add("   Type: " + TrackType.getEnum(stack.getItemDamage()).getType().name());
		tooltip.add("   Volume: " + TrackType.getEnum(stack.getItemDamage()).getVolume());
	}

	public static TrackType getType(ItemStack stack) {
		if(stack != null && stack.getItem() instanceof ItemCassette)
			return TrackType.getEnum(stack.getItemDamage());
		else
			return TrackType.NULL;
	}

}
