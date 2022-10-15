package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.model.Person;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PeopleRepository {

    public static final String SAVE_PERSON_SQL = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB) VALUES(?, ?, ?)";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB FROM PERSON WHERE ID=?";
    private final Connection connection;

    public PeopleRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param person The person to be saved inside the database
     * @return The saved person in the database
     */
    public Person save(Person person){
        try(PreparedStatement preparedStatement = connection.prepareStatement(SAVE_PERSON_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,person.getFirstName());
            preparedStatement.setString(2,person.getLastName());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(person.getDateOfBirth().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));
            int recordsAffected = preparedStatement.executeUpdate(); //to execute the query
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()){
                long id = resultSet.getLong(1);
                person.setId(id);
                System.out.println(person);
            }
            System.out.printf("Records affected: %d%n", recordsAffected);
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return person;
    }


    public Person findByID(Long id) {
        Person foundPerson = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                long personID = resultSet.getLong("ID");
                String firstName = resultSet.getString("FIRST_NAME");
                String lastName = resultSet.getString("LAST_NAME");
                ZonedDateTime dob = ZonedDateTime.of(resultSet.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
                foundPerson = new Person(firstName, lastName, dob);
                foundPerson.setId(personID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundPerson;
    }
}
