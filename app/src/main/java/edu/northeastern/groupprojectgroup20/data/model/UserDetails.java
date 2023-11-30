package edu.northeastern.groupprojectgroup20.data.model;

import java.util.TimeZone;

public class UserDetails {
  public String  dob, gender, weight , height, registerDate,timeZone;


    public UserDetails() {
    }

    public UserDetails(String dob, String gender, String weight, String height, String registerDate,String timeZone) {
        this.dob = dob;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.registerDate = registerDate;
        this.timeZone = timeZone;
    }
}
