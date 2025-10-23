package com.llib.group;

import java.util.*;

/**
 * A precise Map that uses <tt>.equals()</tt> for equality check, instead of comparing entire damn existence of instances
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class LeafiaMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable {
	static Object[] emptyArray = new Object[]{};
	transient Object[] values;
	transient final LeafiaSet<K> set;
	public LeafiaMap(int shortcutBits) {
		set = new LeafiaSet<>(shortcutBits);
		init();
	}
	public LeafiaMap() {
		set = new LeafiaSet<>();
		init();
	}
	private void init() {
		values = emptyArray;
	}
	public void reflect(LeafiaMap<K,V> map) {
		set.reflect(map.set);
		values = new Object[map.set.contents.length];
		System.arraycopy(map.values,0,values,0,map.set.contents.length);
	}

	int quickSearchValue(Object value) {
		int sizeSub = set.quickAccessSize-1;
		for (int i = 0; i < set.quickAccessSize; i++) {
			int item = set.quickAccess[(set.quickAccessIndex-i)&sizeSub];
			if (item >= 0) {
				if (set.isEquals(values[item],value))
					return item;
			}
		}
		return -1;
	}
	public int keyIndexFor(Object value) {
		int quick = quickSearchValue(value);
		if (quick >= 0) return quick;
		int skip = set.skipStart;
		for (int i = 0; i < set.contents.length; i++) {
			if (skip < set.quickAccessSize) {
				if (set.quickAccessSorted[skip] == i) {
					skip++;
					continue;
				}
			}
			if (set.isEquals(value,values[i])) {
				set.addQuickAccess(i);
				return i;
			}
		}
		return -1;
	}
	public void removeIndex(int index) {
		Object[] transform = new Object[values.length-1];
		if (index > 0)
			System.arraycopy(values,0,transform,0,index);
		if (index < values.length-1)
			System.arraycopy(values,index+1,transform,index,values.length-index-1);
		set.remove(index);
		values = transform;
	}
	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}
	public K getKeyFor(V value) {
		int index = keyIndexFor(value);
		if (index < 0)
			return null;
		else
			return (K)set.contents[index];
	}
	public int indexOf(K key) {
		return set.indexOf(key);
	}
	@Override
	public boolean containsValue(Object value) {
		return keyIndexFor(value) >= 0;
	}
	@Override
	public boolean containsKey(Object key) {
		return set.contains(key);
	}
	@Override
	public V get(Object key) {
		int index = set.indexOf(key);
		if (index < 0)
			return null;
		return (V)values[index];
	}
	@Override
	public V put(K key,V value) {
		int index = set.indexOf(key);
		Object old = null;
		if (index < 0) {
			set.add(key);
			values = Arrays.copyOf(values,set.contents.length);
			values[values.length-1] = value;
		} else {
			old = values[index];
			values[index] = value;
		}
		return (V)old;
	}
	@Override
	public V putIfAbsent(K key,V value) {
		int index = set.indexOf(key);
		Object old = null;
		if (index < 0) {
			set.add(key);
			values = Arrays.copyOf(values,set.contents.length);
			values[values.length-1] = value;
		} else {
			old = values[index];
			// do not write in new value
		}
		return (V)old;
	}
	@Override
	public V remove(Object key) {
		int index = set.indexOf(key);
		Object old = null;
		if (index < 0)
			return null;
		else
			old = values[index];
		removeIndex(index);
		return (V)old;
	}
	@Override
	public void clear() {
		values = emptyArray;
		set.clear();
	}

	@Override
	public Set<K> keySet() {
		return set.clone();
	}
	@Override
	public Collection<V> values() {
		ArrayList<V> list = new ArrayList<>();
		for (Object value : values)
			list.add((V)value);
		return list;
	}
	@Override
	public Set<Entry<K,V>> entrySet() {
		LeafiaSet<Entry<K,V>> out = new LeafiaSet<>();
		for (int i = 0; i < set.contents.length; i++) {
			K key = (K)set.contents[i];
			V value = (V)values[i];
			out.add(new Entry<K,V>() {
				@Override public K getKey() { return key; }
				@Override public V getValue() { return value; }
				@Override public V setValue(V newValue) { return put(key,newValue); }
			});
		}
		return out;
	}

	@Override
	public LeafiaMap<K,V> clone() {
		LeafiaMap<K,V> copy = new LeafiaMap<>(Integer.numberOfTrailingZeros(set.quickAccessSize));
		copy.reflect(this);
		return copy;
	}
}