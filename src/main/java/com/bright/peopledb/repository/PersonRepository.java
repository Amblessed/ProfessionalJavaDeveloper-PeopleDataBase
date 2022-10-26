package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.annotation.SQL;
import com.bright.peopledb.enums.Region;
import com.bright.peopledb.model.Address;
import com.bright.peopledb.model.CrudOperation;
import com.bright.peopledb.model.Person;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class PersonRepository extends CrudRepository<Person> {

    public static final String SAVE_PERSON_SQL = """
    INSERT INTO PERSON
    (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS, BUSINESS_ADDRESS, PARENT_ID)
    VALUES(?, ?, ?, ?, ?, ?, ?, ?)""";
    public static final String FIND_BY_ID_SQL = """
    SELECT PARENT.ID AS PARENT_ID, PARENT.FIRST_NAME AS PARENT_FIRST_NAME, PARENT.LAST_NAME AS PARENT_LAST_NAME, PARENT.DOB AS PARENT_DOB, PARENT.SALARY AS PARENT_SALARY, PARENT.EMAIL AS PARENT_EMAIL,
    CHILD.ID AS CHILD_ID, CHILD.FIRST_NAME AS CHILD_FIRST_NAME, CHILD.LAST_NAME AS CHILD_LAST_NAME, CHILD.DOB AS CHILD_DOB, CHILD.SALARY AS CHILD_SALARY, CHILD.EMAIL AS CHILD_EMAIL,
    HOME.ID AS HOME_ID, HOME.STREET_ADDRESS AS HOME_STREET_ADDRESS, HOME.ADDRESS2 AS HOME_ADDRESS2, HOME.CITY AS HOME_CITY, HOME.STATE AS HOME_STATE, HOME.POSTCODE AS HOME_POSTCODE, HOME.COUNTY AS HOME_COUNTY, HOME.REGION AS HOME_REGION, HOME.COUNTRY AS HOME_COUNTRY,
    BUSINESS.ID AS BUSINESS_ID, BUSINESS.STREET_ADDRESS AS BUSINESS_STREET_ADDRESS, BUSINESS.ADDRESS2 AS BUSINESS_ADDRESS2, BUSINESS.CITY AS BUSINESS_CITY, BUSINESS.STATE AS BUSINESS_STATE, BUSINESS.POSTCODE AS BUSINESS_POSTCODE, BUSINESS.COUNTY AS BUSINESS_COUNTY, BUSINESS.REGION AS BUSINESS_REGION, BUSINESS.COUNTRY AS BUSINESS_COUNTRY
    FROM PERSON AS PARENT
    LEFT OUTER JOIN PERSON AS CHILD ON PARENT.ID = CHILD.PARENT_ID
    LEFT OUTER JOIN ADDRESSES AS HOME ON PARENT.HOME_ADDRESS = HOME.ID
    LEFT OUTER JOIN ADDRESSES AS BUSINESS ON PARENT.BUSINESS_ADDRESS = BUSINESS.ID
    WHERE PARENT.ID = ?""";
    public static final String GET_COUNT_SQL = "SELECT COUNT(*) FROM PERSON";
    public static final String FIND_ALL_SQL = """
    SELECT PARENT.ID AS PARENT_ID, PARENT.FIRST_NAME AS PARENT_FIRST_NAME, PARENT.LAST_NAME AS PARENT_LAST_NAME, PARENT.DOB AS PARENT_DOB, PARENT.SALARY AS PARENT_SALARY, PARENT.EMAIL AS PARENT_EMAIL
    FROM PERSON AS PARENT
    FETCH FIRST 100 ROWS ONLY
    """;
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
        preparedStatement.setString(1,entity.getFirstName());
        preparedStatement.setString(2,entity.getLastName());
        preparedStatement.setTimestamp(3, covertDobToTimestamp(entity.getDateOfBirth()));
        preparedStatement.setBigDecimal(4,entity.getSalary());
        preparedStatement.setString(5,entity.getEmail());
        associateAddressWithPerson(preparedStatement, entity.getHomeAddress(), 6);
        associateAddressWithPerson(preparedStatement, entity.getBusinessAddress(), 7);
        Optional<Person> parent = entity.getParent();
        associateChildWithPerson(preparedStatement, parent);
    }

    private static void associateChildWithPerson(PreparedStatement preparedStatement, Optional<Person> parent) throws SQLException {
        if(parent.isPresent()){
            preparedStatement.setLong(8, parent.get().getId());
        }
        else{
            preparedStatement.setObject(8, null);
        }
    }

    @Override
    protected void postSave(Person entity, long id) {
       entity.getChildren()
               .forEach(this::save);
    }

    private void associateAddressWithPerson(PreparedStatement preparedStatement, Optional<Address> address, int parameterIndex) throws SQLException {
        Address savedAddress;
        if (address.isPresent()) {
            savedAddress = addressRepository.save(address.get());
            preparedStatement.setLong(parameterIndex, savedAddress.id());
        }
        else {
            preparedStatement.setObject(parameterIndex, null);
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
        Person finalParent = null;
        do {
            Person currentParent = extractPerson(rs, "PARENT_").get();
            if (Objects.isNull(finalParent)) {
                finalParent = currentParent;
            }
            if (!finalParent.equals(currentParent)) {
                rs.previous();
                break;
            }
            Optional<Person> child = extractPerson(rs, "CHILD_");
            finalParent.setHomeAddress(extractAddress(rs, "HOME_"));
            finalParent.setBusinessAddress(extractAddress(rs, "BUSINESS_"));
            child.ifPresent(finalParent::addChild);
            //finalParent.addChild(child);
        } while (rs.next());
        return finalParent;
    }

    /**
     * @param rs Result Set
     * @param aliasPrefix Alias Prefix for the column names
     * @return A new Person from the result set
     * @throws SQLException thrown
     */
    @NotNull
    private Optional<Person> extractPerson(ResultSet rs, String aliasPrefix) throws SQLException {
        Long personID = getValueByAlias( aliasPrefix + "ID", rs, Long.class);
        if (Objects.isNull(personID)) {
            return Optional.empty();
        }
        String firstName = getValueByAlias(aliasPrefix + "FIRST_NAME", rs, String.class);
        String lastName = getValueByAlias(aliasPrefix + "LAST_NAME", rs, String.class);
        ZonedDateTime dob = ZonedDateTime.of(getValueByAlias(aliasPrefix + "DOB",rs, Timestamp.class).toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = getValueByAlias(aliasPrefix + "SALARY", rs, BigDecimal.class);
        return Optional.of(new Person(personID, firstName, lastName, dob, salary));
    }

    /**
     * @return The SQL Statement needed to find an entry in
     * the database by its ID.
     */
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

    private Address extractAddress(ResultSet resultSet, String aliasPrefix) throws SQLException {
        Long addressId = getValueByAlias(aliasPrefix + "ID", resultSet, Long.class);
        if(Objects.isNull(addressId)){
            return null;
        }
        //long addressId = resultSet.getLong("A_ID");
        String streetAddress = getValueByAlias(aliasPrefix + "STREET_ADDRESS",resultSet, String.class);
        String address2 = getValueByAlias(aliasPrefix + "ADDRESS2",resultSet, String.class);
        String city = getValueByAlias(aliasPrefix + "CITY",resultSet, String.class);
        String state = getValueByAlias(aliasPrefix + "STATE",resultSet, String.class);
        String postcode = getValueByAlias(aliasPrefix + "POSTCODE",resultSet, String.class);
        String county = getValueByAlias(aliasPrefix + "COUNTY",resultSet, String.class);
        Region region = Region.valueOf(getValueByAlias(aliasPrefix + "REGION",resultSet, String.class).toUpperCase());
        String country = getValueByAlias(aliasPrefix + "COUNTRY",resultSet, String.class);
        return new Address(addressId, streetAddress, address2, city, state, postcode, country, county, region);
    }

    private <T> T getValueByAlias(String columnAlias, ResultSet resultSet, Class<T> clazz) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        for(int colIdx=1; colIdx<=columnCount; colIdx++){
            if(columnAlias.equals(resultSet.getMetaData().getColumnLabel(colIdx))){
                return (T) resultSet.getObject(colIdx);
            }
        }
        return null;
    }

}
