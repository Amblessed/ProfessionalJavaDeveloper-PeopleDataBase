package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.annotation.SQL;
import com.bright.peopledb.model.Address;
import com.bright.peopledb.model.CrudOperation;
import com.bright.peopledb.model.Person;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PersonRepository extends CrudRepository<Person> {

    public static final String SAVE_PERSON_SQL = """
    INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS)
    VALUES(?, ?, ?, ?, ?, ?)""";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY, HOME_ADDRESS  FROM PERSON WHERE ID=?";
    public static final String GET_COUNT_SQL = "SELECT COUNT(*) FROM PERSON";
    public static final String FIND_ALL_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PERSON";
    public static final String DELETE_ONE_SQL = "DELETE FROM PERSON WHERE ID=?";
    public static final String DELETE_MANY_SQL = "DELETE FROM PERSON WHERE ID IN (:ids)";
    public static final String UPDATE_SQL = "UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";
    public static final String ALTER_TABLE_SQL = "ALTER TABLE PERSON ADD COLUMN EMAIL CHARACTER VARYING(255);";

    private AddressRepository addressRepository;

    public PersonRepository(Connection connection) {
        super(connection);
        addressRepository = new AddressRepository(connection);
    }

    /**
     * @param entity User to be added to the database
     * @param preparedStatement from the Repository
     * @throws SQLException thrown
     */
    @Override
    @SQL(value = SAVE_PERSON_SQL, crudOperation = CrudOperation.SAVE)
    public void mapForSave(Person entity, PreparedStatement preparedStatement) throws SQLException {
        Address savedAddress;
        preparedStatement.setString(1,entity.getFirstName());
        preparedStatement.setString(2,entity.getLastName());
        preparedStatement.setTimestamp(3, covertDobToTimestamp(entity.getDateOfBirth()));
        preparedStatement.setBigDecimal(4,entity.getSalary());
        preparedStatement.setString(5,entity.getEmail());
        if (entity.getHomeAddress().isPresent()) {
            savedAddress = addressRepository.save(entity.getHomeAddress().get());
            preparedStatement.setLong(6, savedAddress.id());
        }
        else {
            preparedStatement.setObject(6, null);
        }

    }

    @Override
    @SQL(value = UPDATE_SQL, crudOperation = CrudOperation.UPDATE)
    protected void mapForUpdate(Person entity, PreparedStatement preparedStatement) throws SQLException {
       preparedStatement.setString(1, entity.getFirstName());
       preparedStatement.setString(2, entity.getLastName());
       preparedStatement.setTimestamp(3, covertDobToTimestamp(entity.getDateOfBirth()));
       preparedStatement.setBigDecimal(4, entity.getSalary());
    }
    @Override
    @SQL(value = FIND_BY_ID_SQL, crudOperation = CrudOperation.FIND_BY_ID)
    @SQL(value= FIND_ALL_SQL, crudOperation = CrudOperation.FIND_ALL)
    @SQL(value= GET_COUNT_SQL, crudOperation = CrudOperation.COUNT)
    @SQL(value= DELETE_ONE_SQL, crudOperation = CrudOperation.DELETE_ONE)
    Person extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long personID = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        long homeAddressId = rs.getLong("HOME_ADDRESS");
        Optional<Address> homeAddress = addressRepository.findByID(homeAddressId);
        Person person = new Person(personID, firstName, lastName, dob, salary);
        person.setHomeAddress(homeAddress.orElse(null));
        return person;
    }

    @Override
    protected String getFindByIDSql() {
        return FIND_BY_ID_SQL;
    }

    @Override
    public String getFindAllSql() {
        return FIND_ALL_SQL;
    }

    @Override
    public String getCountSql() {
        return GET_COUNT_SQL;
    }

    @Override
    public String getDeleteSql() {
        return DELETE_ONE_SQL;
    }

    @Override
    protected String getDeleteManySql() {
        return DELETE_MANY_SQL;
    }

    @Override
    protected String getAlterTableSql() {
        return ALTER_TABLE_SQL;
    }

    /**
     * @param dob the date of birth
     * @return Timestamp from the dob entered
     */
    @NotNull
    private static Timestamp covertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }

}
