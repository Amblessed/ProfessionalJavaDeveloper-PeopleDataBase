package com.bright.peopledb.repository;

/*
 * @Project Name: PeopleDB
 * @Author: Okechukwu Bright Onwumere
 * @Created: 22/08/2022
 */


import com.bright.peopledb.model.Person;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


class PeopleRepositoryTests {

    private final Name name = new Faker().name();
    private Connection connection;
    private String firstName;
    private String lastName;
    private PeopleRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:~/peopletest".replace("~", System.getProperty("user.home")));
        connection.setAutoCommit(false); //changes made to the database are not saved as long as connection is open
        repository = new PeopleRepository(connection);
        firstName = name.firstName();
        lastName = name.lastName();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if(Objects.nonNull(connection)){
            connection.close();
        }
    }

    void printNames(String firstName, String lastName){
        System.out.printf("First Name: %s, Last Name: %s%n", firstName, lastName);
    }

    @Test
    @DisplayName("Can save a single user in the Database")
    void canSaveOnePerson() {
        printNames(firstName, lastName);
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1980, 11,15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person savedPerson = repository.save(person);
        assertThat(savedPerson.getId()).isPositive();
    }

    @Test
    @DisplayName("Can save two users in the Database")
    void canSaveTwoPerson(){
        printNames(firstName, lastName);
        Person person1 = new Person(firstName, lastName, ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        firstName = name.firstName();
        lastName = name.lastName();
        printNames(firstName, lastName);
        Person person2 = new Person(firstName, lastName, ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8")));
        Person savedPerson1 = repository.save(person1);
        Person savedPerson2 = repository.save(person2);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }




}
