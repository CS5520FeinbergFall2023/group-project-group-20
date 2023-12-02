package edu.northeastern.groupprojectgroup20.data.model;

public class HistoryData {
   private String lastUpdateTime, calories, exercise, sleep, steps;

    public HistoryData() {
    }

    public HistoryData(String lastUpdateTime, String calories, String exercise, String sleep, String steps) {
        this.lastUpdateTime = lastUpdateTime;
        this.calories = calories;
        this.exercise = exercise;
        this.sleep = sleep;
        this.steps = steps;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }
}
