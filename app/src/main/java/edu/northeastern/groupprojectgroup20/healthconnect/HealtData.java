package edu.northeastern.groupprojectgroup20.healthconnect;

import java.time.ZonedDateTime;

public class HealtData {
    private long steps;
    private double calories;
    private double exercise;
    private long sleep;

    private ZonedDateTime timeStamp;

    public HealtData(long steps, double calories, double exercise, long sleep, ZonedDateTime timeStamp) {
        this.steps = steps;
        this.calories = calories;
        this.exercise = exercise;
        this.sleep = sleep;
        this.timeStamp = ZonedDateTime.now();
    }

    public long getSteps() {
        return steps;
    }

    public double getCalories() {
        return calories;
    }

    public double getExercise() {
        return exercise;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setExercise(double exercise) {
        this.exercise = exercise;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public ZonedDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(ZonedDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }
}
