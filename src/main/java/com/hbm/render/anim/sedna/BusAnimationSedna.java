package com.hbm.render.anim.sedna;


import java.util.HashMap;
import java.util.Map;
// Th3_Sl1ze: this duplicate abomination was done solely for sedna guns, I'm not gonna solve drillgon guns compat issues, no, please don't ask me for that
public class BusAnimationSedna {
    //"buses" with one S since it's not a vehicle
    private final HashMap<String, BusAnimationSequenceSedna> animationBuses = new HashMap<String, BusAnimationSequenceSedna>();
    //multiples buses exist simultaneously and start with 0.
    //a bus has one authority, i.e. the translation of a single part of a model or the rotation of the entire thing.
    //imagine the busses being film strips that hang from the ceiling, with the tape player
    //rolling down, picking up images from all tapes and combining them into a movie.

    //0 by default, will always equal the duration of the longest BusAnimationSequenceSedna
    private int totalTime = 0;

    /**
     * Adds a bus to the animation
     * If an object has several moving parts, each transformation type of each seperat bus should have its own bus
     * Unless you use one bus for several things because the animation is identical, that's ok too
     * @param name of the bus being added
     * @param bus the bus in question
     * @return
     */
    public BusAnimationSedna addBus(String name, BusAnimationSequenceSedna bus) {

        animationBuses.put(name, bus);

        int duration = bus.getTotalTime();

        if(duration > totalTime)
            totalTime = duration;

        return this;
    }

    /**
     * In case there is keyframes being added to sequences in post, this method allows the totalTime
     * to be updated.
     */
    public void updateTime() {

        for(Map.Entry<String, BusAnimationSequenceSedna> sequence : animationBuses.entrySet()) {

            int time = sequence.getValue().getTotalTime();

            if(time > totalTime)
                totalTime = time;
        }
    }

    /**
     * Gets a bus from the specified name. Usually not something you want to do
     * @param name
     * @param bus
     * @return
     */
    public BusAnimationSequenceSedna getBus(String name) {
        return animationBuses.get(name);
    }

    /**
     * Multiplies all keyframe durations by the supplied double. Numbers below 1 make the animation play faster.
     * @param mult
     */
    public void setTimeMult(double mult) {
        for(Map.Entry<String, BusAnimationSequenceSedna> sequence : animationBuses.entrySet()) {
            sequence.getValue().multiplyTime(mult);
        }
    }

    /**
     * Gets the state of a bus at a specified time
     * @param name the name of the bus in question
     * @param millis the elapsed time since the animation started in milliseconds
     * @return
     */
    public double[] getTimedTransformation(String name, int millis) {

        if(this.animationBuses.containsKey(name))
            return animationBuses.get(name).getTransformation(millis);

        return null;
    }

    /**
     * reads all buses and checks if inbetween the last invocation and this one, a sound was scheduled
     * @param lastMillis the last time the bus was checked
     * @param millis the current time
     */
    public void playPendingSounds(int lastMillis, int millis) {
        //TODO: pending
    }

    public int getDuration() {
        return totalTime;
    }

}
