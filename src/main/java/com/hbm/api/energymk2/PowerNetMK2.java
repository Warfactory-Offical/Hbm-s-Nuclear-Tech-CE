package com.hbm.api.energymk2;

import com.hbm.uninos.NodeNet;
import com.hbm.util.Tuple;
import com.hbm.util.Tuple.Pair;

import java.util.*;

/**
 * Technically MK3 since it's now UNINOS compatible, although UNINOS was build out of 95% nodespace code
 *
 * @author hbm
 */
public class PowerNetMK2 extends NodeNet<IEnergyReceiverMK2, IEnergyProviderMK2, Nodespace.PowerNode> {

    public long energyTracker = 0L;

    protected static int timeout = 3_000;

    @Override public void resetTrackers() { this.energyTracker = 0; }

    @Override
    public void update() {

        if(providerEntries.isEmpty()) return;
        if(receiverEntries.isEmpty()) return;

        long timestamp = System.currentTimeMillis();

        List<Pair<IEnergyProviderMK2, Long>> providers = new ArrayList<>();
        long powerAvailable = 0;

        // sum up available power
        Iterator<Map.Entry<IEnergyProviderMK2, Long>> provIt = providerEntries.entrySet().iterator();
        while(provIt.hasNext()) {
            Map.Entry<IEnergyProviderMK2, Long> entry = provIt.next();
            if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { provIt.remove(); continue; }
            long src = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
            if(src > 0) {
                providers.add(new Pair<>(entry.getKey(), src));
                powerAvailable += src;
            }
        }

        // sum up total demand, categorized by priority
        List<Pair<IEnergyReceiverMK2, Long>>[] receivers = new ArrayList[IEnergyReceiverMK2.ConnectionPriority.values().length];
        for(int i = 0; i < receivers.length; i++) receivers[i] = new ArrayList<>();
        long[] demand = new long[IEnergyReceiverMK2.ConnectionPriority.values().length];
        long totalDemand = 0;

        Iterator<Map.Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();

        while(recIt.hasNext()) {
            Map.Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
            if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { recIt.remove(); continue; }
            long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
            if(rec > 0) {
                int p = entry.getKey().getPriority().ordinal();
                receivers[p].add(new Pair<>(entry.getKey(), rec));
                demand[p] += rec;
                totalDemand += rec;
            }
        }

        long toTransfer = Math.min(powerAvailable, totalDemand);
        long energyUsed = 0;

        // add power to receivers, ordered by priority
        for(int i = IEnergyReceiverMK2.ConnectionPriority.values().length - 1; i >= 0; i--) {
            List<Pair<IEnergyReceiverMK2, Long>> list = receivers[i];
            long priorityDemand = demand[i];

            for(Pair<IEnergyReceiverMK2, Long> entry : list) {
                double weight = (double) entry.getValue() / (double) (priorityDemand);
                long toSend = (long) Math.max(toTransfer * weight, 0D);
                energyUsed += (toSend - entry.getKey().transferPower(toSend, false)); //leftovers are subtracted from the intended amount to use up
            }

            toTransfer -= energyUsed;
        }

        this.energyTracker += energyUsed;
        long leftover = energyUsed;

        // remove power from providers
        for(Pair<IEnergyProviderMK2, Long> entry : providers) {
            double weight = (double) entry.getValue() / (double) powerAvailable;
            long toUse = (long) Math.max(energyUsed * weight, 0D);
            entry.getKey().usePower(toUse);
            leftover -= toUse;
        }

        // rounding error compensation, detects surplus that hasn't been used and removes it from random providers
        int iterationsLeft = 100; // whiles without emergency brakes are a bad idea
        while(iterationsLeft > 0 && leftover > 0 && !providers.isEmpty()) {
            iterationsLeft--;

            Pair<IEnergyProviderMK2, Long> selected = providers.get(rand.nextInt(providers.size()));
            IEnergyProviderMK2 scapegoat = selected.getKey();

            long toUse = Math.min(leftover, scapegoat.getPower());
            scapegoat.usePower(toUse);
            leftover -= toUse;
        }
    }

    public long sendPowerDiode(long power, boolean simulate) {
        if (receiverEntries.isEmpty()) return power;

        long timestamp = System.currentTimeMillis();

        List<Pair<IEnergyReceiverMK2, Long>>[] receivers = new ArrayList[IEnergyReceiverMK2.ConnectionPriority.values().length];
        for(int i = 0; i < receivers.length; i++) receivers[i] = new ArrayList<>();
        long[] demand = new long[IEnergyReceiverMK2.ConnectionPriority.values().length];
        long totalDemand = 0;

        Iterator<Map.Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();

        while(recIt.hasNext()) {
            Map.Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
            if(timestamp - entry.getValue() > timeout) { recIt.remove(); continue; }
            long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
            int p = entry.getKey().getPriority().ordinal();
            receivers[p].add(new Pair<>(entry.getKey(), rec));
            demand[p] += rec;
            totalDemand += rec;
        }

        long toTransfer = Math.min(power, totalDemand);
        long energyUsed = 0;

        for(int i = IEnergyReceiverMK2.ConnectionPriority.values().length - 1; i >= 0; i--) {
            List<Pair<IEnergyReceiverMK2, Long>> list = receivers[i];
            long priorityDemand = demand[i];

            for(Pair<IEnergyReceiverMK2, Long> entry : list) {
                double weight = (double) entry.getValue() / (double) (priorityDemand);
                long toSend = (long) Math.max(toTransfer * weight, 0D);
                energyUsed += (toSend - entry.getKey().transferPower(toSend, simulate)); //leftovers are subtracted from the intended amount to use up
            }

            toTransfer -= energyUsed;
        }

        if (!simulate) this.energyTracker += energyUsed;

        return power - energyUsed;
    }

    public long extractPowerDiode(long power, boolean simulate) {
        if (providerEntries.isEmpty() || power <= 0) return 0;

        long timestamp = System.currentTimeMillis();

        List<Tuple.Pair<IEnergyProviderMK2, Long>> providers = new ArrayList<>();
        long supply = 0;

        Iterator<Map.Entry<IEnergyProviderMK2, Long>> provIt = providerEntries.entrySet().iterator();

        while (provIt.hasNext()) {
            Map.Entry<IEnergyProviderMK2, Long> entry = provIt.next();
            if (timestamp - entry.getValue() > timeout) {
                if (!simulate) provIt.remove();
                continue;
            }
            long prov = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
            if (prov > 0) {
                providers.add(new Tuple.Pair<>(entry.getKey(), prov));
                supply += prov;
            }
        }

        if (supply <= 0) return 0;

        long powerToExtract = Math.min(power, supply);
        long totalExtracted = 0;

        for (Tuple.Pair<IEnergyProviderMK2, Long> entry : providers) {
            double weight = (double) entry.getValue() / (double) supply;
            long toExtract = (long) Math.ceil(powerToExtract * weight);

            if (toExtract > 0) {
                long actualExtract = Math.min(toExtract, entry.getKey().getPower());
                if (!simulate) entry.getKey().usePower(actualExtract);
                totalExtracted += actualExtract;
            }
        }

        if (!simulate) {
            this.energyTracker += totalExtracted;
        }

        return totalExtracted;
    }
}