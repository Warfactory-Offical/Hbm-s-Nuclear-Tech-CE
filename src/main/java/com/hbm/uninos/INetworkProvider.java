package com.hbm.uninos;

import java.util.function.Supplier;

/**
 * Each instance of a network provider is a valid "type" of node in UNINOS
 * @author hbm
 */
@FunctionalInterface
public interface INetworkProvider<T extends NodeNet<?, ?, ?, T>> extends Supplier<T> {
}
