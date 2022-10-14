package com.bright.peopledb.repository;

/*
 * @Project Name: PeopleDB
 * @Author: Okechukwu Bright Onwumere
 * @Created: 22/08/2022
 */


import com.bright.peopledb.model.Person;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;


class PeopleRepositoryTests {

    private final Name name = new Faker().name();

    public ZonedDateTime getZonedDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoSecond, String zoneID){
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoSecond, ZoneId.of(zoneID));
    }

    @Test
    @DisplayName("Save a single User in the Database")
    void canSaveOnePerson() {
        PeopleRepository repository = new PeopleRepository();
        Person person = new Person(name.firstName(), name.lastName(), getZonedDateTime(1980, 11,15, 15, 15, 0, 0, "-6"));
        Person savedPerson = repository.save(person);
        assertThat(savedPerson.getId()).isPositive();
    }




}
