package com.codeforcommunity.dto;

public class UserReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  private final int id;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final int year;
  private final String major;
  private final int privilegeLevel;
  private final int yearOfGraduation;
  private final String college;
  private final String gender;

  public UserReturn(
      int id,
      String email,
      String firstName,
      String lastName,
      String _password,
      int year,
      String major,
      int privilegeLevel,
      int yearOfGraduation,
      String college,
      String gender) {
    this.id = id;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.year = year;
    this.major = major;
    this.privilegeLevel = privilegeLevel;
    this.yearOfGraduation = yearOfGraduation;
    this.college = college;
    this.gender = gender;
  }

  public int getId() {
    return this.id;
  }

  public String getEmail() {
    return this.email;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public int getYear() {
    return this.year;
  }

  public String getMajor() {
    return this.major;
  }

  public int getPrivilegeLevel() {
    return this.privilegeLevel;
  }

  public int getYearOfGraduation() {
    return this.yearOfGraduation;
  }

  public String getCollege() {
    return this.college;
  }

  public String getGender() {
    return this.gender;
  }

  @Override
  public String toString() {
    return this.id
        + " "
        + this.email
        + " "
        + this.firstName
        + " "
        + this.lastName
        + " "
        + this.year
        + " "
        + this.yearOfGraduation
        + " "
        + this.major
        + " "
        + this.college
        + " "
        + this.gender
        + " "
        + this.privilegeLevel;
  }
}
