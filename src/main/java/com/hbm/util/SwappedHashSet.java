package com.hbm.util;

import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

/**
 * A crude implementation of the HashSet with a few key differences:
 * - instead of being stored as the key, the objects are stored as values in the underlying HashMap with the hash being the key
 *   - consequently, things with matching hash are considered the same, skipping the equals check
 * - no equals check means that collisions are possible, so be careful
 * - the underlying HashMap is accessible, which means that the instances can be grabbed out of the HashedSet if a hash is supplied
 *
 * This sack of crap was only intended for the drone request network code
 *
 * @author hbm
 *
 * @param <T>
 */
//used to be called HashedSet but i renamed it to something more descriptive
public class SwappedHashSet<T> implements Set<T> {

    HashMap<Integer, T> map = new HashMap<>();

    public static class SwappedHashIterator<T> implements Iterator<T> {

        private final Iterator<Map.Entry<Integer, T>> iterator;

        public SwappedHashIterator(SwappedHashSet<T> set) {
            this.iterator = set.map.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public T next() {
            return this.iterator.next().getValue();
        }

        @Override
        public void remove() {
            this.iterator.remove();
        }
    }

    public SwappedHashSet() { }

    public SwappedHashSet(Set<T> reachableNodes) {
        this.addAll(reachableNodes);
    }

    public HashMap<Integer, T> getMap() {
        return this.map;
    }

    @Override
    public boolean add(T e) {
        boolean contains = this.contains(e);
        this.map.put(e.hashCode(), e);
        return contains;
    }

    @Override
    public boolean addAll(Collection c) {
        boolean ret = false;
        for(Object o : c) if(add((T) o)) ret = true;
        return ret;
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return this.map.containsKey(o.hashCode());
    }

    @Override
    public boolean containsAll(Collection c) {

        for(Object o : c) {
            if(!this.contains(o)) return false;
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new SwappedHashIterator<>(this);
    }

    @Override
    public boolean remove(Object o) {
        T obj = this.map.get(o.hashCode());
        boolean rem = false;

        if(obj != null) {
            rem = true;
            this.map.remove(o.hashCode());
        }

        return rem;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new NotImplementedException("Fuck you");
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Object[] toArray() {
        throw new NotImplementedException("Fuck you");
    }

    @Override
    public <A> A[] toArray(A[] a) {
        throw new NotImplementedException("Fuck you");
    }
}
