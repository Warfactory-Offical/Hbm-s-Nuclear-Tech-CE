package com.hbm.inventory.control_panel.nodes.registry;

import com.hbm.inventory.control_panel.nodes.NodeMath;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Map;

public final class MathOpRegistry {
    // don't use directly, call via api whenever possible
    public static final Map<String, NodeMath.MathOp> BY_ID = new Object2ObjectLinkedOpenHashMap<>(32);
    public static final Map<String, String> NAME_TO_ID = new Object2ObjectLinkedOpenHashMap<>(32);

    private MathOpRegistry() {
    }

    public static NodeMath.MathOp register(NodeMath.MathOp op) {
        if (BY_ID.containsKey(op.id)) throw new IllegalArgumentException("Duplicate math op id: " + op.id);
        if (NAME_TO_ID.containsKey(op.displayName)) throw new IllegalArgumentException("Duplicate math op displayName: " + op.displayName);
        BY_ID.put(op.id, op);
        NAME_TO_ID.put(op.displayName, op.id);
        return op;
    }

    public static NodeMath.MathOp byId(String id) {
        return BY_ID.get(id);
    }

    public static NodeMath.MathOp byDisplayName(String name) {
        String id = NAME_TO_ID.get(name);
        return id != null ? BY_ID.get(id) : null;
    }

    public static Collection<NodeMath.MathOp> all() {
        return BY_ID.values();
    }

    public static void init() {
        if (!BY_ID.isEmpty()) return;

        register(NodeMath.MathOp.builder("add", "Add").arity(2).eval(a -> a[0] + a[1]).build());
        register(NodeMath.MathOp.builder("sub", "Subtract").arity(2).eval(a -> a[0] - a[1]).build());
        register(NodeMath.MathOp.builder("mul", "Multiply").arity(2).eval(a -> a[0] * a[1]).build());
        register(NodeMath.MathOp.builder("div", "Divide").arity(2).eval(a -> a[0] / a[1]).build());
        register(NodeMath.MathOp.builder("mod", "Modulo").arity(2).eval(a -> a[0] % a[1]).build());
        register(NodeMath.MathOp.builder("pow", "Power").arity(2).labels("Base", "Exponent").eval(a -> (float) Math.pow(a[0], a[1])).build());
        register(NodeMath.MathOp.builder("log", "Logarithm").arity(2).labels("Value", "Base").eval(a -> (float) (Math.log(a[0]) / Math.log(a[1]))).build());
        register(NodeMath.MathOp.builder("exp", "Exponent").arity(1).labels("Input").eval(a -> (float) Math.exp(a[0])).build());
        register(NodeMath.MathOp.builder("sqrt", "Square root").arity(1).labels("Input").eval(a -> (float) Math.sqrt(a[0])).build());
        register(NodeMath.MathOp.builder("abs", "Absolute").arity(1).labels("Input").eval(a -> Math.abs(a[0])).build());
        register(NodeMath.MathOp.builder("eq", "Equal").arity(2).eval(a -> a[0] == a[1] ? 1f : 0f).build());
        register(NodeMath.MathOp.builder("gt", "Greater").arity(2).eval(a -> a[0] > a[1] ? 1f : 0f).build());
        register(NodeMath.MathOp.builder("lt", "Less").arity(2).eval(a -> a[0] < a[1] ? 1f : 0f).build());
        register(NodeMath.MathOp.builder("ge", "Greater/equal").arity(2).eval(a -> a[0] >= a[1] ? 1f : 0f).build());
        register(NodeMath.MathOp.builder("le", "Less/equal").arity(2).eval(a -> a[0] <= a[1] ? 1f : 0f).build());
        register(NodeMath.MathOp.builder("clamp", "Clamp").arity(3).labels("Value", "Min", "Max").eval(a -> MathHelper.clamp(a[0], a[1], a[2])).build());
    }
}
