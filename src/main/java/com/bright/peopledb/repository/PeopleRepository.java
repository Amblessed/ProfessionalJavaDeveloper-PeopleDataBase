package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.model.Person;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class PeopleRepository {

    public static final String SAVE_PERSON_SQL = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB) VALUES(?, ?, ?)";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PERSON WHERE ID=?";
    public static final String GET_COUNT_SQL = "SELECT COUNT(*) FROM PERSON";
    public static final String FIND_ALL_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PERSON";
    public static final String DELETE_ONE_SQL = "DELETE FROM PERSON WHERE ID=?";
    public static final String DELETE_MANY_SQL = "DELETE FROM PERSON WHERE ID IN (:ids)";
    public static final String UPDATE_SQL = "UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";
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
            preparedStatement.setTimestamp(3, covertDobToTimestamp(person.getDateOfBirth()));
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

    @NotNull
    private static Timestamp covertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }


    /**
     * @param id of the user to find
     * @return the person if found else null
     */
    public Optional<Person> findByID(Long id) {
        Person foundPerson = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                foundPerson = extractEntityFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(foundPerson);
    }


    /**
     * @return the number of users in the database
     */
    public long count(){
        long total = 0;
        try(PreparedStatement ps = connection.prepareStatement(GET_COUNT_SQL)){
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()){
                total = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * @return all the users in the database.
     */
    public List<Person> findAll() {
        List<Person> entities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_SQL)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entities;
    }

    /**
     * @param entity the user to  be deleted from the database
     */
    public void delete(Person entity) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_ONE_SQL)){
            prepareStatement.setLong(1, entity.getId());
            int affectedRecordCount = prepareStatement.executeUpdate();
            System.out.println("affectedRecordCount: " + affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param people the list of users to be deleted from the database
     */
    public void delete(Person... people) {
       /* "Another way to:for....(Person person: people){delete(person);}" */
        try(Statement statement = connection.createStatement()) {
            String ids = Arrays.stream(people).map(Person::getId).map(String::valueOf).collect(joining(","));
            int affectedRecordCount = statement.executeUpdate(DELETE_MANY_SQL.replace(":ids", ids));
            System.out.printf("Affected Records Count: %d%n", affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param person the user whose details are to be updated in the database.
     */
    public void update(Person person) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1,person.getFirstName());
            preparedStatement.setString(2,person.getLastName());
            preparedStatement.setTimestamp(3, covertDobToTimestamp(person.getDateOfBirth()));
            preparedStatement.setBigDecimal(4, person.getSalary());
            preparedStatement.setLong(5, person.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    Person extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long personID = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        return new Person(personID, firstName, lastName, dob, salary);
    }
}
