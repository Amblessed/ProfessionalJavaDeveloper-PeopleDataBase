package com.bright.peopledb.model;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
@Getter
@ToString
public class Person {

    @Setter
    @Getter(AccessLevel.NONE)
    private Long id;

    private String firstName;
    private String lastName;
    private ZonedDateTime dateOfBirth;

    public Person(String firstName, String lastName, ZonedDateTime dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;

    }

    public Long getId(){
        return id;
    }

}
