package com.hbm.inventory.control_panel.nodes.registry;

import com.hbm.inventory.control_panel.nodes.NodeBoolean;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.Collection;
import java.util.Map;

public final class BoolOpRegistry {
    // don't use directly
    public static final Map<String, NodeBoolean.BoolOp> BY_ID = new Object2ObjectLinkedOpenHashMap<>(16);
    public static final Map<String, String> NAME_TO_ID = new Object2ObjectLinkedOpenHashMap<>(16);

    private BoolOpRegistry() {}

    public static NodeBoolean.BoolOp register(NodeBoolean.BoolOp op) {
        if (BY_ID.containsKey(op.id)) throw new IllegalArgumentException("Duplicate bool op id: " + op.id);
        if (NAME_TO_ID.containsKey(op.displayName)) throw new IllegalArgumentException("Duplicate bool op displayName: " + op.displayName);
        BY_ID.put(op.id, op);
        NAME_TO_ID.put(op.displayName, op.id);
        return op;
    }

    public static NodeBoolean.BoolOp byId(String id) {
        if (BY_ID.isEmpty()) init();
        return id != null ? BY_ID.get(id) : null;
    }

    public static NodeBoolean.BoolOp byDisplayName(String name) {
        if (BY_ID.isEmpty()) init();
        String id = NAME_TO_ID.get(name);
        return id != null ? BY_ID.get(id) : null;
    }

    public static Collection<NodeBoolean.BoolOp> all() {
        if (BY_ID.isEmpty()) init();
        return BY_ID.values();
    }

    public static void init() {
        if (!BY_ID.isEmpty()) return;

        register(NodeBoolean.BoolOp.builder("and","AND").arity(2).labels("A","B").eval(a -> a[0] && a[1]).build());
        register(NodeBoolean.BoolOp.builder("or","OR").arity(2).labels("A","B").eval(a -> a[0] || a[1]).build());
        register(NodeBoolean.BoolOp.builder("not","NOT").arity(1).labels("Input").eval(a -> !a[0]).build());
        register(NodeBoolean.BoolOp.builder("xor","XOR").arity(2).labels("A","B").eval(a -> a[0] ^ a[1]).build());
        register(NodeBoolean.BoolOp.builder("nand","NAND").arity(2).labels("A","B").eval(a -> !(a[0] && a[1])).build());
        register(NodeBoolean.BoolOp.builder("nor","NOR").arity(2).labels("A","B").eval(a -> !(a[0] || a[1])).build());
        register(NodeBoolean.BoolOp.builder("xnor","XNOR").arity(2).labels("A","B").eval(a -> a[0] == a[1]).build());
    }
}
