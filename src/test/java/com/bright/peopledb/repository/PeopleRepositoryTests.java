package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.enums.Region;
import com.bright.peopledb.model.Address;
import com.bright.peopledb.model.Person;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.github.javafaker.Number;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.bright.peopledb.utilities.RandomAddress.getRandomAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PeopleRepositoryTests {

    private final Name name = new Faker().name();
    private final Number number = new Faker().number();
    private Connection connection;
    private String firstName;
    private String lastName;
    private PersonRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:~/peopletest".replace("~", System.getProperty("user.home")));
        connection.setAutoCommit(false); //changes made to the database are not saved as long as connection is open
        repository = new PersonRepository(connection);
        firstName = name.firstName();
        lastName = name.lastName();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if(Objects.nonNull(connection)){
            connection.close();
        }
    }

    @Test
    @DisplayName("Can save a single user in the Database")
    void canSaveOnePerson() {
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1980, 11,15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person savedPerson = repository.save(person);
        assertThat(savedPerson.getId()).isPositive();
    }

    @Test
    @DisplayName("Can save two users in the Database")
    void canSaveTwoPerson(){
        Person person1 = new Person(firstName, lastName, ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        firstName = name.firstName();
        lastName = name.lastName();
        Person person2 = new Person(firstName, lastName, ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8")));
        Person savedPerson1 = repository.save(person1);
        Person savedPerson2 = repository.save(person2);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }

    @Test
    @DisplayName("Can save a user with Address")
    void canSavePersonWithAddress() {
        System.out.println(firstName + " && " + lastName);
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Address address = new Address(null, getRandomAddress().get("strAdd"), getRandomAddress().get("secAdd"),
                getRandomAddress().get("city"), getRandomAddress().get("state"), getRandomAddress().get("zipCode"),
                getRandomAddress().get("country"), getRandomAddress().get("county"), Region.EAST);
        person.setHomeAddress(address);

        Person savedPerson = repository.save(person);
        assertThat(savedPerson.getHomeAddress().get().id()).isPositive();
        // connection.commit();
    }

    @Test
    @DisplayName("Can find a user by ID with Address")
    void canFindPersonByIdWithAddress() {
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        String state = getRandomAddress().get("state");
        System.out.println(state);
        Address address = new Address(null, getRandomAddress().get("strAdd"), getRandomAddress().get("secAdd"),
                getRandomAddress().get("city"), state, getRandomAddress().get("zipCode"),
                getRandomAddress().get("country"), getRandomAddress().get("county"), Region.EAST);
        person.setHomeAddress(address);
        Person savedPerson = repository.save(person);
        Person foundPerson = repository.findByID(savedPerson.getId()).get();
        assertThat(foundPerson.getHomeAddress().get().state()).isEqualTo(state);
        // connection.commit();
    }

    @Test
    @DisplayName("Can find a user in DB by ID")
    void canFindPersonByID(){
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8")));
        Person savedPerson = repository.save(person);
        Person foundPerson = repository.findByID(savedPerson.getId()).orElseThrow();
        assertThat(foundPerson).isEqualTo(savedPerson);
    }

    @Test
    @DisplayName("Find an ID not existing in the Database")
    void testPersonIdNotFound(){
        Optional<Person> person = repository.findByID(-1L);
        assertTrue(person.isEmpty());
        assertThat(person).isEmpty();
    }
    @Test
    @DisplayName("Can find all the users in the Database")
    @Disabled("Takes a long time to run")
    void canFindAll() {
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+6"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("-4"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+3"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+2"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("-1"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+4"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+2"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(number.numberBetween(1980, 1990), 11, 15, 15, 15, 0, 0, ZoneId.of("+8"))));

        List<Person> people = repository.findAll();
        assertThat(people).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Can count the number of users in the database")
    void canGetCount(){
        long startCount = repository.count();
        Person person = new Person(firstName, lastName, ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8")));
        repository.save(person);
        long endCount = repository.count();
        System.out.printf("StartCount: %d and EndCount: %d%n", startCount, endCount);
        assertThat(endCount).isEqualTo(startCount + 1);
    }

    @Test
    @DisplayName("Can delete a user from the database")
    void canDelete(){
        Person savedPerson = repository.save(new Person(firstName, lastName, ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8"))));
        long startCount = repository.count();
        repository.delete(savedPerson);
        long endCount = repository.count();
        assertThat(endCount).isEqualTo(startCount - 1);
    }

    @Test
    @DisplayName("Can delete multiple users from the database")
    void canDeleteMultiplePeople(){
        Person savedPerson = repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8"))));
        Person savedPerson2 = repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(1986, 10, 15, 12, 43, 0, 0, ZoneId.of("-8"))));
        long startCount = repository.count();
        repository.delete(savedPerson, savedPerson2);
        long endCount = repository.count();
        assertThat(endCount).isEqualTo(startCount - 2);
    }

    @Test
    @DisplayName("Can update user details in the database")
    void canUpdate(){
        Person savedPerson = repository.save(new Person(name.firstName(), name.lastName(), ZonedDateTime.of(1982, 9, 25, 13, 13, 0, 0, ZoneId.of("-8"))));
        Person foundPerson1 = null;
        Person foundPerson2 = null;
        System.out.println("savedPerson.getId(): " + savedPerson.getId());
        System.out.println(savedPerson);
        if(repository.findByID(savedPerson.getId()).isPresent()){
           foundPerson1 = repository.findByID(savedPerson.getId()).get();
        }
        savedPerson.setSalary(new BigDecimal("73000.28"));
        repository.update(savedPerson);
        if(repository.findByID(savedPerson.getId()).isPresent()){
            foundPerson2 = repository.findByID(savedPerson.getId()).get();
        }
        assertThat(foundPerson1).isNotNull();
        assertThat(foundPerson2).isNotNull();
        assertThat(foundPerson2.getSalary()).isNotEqualTo(foundPerson1.getSalary());
    }

    @Test
    @Disabled("Not need every time")
    @DisplayName("Can alter table in the database")
    void canAlterTable(){
        repository.alterTable();
    }

    @Test
    @Disabled("Data only needs to be load once into the database")
    void loadData() throws IOException, SQLException {
        long startTime = System.currentTimeMillis();
        Files.lines(Path.of("src/test/resources/Hr5m.csv"))
                .skip(1)
                //.limit(100)
                .map(line -> line.split(","))
                .map(arr -> {
                    LocalDate dateOfBirth = LocalDate.parse(arr[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime timeOfBirth = LocalTime.parse(arr[11].toLowerCase(), DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime localDateTime = LocalDateTime.of(dateOfBirth, timeOfBirth);
                    ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("+0"));
                    Person person = new Person(arr[2], arr[4], zonedDateTime);
                    person.setSalary(new BigDecimal(arr[25]));
                    person.setEmail(arr[6]);
                    return person;
                })
                .forEach(repository::save);
        connection.commit();
        long endTime = System.currentTimeMillis();
        System.out.printf("Execution Time: %s", endTime - startTime);
    }
}
