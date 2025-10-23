package com.llib.group;

import com.llib.LLibRsc.*;
import com.llib.exceptions.LeafiaDevFlaw;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A precise Set that also allows concurrent modifications during iteration.
 * <p>Biggest difference is the <tt>contains</tt> method, while HashSet and HashMap compares
 * only the hash of instances, this one uses <tt>.equals()</tt> for more proper detection, albeit slower.
 * <p>Much recommended to use this one than HashSet unless we're dealing with billions of elements.
 * @param <E> the type of elements maintained by this set
 */
public class LeafiaSet<E> extends AbstractSet<E> implements Set<E>, List<E>, Cloneable {
	static final LeafiaVoid VOID = new LeafiaVoid();
	transient int quickAccessIndex = 0;
	transient int[] quickAccess;
	transient int[] quickAccessSorted;
	public transient final int quickAccessSize;
	transient int skipStart;

	static Object[] emptyArray = new Object[]{};
	transient Object[] contents;

	public LeafiaSet(int shortcutBits) {
		this.quickAccessSize = 1 << shortcutBits;
		this.init();
	}
	public LeafiaSet() {
		this.quickAccessSize = 8;
		this.init();
	}
	private void init() {
		contents = emptyArray;
		quickAccess = new int[quickAccessSize];
		quickAccessSorted = new int[quickAccessSize];
		Arrays.fill(quickAccess,-1);
		this.skipStart = quickAccessSize;
	}
	public void reflect(LeafiaSet<E> set) {
		if (this.quickAccessSize != set.quickAccessSize) throw new LeafiaDevFlaw("Cannot reflect LeafiaSet with different quickAccessSize");
		quickAccess = new int[set.quickAccess.length];
		quickAccessSorted = new int[set.quickAccessSorted.length];
		skipStart = set.skipStart;
		quickAccessIndex = set.quickAccessIndex;
		contents = new Object[set.contents.length];
		System.arraycopy(set.quickAccess,0,quickAccess,0,set.quickAccess.length);
		System.arraycopy(set.quickAccessSorted,0,quickAccessSorted,0,set.quickAccessSorted.length);
		System.arraycopy(set.contents,0,contents,0,set.contents.length);
	}
	boolean isEquals(Object a,Object b) {
		if (a instanceof LeafiaVoid) return false;
		if (b instanceof LeafiaVoid) return false;
		if (a == null | b == null) {
			return (a == null) && (b == null);
		} else
			return a.equals(b);
	}
	boolean isEqualsInd(int a,int b) {
		if (Math.min(a,b) < 0) return false;
		return isEquals(contents[a],contents[b]);
	}
	void sortQuickAccess() {
		System.arraycopy(quickAccess,0,quickAccessSorted,0,quickAccessSize);
		Arrays.sort(quickAccessSorted);
		for (int i = 0; i < quickAccessSize; i++) {
			skipStart = i;
			if (quickAccessSorted[i] >= 0) break;
		}
	}
	void moveQuickAccess(int from,int to) {
		int amt = to-from;
		for (int i = 0; i < quickAccessSize; i++) {
			if (quickAccess[i] >= from)
				quickAccess[i] += amt;
		}
	}
	void addQuickAccess(int element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = quickAccessIndex; i > quickAccessIndex-quickAccessSize; i--) {
			// if there was duplicate on the quick access
			if (isEqualsInd(quickAccess[i&sizeSub],element)) {
				if ((i&sizeSub) == quickAccessIndex) return; // if it was already the first entry, do nothing
				if ((i&sizeSub) == nextIndex) break; // if it was the next to be replaced, ignore as it will be replaced by the same value anyway
				quickAccess[i&sizeSub] = quickAccess[nextIndex]; // swap out the duplicate with entry that would be overwritten next otherwise
				break;
			}
		}
		quickAccess[nextIndex] = element; // write quick access
		quickAccessIndex = nextIndex;
		sortQuickAccess();
	}
	void removeQuickAccess(int element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = quickAccessIndex; i > quickAccessIndex-quickAccessSize; i--) {
			if (isEqualsInd(quickAccess[i&sizeSub],element)) {
				quickAccess[i&sizeSub] = quickAccess[nextIndex];
				quickAccess[nextIndex] = -1;
				sortQuickAccess();
				return;
			}
		}
	}
	int quickSearch(Object element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = 0; i < quickAccessSize; i++) {
			int item = quickAccess[(quickAccessIndex-i)&sizeSub];
			if (item >= 0) {
				if (item >= contents.length) continue; // failsafe
				if (isEquals(contents[item],element)) {
					// swap out the position of this entry with the latest one
					// do not call sortQuickAccess as this does not change values, just the order
					quickAccess[(quickAccessIndex-i)&sizeSub] = quickAccess[nextIndex];
					quickAccess[nextIndex] = item;
					quickAccessIndex = nextIndex;
					return item;
				}
			}
		}
		return -1;
	}
	@Override
	public Iterator<E> iterator() {
		Object[] copy = new Object[LeafiaSet.this.contents.length];
		System.arraycopy(contents,0,copy,0,contents.length);
		return new Iterator<E>() {
			int needle = 0;
			@Override
			public boolean hasNext() {
				return copy.length > needle;
			}
			@Override
			public E next() {
				return (E)(copy[needle++]);
			}
		};
	}

	////////////////////////////////////////////////////////////

	@Override
	public int size() {
		return this.contents.length;
	}
	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}
	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	/**
	 * Returns the index of occurrence of the specified element in the internal array of this set,
	 * or -1 if this set does not contain one.
	 * <br>More formally, returns the index <tt>i</tt> such that
	 * <pre>{@code (element==null ? get(i)==null : element.equals(get(i)))}</pre> or -1 if there is no such index.
	 * @param element element to search for
	 * @return the index of occurrence of the specified element in the internal array of this set, or -1 if this set does not contain one.
	 */
	@Override
	public int indexOf(Object element) {
		int quick = quickSearch(element);
		if (quick >= 0) return quick;
		int skip = skipStart;
		for (int i = 0; i < contents.length; i++) {
			if (skip < quickAccessSize) {
				if (quickAccessSorted[skip] == i) {
					skip++;
					continue;
				}
			}
			if (isEquals(element,contents[i])) {
				addQuickAccess(i);
				return i;
			}
		}
		return -1;
	}

	/**
	 * As sets does not permit duplicate elements within, this is completely identical to
	 * <pre>{@code indexOf(o);}</pre>
	 * @param o element to search for
	 * @return the index of occurrence of the specified element in the internal array of this set, or -1 if this set does not contain one.
	 * @see LeafiaSet#indexOf(Object)
	 */
	@Override
	public int lastIndexOf(Object o) { return indexOf(o); }

	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException("LeafiaSet does not support listIterator, go home!");
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException("LeafiaSet does not support listIterator, go home!");
	}

	@Override
	public List<E> subList(int fromIndex,int toIndex) {
		//TODO: this
		throw new UnsupportedOperationException("W.I.P.");
	}

	@Override
	public Object[] toArray() {
		Object[] readOnlyArray = new Object[contents.length];
		System.arraycopy(contents,0,readOnlyArray,0,contents.length);
		return readOnlyArray;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return super.toArray(a);
	}

	@Override
	public boolean add(E e) {
		if (contains(e)) return false;
		contents = Arrays.copyOf(contents,contents.length+1);
		contents[contents.length-1] = e;
		return true;
	}

	/**
	 * Adds the specified element to the end of internal array in this set if it was not already present.
	 * <br>More formally, adds the specified element <tt>e</tt> to this set if
	 * this set contains no element <tt>e2</tt> such that
	 * <pre>{@code (e==null ? e2==null : e.equals(e2))}</pre>
	 * If said element already exists in this set, it will be replaced with the new element <tt>e</tt>.
	 * <h3>LeafiaSet exclusive.</h3>
	 * @param e element to be added to this set
	 * @return index of the element replaced, or -1 if it was added as a new element
	 */
	public int addAnyway(E e) {
		int index = indexOf(e);
		if (index >= 0) {
			contents[index] = e;
		} else
			add(e);
		return index;
	}
	public boolean removeElement(Object o) {
		int index = indexOf(o);
		if (index < 0) return false;
		remove(index);
		return true;
	}
	@Override
	public boolean remove(Object o) {
		return removeElement(o);
	}
	@Override
	public E remove(int index) {
		E removed = (E)contents[index];
		Object[] transform = new Object[contents.length-1];
		if (index > 0)
			System.arraycopy(contents,0,transform,0,index);
		if (index < contents.length-1)
			System.arraycopy(contents,index+1,transform,index,contents.length-index-1);
		removeQuickAccess(index);
		moveQuickAccess(index+1,index);
		sortQuickAccess();
		contents = transform;
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return super.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	/**
	 * Inserts all the elements in the specified collection into this
	 * set at the specified internal array position.
	 * <br>If the collection contained an element this set also contains,
	 * that element is ignored.
	 * @param index index at which to insert the first element from the
	 *              specified collection
	 * @param c collection containing elements to be added to this list
	 * @return true if all elements were added in place successfully
	 */
	@Override
	public boolean addAll(int index,Collection<? extends E> c) {
		boolean allSuccess = true;
		index = Math.max(Math.min(index,this.contents.length-1),0);
		for (E e : c) {
			if (!contains(e)) {
				add(index,e);
				index++;
			} else
				allSuccess = false;
		}
		return allSuccess;
	}
	/**
	 * Inserts all the elements in the specified collection into this
	 * set at the specified internal array position.
	 * <br>If the collection contained an element this set also contains,
	 * that element is replaced with element from the collection.
	 * <h3>LeafiaSet exclusive.</h3>
	 * @param index index at which to insert the first element from the
	 *              specified collection
	 * @param c collection containing elements to be added to this list
	 * @return true if all elements were added in place successfully
	 */
	public boolean addAllAnyway(int index,Collection<? extends E> c) {
		boolean allSuccess = true;
		index = Math.max(Math.min(index,this.contents.length-1),0);
		for (E e : c) {
			if (!contains(e)) {
				add(index,e);
				index++;
			} else {
				addAnyway(e);
				allSuccess = false;
			}
		}
		return allSuccess;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		List.super.replaceAll(operator);
	}

	@Override
	public void sort(Comparator<? super E> c) {
		Arrays.fill(quickAccess,-1);
		skipStart = quickAccessSize;
		//List.super.sort(c);
		//TODO: this
		throw new UnsupportedOperationException("W.I.P.");
	}

	@Override
	public void clear() {
		contents = emptyArray;
		Arrays.fill(quickAccess,-1);
		skipStart = quickAccessSize;
	}

	@Override
	public Spliterator<E> spliterator() {
		return super.spliterator();
	}

	@Override
	public E get(int index) {
		return (E)contents[index];
	}
	/**
	 * Replaces the element at the specified position in the internal array of
	 * this set with the specified element if this set does not already contain one.
	 * <br>More formally, adds the specified element <tt>newElement</tt> to this set if
	 * this set contains no element <tt>e2</tt> such that
	 * <pre>{@code (newElement==null ? e2==null : newElement.equals(e2))}</pre>
	 * @param index index of the element to replace
	 * @param newElement element to be stored at the specified position
	 * @return the element previously at the specified position,
	 * or <tt>newElement</tt> if this set already contains one.
	 */
	@Override
	public E set(int index,E newElement) {
		if (contains(newElement))
			return newElement;
		E previous = get(index);
		removeQuickAccess(index);
		sortQuickAccess();
		contents[index] = newElement;
		return previous;
	}

	@Override
	public void add(int index,E element) {
		if (contains(element)) return;
		Object[] transform = new Object[contents.length+1];
		if (index > 0)
			System.arraycopy(contents,0,transform,0,index);
		if (index < contents.length)
			System.arraycopy(contents,index,transform,index+1,contents.length-index);
		moveQuickAccess(index,index+1);
		transform[index] = element;
		contents = transform;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public LeafiaSet<E> clone() {
		LeafiaSet<E> copy = new LeafiaSet<>(Integer.numberOfTrailingZeros(quickAccessSize));
		copy.reflect(this);
		return copy;
	}
}
