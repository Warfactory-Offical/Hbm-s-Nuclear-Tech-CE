package com.llib.technical;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Simple wrapping class that buffers a value and allows comparison between stored states, useful for detecting changes for syncing purposes.
 * @param <T> The type of variable being wrapped
 */
public class FiaLatch<T> {
	public T cur;
	T stored;
	@Nonnull
	public Function<T,?> getter = (cur)->cur;
	public FiaLatch(T initialValue) {
		cur = initialValue;
		stored = initialValue;
	}
	public FiaLatch(T initialValue,@Nonnull Function<T,?> getter) {
		cur = initialValue;
		stored = initialValue;
		this.getter = getter;
	}
	public T update() { stored = cur; return cur; }
	public boolean needsUpdate() {
		if (stored == null)
			return cur == null;
		return !stored.equals(cur);
	}

	public T getStore() { return stored; }
	public FiaLatch<T> setStore(T newValue) { stored = newValue; return this; }

	public T get() { return cur; }
	public FiaLatch<T> set(T newValue) { cur = newValue; return this; }

	public Object getInterest() { return getter.apply(cur); }
}