package com.hbm.capability;

import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.config.GeneralConfig;
import net.minecraftforge.energy.IEnergyStorage;

public class NTMCableEnergyCapabilityWrapper implements IEnergyStorage {

    private final PowerNetMK2 net;

    public NTMCableEnergyCapabilityWrapper(PowerNetMK2 net) {
        this.net = net;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;

        long heToOffer = (long) Math.floor(maxReceive / GeneralConfig.conversionRateHeToRF);
        if (heToOffer <= 0) return 0;

        long leftoverHE = net.sendPowerDiode(heToOffer, simulate);
        long acceptedHE = heToOffer - leftoverHE;

        return (int) Math.min(maxReceive, Math.min(Integer.MAX_VALUE, Math.round(acceptedHE * GeneralConfig.conversionRateHeToRF)));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;

        long heToExtract = (long) Math.floor(maxExtract / GeneralConfig.conversionRateHeToRF);
        if (heToExtract <= 0) return 0;

        long extractedHE = net.extractPowerDiode(heToExtract, simulate);

        return (int) Math.min(maxExtract, Math.min(Integer.MAX_VALUE, Math.round(extractedHE * GeneralConfig.conversionRateHeToRF)));
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return !net.providerEntries.isEmpty();
    }

    @Override
    public boolean canReceive() {
        return !net.receiverEntries.isEmpty();
    }
}
