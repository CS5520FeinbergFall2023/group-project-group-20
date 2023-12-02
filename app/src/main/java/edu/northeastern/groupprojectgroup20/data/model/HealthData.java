package edu.northeastern.groupprojectgroup20.data.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HealthData {
    private long steps;
    private double calories;
    private double exercise;
    private long sleep;
    private String lastUpdateTime;
    private String timeStamp;

    public HealthData() {}

    // constructor for testing
    public HealthData(long steps, double calories, double exercise, long sleep, ZonedDateTime timeStamp) {
        this.steps = steps;
        this.calories = calories;
        this.exercise = exercise;
        this.sleep = sleep;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.lastUpdateTime = timeStamp.format(formatter);
        this.timeStamp = timeStamp.format(timeStampFormatter);

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

    public String getLastUpdateTime() {
        return lastUpdateTime;
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

    public String getTimeStamp() {
        return timeStamp;
    }

}
