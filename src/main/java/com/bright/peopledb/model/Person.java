package com.bright.peopledb.model;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

public class Person {

    @Setter
    private Long id;
    public Person(String firstName, String lastName, ZonedDateTime zonedDateTime) {

    }

    public Long getId(){
        return 1L;
    }

}
