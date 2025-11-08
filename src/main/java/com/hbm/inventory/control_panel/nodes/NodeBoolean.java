package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.nodes.registry.BoolOpRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Objects;

public class NodeBoolean extends Node {

    private BoolOp op;
    private String opId;

    public NodeBoolean(float x, float y) {
        this(x, y, "and");
    }

    public NodeBoolean(float x, float y, String opId) {
        super(x, y);
        this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataType.NUMBER, new DataValueFloat(0)));
        NodeDropdown opSelector = new NodeDropdown(this, otherElements.size(), s -> {
            BoolOp chosen = BoolOpRegistry.byDisplayName(s);
            if (chosen != null) setOperation(chosen);
            return null;
        }, () -> op != null ? op.displayName : "AND");

        for (BoolOp m : BoolOpRegistry.all()) {
            opSelector.list.addItems(m.displayName);
        }
        this.otherElements.add(opSelector);

        setOperation(BoolOpRegistry.byId(opId));
        evalCache = new DataValue[1];
    }

    private void setOperation(BoolOp newOp) {
        if (newOp == null) newOp = BoolOpRegistry.byId("and");
        this.op = newOp;
        this.opId = newOp.id;
        for (NodeConnection c : inputs) c.removeConnection();
        this.inputs.clear();

        for (int i = 0; i < op.arity; i++) {
            String label = op.inputLabels[i];
            this.inputs.add(new NodeConnection(label, this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        }
        recalcSize();
        invalidateCache();
    }

    private void invalidateCache() {
        cacheValid = false;
        evalCache[0] = null;
    }

    public NodeBoolean setData(BoolOp newOp) {
        setOperation(newOp);
        return this;
    }

    public NodeBoolean setData(String id) {
        setOperation(BoolOpRegistry.byId(id));
        return this;
    }

    @Override
    public NodeType getType() {
        return NodeType.BOOLEAN;
    }

    @Override
    public String getDisplayName() {
        return op != null ? op.displayName : "Boolean";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "boolean");
        tag.setString("opId", opId);
        return super.writeToNBT(tag, sys);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
        String id = tag.hasKey("opId", Constants.NBT.TAG_STRING) ? tag.getString("opId") : null;
        setOperation(BoolOpRegistry.byId(id));
        super.readFromNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int idx) {
        if (cacheValid) return evalCache[0];
        cacheValid = true;

        final int n = inputs.size();
        boolean[] args = new boolean[n];
        for (int i = 0; i < n; i++) {
            DataValue v = inputs.get(i).evaluate();
            if (v == null) {
                cacheValid = false;
                return null;
            }
            args[i] = v.getBoolean();
        }

        boolean out = op.eval(args);
        return evalCache[0] = new DataValueFloat(out);
    }

    public static final class BoolOp {
        public final String id;
        public final String displayName;
        public final int arity;
        public final String[] inputLabels;
        private final Fn fn;

        private BoolOp(String id, String displayName, int arity, String[] inputLabels, Fn fn) {
            this.id = Objects.requireNonNull(id);
            this.displayName = Objects.requireNonNull(displayName);
            this.arity = arity;
            this.inputLabels = inputLabels != null ? inputLabels.clone() : defaultLabels(arity);
            this.fn = Objects.requireNonNull(fn);
            if (this.inputLabels.length != arity) {
                throw new IllegalArgumentException("inputLabels length must equal arity");
            }
        }

        private static String[] defaultLabels(int n) {
            String[] names = new String[n];
            for (int i = 0; i < n; i++) names[i] = "Input " + (i + 1);
            return names;
        }

        public static Builder builder(String id, String displayName) {
            return new Builder(id, displayName);
        }

        public boolean eval(boolean[] in) {
            return fn.eval(in);
        }

        @Override
        public String toString() {
            return "BoolOp{" + id + ", " + displayName + ", arity=" + arity + ", labels=" + Arrays.toString(inputLabels) + "}";
        }

        @FunctionalInterface
        public interface Fn {
            boolean eval(boolean[] a);
        }

        public static final class Builder {
            private final String id;
            private final String name;
            private int arity = 2;
            private String[] labels = null;
            private Fn fn;

            private Builder(String id, String name) {
                this.id = id;
                this.name = name;
            }

            public Builder arity(int n) {
                this.arity = n;
                return this;
            }

            public Builder labels(String... labels) {
                this.labels = labels;
                return this;
            }

            public Builder eval(Fn fn) {
                this.fn = fn;
                return this;
            }

            public BoolOp build() {
                return new BoolOp(id, name, arity, labels, fn);
            }
        }
    }
}
