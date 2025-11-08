package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.nodes.registry.MathOpRegistry;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.Objects;

public class NodeMath extends Node {

    private MathOp op;
    private String opId;

    public NodeMath(float x, float y) {
        this(x, y, "add");
    }

    public NodeMath(float x, float y, String opId) {
        super(x, y);
        this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataType.NUMBER, new DataValueFloat(0)));
        NodeDropdown opSelector = new NodeDropdown(this, otherElements.size(), s -> {
            MathOp chosen = MathOpRegistry.byDisplayName(s);
            if (chosen != null) setOperation(chosen);
            return null;
        }, () -> op != null ? op.displayName : "Add");

        for (MathOp m : MathOpRegistry.all()) {
            opSelector.list.addItems(m.displayName);
        }
        this.otherElements.add(opSelector);

        setOperation(MathOpRegistry.byId(opId));
        evalCache = new DataValue[1];
    }

    private void setOperation(MathOp newOp) {
        if (newOp == null) newOp = MathOpRegistry.byId("add");
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

    public NodeMath setData(MathOp op) {
        setOperation(op);
        return this;
    }

    private void invalidateCache() {
        cacheValid = false;
        evalCache[0] = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "math");
        tag.setString("opId", opId);
        return super.writeToNBT(tag, sys);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
        MathOp candidate = MathOpRegistry.byId(tag.getString("opId"));
        setOperation(candidate);
        super.readFromNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int idx) {
        if (cacheValid) return evalCache[0];
        cacheValid = true;

        final int n = inputs.size();
        float[] args = new float[n];
        for (int i = 0; i < n; i++) {
            DataValue v = inputs.get(i).evaluate();
            if (v == null) {
                cacheValid = false;
                return null;
            }
            args[i] = v.getNumber();
        }

        float out = op.eval(args);
        return evalCache[0] = new DataValueFloat(out);
    }

    @Override
    public NodeType getType() {
        return NodeType.MATH;
    }

    @Override
    public String getDisplayName() {
        return op != null ? op.displayName : "Math";
    }

    public static final class MathOp {
        public final String id;
        public final String displayName;
        public final int arity;
        public final String[] inputLabels;
        private final FloatN fn;

        private MathOp(String id, String displayName, int arity, String[] inputLabels, FloatN fn) {
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

        public float eval(float[] in) {
            return fn.eval(in);
        }

        @Override
        public String toString() {
            return "MathOp{" + id + ", " + displayName + ", arity=" + arity + ", labels=" + Arrays.toString(inputLabels) + "}";
        }

        @FunctionalInterface
        public interface FloatN {
            float eval(float[] a);
        }

        public static final class Builder {
            private final String id;
            private final String name;
            private int arity = 2;
            private String[] labels = null;
            private FloatN fn;

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

            public Builder eval(FloatN fn) {
                this.fn = fn;
                return this;
            }

            public MathOp build() {
                return new MathOp(id, name, arity, labels, fn);
            }
        }
    }
}
