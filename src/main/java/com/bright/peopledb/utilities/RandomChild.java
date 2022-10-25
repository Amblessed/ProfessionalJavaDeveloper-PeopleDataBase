package com.bright.peopledb.utilities;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 25/10/2022
 */


import com.bright.peopledb.model.Person;
import com.github.javafaker.Faker;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RandomChild {

    private RandomChild(){}

    public static Person getRandomChild(String lastName, int yearDiff){
        SecureRandom secureRandom = new SecureRandom();
        int month = secureRandom.nextInt(1,13);
        int dayBound = switch(month){
            case 1, 3, 5, 7, 8, 10, 12 -> 32;
            case 2 -> 29;
            default -> 31;
        };
        int day = secureRandom.nextInt(1,dayBound);
        return new Person(new Faker().name().firstName(), lastName, ZonedDateTime.of(2010 + yearDiff, month, day, 15, 15, 0, 0, ZoneId.of("-6")));
    }
}
