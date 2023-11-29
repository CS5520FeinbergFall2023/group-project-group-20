package edu.northeastern.groupprojectgroup20.data.model;

public class UserDetails {
  public String  dob, gender, weight , height, registerDate;

    public UserDetails() {
    }

    public UserDetails(String dob, String gender, String weight, String height, String registerDate) {
        this.dob = dob;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.registerDate = registerDate;
    }
}
