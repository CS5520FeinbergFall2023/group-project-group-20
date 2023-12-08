package edu.northeastern.groupprojectgroup20.data.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GameData {
    private long TotalAccumulatedSteps;
    private double TotalAccumulatedCalories;
    private double TotalAccumulatedExercise;
    private long TotalAccumulatedSleep;
    private String lastUpdateTime;
    private double HP;
    private double ATK;
    private double DEF;
    private String timeStamp;

    public GameData() {}

    public GameData(long steps, double calories, double exercise, long sleep, ZonedDateTime lastUpdateTime) {
        this.TotalAccumulatedSteps = steps;
        this.TotalAccumulatedCalories = calories;
        this.TotalAccumulatedExercise = exercise;
        this.TotalAccumulatedSleep = sleep;
        this.HP = calculateHPFromSteps();
        this.ATK = calculateATKFromCalories();
        this.DEF = calculateDEFfromExercise();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        this.lastUpdateTime = lastUpdateTime.format(formatter);
        DateTimeFormatter timeStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.timeStamp = lastUpdateTime.format(timeStampFormatter);
    }

    public void setTotalAccumulatedExercise(double totalAccumulatedExercise) {
        TotalAccumulatedExercise = totalAccumulatedExercise;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public double getHP() {
        return HP;
    }

    public void setHP(double HP) {
        this.HP = HP;
    }

    public double getATK() {
        return ATK;
    }

    public void setATK(double ATK) {
        this.ATK = ATK;
    }

    public double getDEF() {
        return DEF;
    }

    public void setDEF(double DEF) {
        this.DEF = DEF;
    }

    //HP
    public double calculateHPFromSteps() {
        double initialHP = 300;
        return initialHP + TotalAccumulatedSteps / 100;
    }

    //ATK
    public double calculateATKFromCalories() {
        double initialATK = 100;
        return initialATK + TotalAccumulatedCalories * 0.015;
    }

    //DEF
    public double calculateDEFfromExercise() {
        double initialDEF = 100;

        double DEFValue = 0;
        if (TotalAccumulatedExercise <= 60) {
            DEFValue = TotalAccumulatedExercise * 1.2;
        } else if (60 < TotalAccumulatedExercise && TotalAccumulatedExercise <300) {
            DEFValue = (TotalAccumulatedExercise - 60) * 0.3 + 72;
        }
        else {
            DEFValue = 144;
        }
        return initialDEF + DEFValue;
    }

    // Recovery
    public int calculateRecoveryFromSleep() {
        int skillRecovery = 2;

        if (TotalAccumulatedSleep > 480) {
            skillRecovery = 3;
        } else if (TotalAccumulatedSleep < 240) {
            skillRecovery = 1;
        } else {
            skillRecovery = 2;
        }


        return skillRecovery;
    }

    public long getTotalAccumulatedSteps() {
        return TotalAccumulatedSteps;
    }

    public void setTotalAccumulatedSteps(long steps) {
        this.TotalAccumulatedSteps = steps;
    }

    public double getTotalAccumulatedCalories() {
        return TotalAccumulatedCalories;
    }

    public void setTotalAccumulatedCalories(double totalAccumulatedcalories) {
        this.TotalAccumulatedCalories = totalAccumulatedcalories;
    }

    public double getTotalAccumulatedExercise() {
        return TotalAccumulatedExercise;
    }

    public void setExercise(double exercise) {
        this.TotalAccumulatedExercise = exercise;
    }

    public long getTotalAccumulatedSleep() {
        return TotalAccumulatedSleep;
    }

    public void setTotalAccumulatedSleep(long totalAccumulatedSleep) {
        this.TotalAccumulatedSleep = totalAccumulatedSleep;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
