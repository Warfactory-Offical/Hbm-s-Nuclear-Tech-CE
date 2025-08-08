package com.hbm.uninos;

/**
 * Each instance of a network provider is a valid "type" of node in UNINOS
 * @author hbm
 */
@SuppressWarnings("rawtypes") //stfu intellij
public interface INetworkProvider<T extends NodeNet> {

    T provideNetwork();
}
