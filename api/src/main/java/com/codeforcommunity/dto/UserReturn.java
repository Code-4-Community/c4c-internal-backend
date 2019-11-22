package com.codeforcommunity.dto;

public class UserReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  private final int id;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final String year;
  private final String major;
  private final int privilegeLevel;

  public UserReturn(int id, String email, String firstName, String lastName, String year, String major,
      int privilegeLevel) {
    this.id = id;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.year = year;
    this.major = major;
    this.privilegeLevel = privilegeLevel;
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

  public String getYear() {
    return this.year;
  }

  public String getMajor() {
    return this.major;
  }

  public int getPrivilegeLevel() {
    return this.privilegeLevel;
  }

  @Override
  public String toString() {
    return this.id + " " + this.email + " " + this.firstName + " " + this.lastName + " " + this.year + " " + this.major + " " + this.privilegeLevel;
  }
}