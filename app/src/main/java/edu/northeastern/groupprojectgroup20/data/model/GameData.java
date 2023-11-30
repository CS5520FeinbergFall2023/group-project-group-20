package edu.northeastern.groupprojectgroup20.data.model;

public class GameData {
    private long steps;
    private double calories;
    private double exercise;
    private long sleep;
    private String lastUpdateTime;

    public GameData(long steps, double calories, double exercise, long sleep, String lastUpdateTime) {
        this.steps = steps;
        this.calories = calories;
        this.exercise = exercise;
        this.sleep = sleep;
        this.lastUpdateTime = lastUpdateTime;
    }

    //HP
    public int calculateAttributeFromSteps() {
        return (int)(steps / 100);
    }

    //ATK
    public int calculateAttributeFromCalories() {
        return (int)(calories * 1.5);
    }

    //DEF
    public int calculateAttributeFromExercise() {
        int DEFValue = 0;
        if (exercise <= 60) {
            DEFValue = (int)(exercise * 1.2);
        } else if (60 < exercise && exercise <300) {
            DEFValue = (int)((exercise - 60) * 0.3) + 72;
        }
        else {
            DEFValue = 144;
        }
        return DEFValue;
    }

    public int calculateAttributeFromSleep() {
        int skillRecovery = 2; // 初始设定为2

        if (sleep > 480) { // 如果睡眠时长大于480分钟
            skillRecovery = 3;
        } else if (sleep < 240) { // 如果睡眠时长小于240分钟
            skillRecovery = 1;
        } else {
            skillRecovery = 2;
        }


        return skillRecovery;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getExercise() {
        return exercise;
    }

    public void setExercise(double exercise) {
        this.exercise = exercise;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
