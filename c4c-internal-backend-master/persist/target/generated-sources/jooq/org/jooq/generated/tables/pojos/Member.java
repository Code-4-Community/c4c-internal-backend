/*
 * This file is generated by jOOQ.
 */
package org.jooq.generated.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Member implements Serializable {

    private static final long serialVersionUID = 987369989;

    private String  id;
    private String  email;
    private String  firstName;
    private String  lastName;
    private Integer graduationYear;
    private String  major;
    private Integer privilegeLevel;

    public Member() {}

    public Member(Member value) {
        this.id = value.id;
        this.email = value.email;
        this.firstName = value.firstName;
        this.lastName = value.lastName;
        this.graduationYear = value.graduationYear;
        this.major = value.major;
        this.privilegeLevel = value.privilegeLevel;
    }

    public Member(
        String  id,
        String  email,
        String  firstName,
        String  lastName,
        Integer graduationYear,
        String  major,
        Integer privilegeLevel
    ) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.graduationYear = graduationYear;
        this.major = major;
        this.privilegeLevel = privilegeLevel;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getGraduationYear() {
        return this.graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getMajor() {
        return this.major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getPrivilegeLevel() {
        return this.privilegeLevel;
    }

    public void setPrivilegeLevel(Integer privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Member (");

        sb.append(id);
        sb.append(", ").append(email);
        sb.append(", ").append(firstName);
        sb.append(", ").append(lastName);
        sb.append(", ").append(graduationYear);
        sb.append(", ").append(major);
        sb.append(", ").append(privilegeLevel);

        sb.append(")");
        return sb.toString();
    }
}
