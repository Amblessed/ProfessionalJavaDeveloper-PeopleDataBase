package com.bright.peopledb.model;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;


/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 15/10/2022
 */


class PersonTest {

    private final Name name = new Faker().name();
    private String firstName;
    private String lastName;

    @BeforeEach
    void setUp() {
        firstName = name.firstName();
        lastName = name.lastName();
    }

    @Test
    @DisplayName("Test that for the same user in database")
    void testForEquality(){
        Person person1 = new Person(firstName, lastName, ZonedDateTime.of(2000, 9, 15, 12, 15, 0, 0, ZoneId.of("-6")));
        Person person2 = new Person(firstName, lastName, ZonedDateTime.of(2000, 9, 15, 12, 15, 0, 0, ZoneId.of("-6")));
        assertThat(person1).isEqualTo(person2);
    }

    @Test
    @DisplayName("Test that different users are not equal in database")
    void testForInEquality(){
        Person person1 = new Person(name.firstName(), lastName, ZonedDateTime.of(2002, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person person2 = new Person(name.firstName(), lastName, ZonedDateTime.of(2002, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        assertThat(person1).isNotEqualTo(person2);
    }

}