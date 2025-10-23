package com.llib.technical;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LeafiaEase {
    public enum Ease { LINEAR,SINE,QUAD,CUBE,QUART,QUINT,EXPO,CIRC,BACK,ELASTIC,BOUNCE,SMOOTH_STEP,SMOOTHER_STEP }
    public enum Direction { I,O,IO }
    public Ease ease;
    public Direction dir;
    static double lerp(double a,double b,double t) {
        return a+(b-a)*t;
    }
    double i(double x) {
        switch(ease) {
            case LINEAR:
                return x;
            case SINE:
                return 1 - Math.cos((x * Math.PI) / 2);
            case QUAD:
                return Math.pow(x, 2);
            case CUBE:
                return Math.pow(x, 3);
            case QUART:
                return Math.pow(x, 4);
            case QUINT:
                return Math.pow(x, 5);
            case EXPO:
                return (x <= 0) ? 0 : Math.pow(2, ((x - 1) * 10));
            case CIRC:
                return 1 - Math.sqrt(1 - Math.pow(x, 2));
            case BACK:
                double backConstant = 1.70158;
                return (backConstant + 1) * Math.pow(x, 3) - (backConstant) * Math.pow(x, 2);
            case ELASTIC:
                double elasticConstant = (Math.PI / 2) / 3;
                if (x <= 0) return 0;
                else if (x >= 1) return 1;
                else return -(Math.pow(2, (x - 1) * 10)) * Math.sin((x * 10 - 10.75) * elasticConstant);
            case BOUNCE:
                return 1 - o(x);
            case SMOOTH_STEP:
            case SMOOTHER_STEP:
                return 2 * io(x / 2);
            default: return Double.NaN;
        }
    }
    double o(double x) {
        switch(ease) {
            case BOUNCE:
                double n1 = 7.5625;
                double d1 = 2.75;
                if (x < 1 / d1) return n1 * x * x;
                else if (x < 2 / d1) {
                    x = x - 1.5;
                    return n1 * (x / d1) * x + 0.75;
                }
                else if (x < 2.5 / d1) {
                    x = x - 2.25;
                    return n1 * (x / d1) * x + 0.9375;
                }
                else {
                    x = x - 2.625;
                    return n1 * (x / d1) * x + 0.984375;
                }
            case SMOOTH_STEP:
            case SMOOTHER_STEP: return 2*io(x/2+0.5)-1;
            default: return 1-i(1-x);
        }
    }
    double io(double x) {
        switch(ease) {
            case SINE: return -(Math.cos(Math.PI * x) - 1) / 2;
            case BOUNCE: return (x<0.5) ? (1-o(1-2*x))/2 : (1+o(2*x-1))/2;
            case SMOOTH_STEP: return x*x*(x*-2+3);
            case SMOOTHER_STEP: return x*x*x*(x*(x*6-15)+10);
            default: return (x<=.5) ? i(x*2)/2 : 1-i(1-(x*2-1))/2+0.5;
        }
    }
    public double get(double t) {
        switch(dir) {
            case I: return i(t);
            case O: return o(t);
            case IO: return io(t);
            default: return Double.NaN;
        }
    }
    public double get(double t,double min) {
        return get(t,min,1);
    }
    public double get(double t,double min,double max) {
        return lerp(min,max,t);
    }
    public double get(double t,double min,double max,boolean clamp) {
        if (clamp) return MathHelper.clampedLerp(min,max,t);
        else return get(t,min,max);
    }
    public double get(double t,@Nullable Double min,@Nullable Double max,@Nullable Boolean clamp) {
        if (min == null) min = 0.0;
        if (max == null) max = 1.0;
        if (clamp == null) clamp = false;
        return get(t,min,max,clamp);
    }
    public LeafiaEase(Ease ease,Direction dir) {
        this.ease = ease;
        this.dir = dir;
    }
    public boolean updateEase(Ease ease,Direction dir) {
        boolean changed = false;
        if (this.ease != ease) {
            this.ease = ease;
            changed = true;
        }
        if (this.dir != dir) {
            this.dir = dir;
            changed = true;
        }
        return changed;
    }
    public static LeafiaEase parseEase(String s) throws CommandException {
        if (s == "linear") return new LeafiaEase(Ease.LINEAR,Direction.IO);
        String easeName;
        Direction direction;
        if (s.endsWith("In")) {
            easeName = s.substring(0,s.length()-2);
            direction = Direction.I;
        } else if (s.endsWith("Out")) {
            easeName = s.substring(0,s.length()-3);
            direction = Direction.O;
        } else if (s.endsWith("InOut")) {
            easeName = s.substring(0,s.length()-5);
            direction = Direction.IO;
        } else throw new SyntaxErrorException("Invalid ease: "+s,new Object[0]);
        for (Ease ease : Ease.values()) {
            if (ease.name().toLowerCase().equals(easeName))
                return new LeafiaEase(ease,direction);
        }
        throw new SyntaxErrorException("Invalid ease: "+s,new Object[0]);
    }
    public static String[] listEasesForCommands() {
        List<String> list = new ArrayList<>();
        for (Ease ease : Ease.values()) {
            if (ease == Ease.LINEAR)
                list.add(ease.name().toLowerCase());
            else {
                list.add(ease.name().toLowerCase()+"In");
                list.add(ease.name().toLowerCase()+"Out");
                list.add(ease.name().toLowerCase()+"InOut");
            }
        }
        return list.toArray(new String[0]);
    }
}
