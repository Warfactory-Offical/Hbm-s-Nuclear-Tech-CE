package com.hbm.util;

import com.hbm.lib.UnsafeHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;

import static com.hbm.lib.UnsafeHolder.U;

/**
 * A lock-free, thread-safe MPSC {@link IntArrayList} stack that supports atomic drain.
 *
 * @author mlbv
 */
public class MpscIntArrayListCollector {
    private static final long HEAD_OFF = UnsafeHolder.fieldOffset(MpscIntArrayListCollector.class, "head");
    private Node head;

    public void push(int i) {
        while (true) {
            Node h = (Node) U.getObjectVolatile(this, HEAD_OFF);
            Node n = new Node(i, h);
            if (U.compareAndSwapObject(this, HEAD_OFF, h, n)) return;
        }
    }

    public void pushBatch(@NotNull IntArrayList values) {
        int size = values.size();
        if (size == 0) return;
        Node headNode = null;
        Node tailNode = null;
        for (int i = 0; i < size; i++) {
            Node n = new Node(values.getInt(i), headNode);
            headNode = n;
            if (tailNode == null) tailNode = n;
        }
        while (true) {
            Node h = (Node) U.getObjectVolatile(this, HEAD_OFF);
            tailNode.next = h;
            if (U.compareAndSwapObject(this, HEAD_OFF, h, headNode)) return;
        }
    }

    @NotNull
    public IntArrayList drain() {
        Node h = (Node) U.getAndSetObject(this, HEAD_OFF, null);
        IntArrayList out = new IntArrayList();
        for (Node p = h; p != null; p = p.next) out.add(p.v);
        return out;
    }

    private static final class Node {
        private final int v;
        private Node next;

        Node(int v, Node next) {
            this.v = v;
            this.next = next;
        }
    }
}
