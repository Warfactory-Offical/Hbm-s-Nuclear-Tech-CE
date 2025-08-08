package com.hbm.uninos.networkproviders;

import com.hbm.uninos.INetworkProvider;

import com.hbm.api.energymk2.PowerNetMK2;

public class PowerNetProvider implements INetworkProvider<PowerNetMK2> {
    @Override
    public PowerNetMK2 provideNetwork() {
        return new PowerNetMK2();
    }
}
