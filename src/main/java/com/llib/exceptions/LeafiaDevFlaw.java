package com.llib.exceptions;

public class LeafiaDevFlaw extends RuntimeException {
    public LeafiaDevFlaw(String s) {
        super("\uE05E\u8AFA"+s);
    }
    public LeafiaDevFlaw(String s,Exception e) {
        this(s+": "+e.getMessage());
        appendStackTrace(e);
    }
    public LeafiaDevFlaw(Exception e) {
        this(e.getMessage());
        appendStackTrace(e);
    }
    public LeafiaDevFlaw appendStackTrace(Exception e) {
        StackTraceElement[] stack = new StackTraceElement[e.getStackTrace().length+this.getStackTrace().length];
        System.arraycopy(this.getStackTrace(),0,stack,0,this.getStackTrace().length);
        System.arraycopy(e.getStackTrace(),0,stack,this.getStackTrace().length,e.getStackTrace().length);
        this.setStackTrace(stack);
        return this;
    }
}