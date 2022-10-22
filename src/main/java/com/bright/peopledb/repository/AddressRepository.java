package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 21/10/2022
 */


import com.bright.peopledb.annotation.SQL;
import com.bright.peopledb.enums.Region;
import com.bright.peopledb.model.Address;
import com.bright.peopledb.model.CrudOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressRepository extends CrudRepository<Address> {

    public static final String SAVE_ADDRESS_SQL = """
    INSERT INTO ADDRESSES (STREET_ADDRESS, ADDRESS2, CITY, STATE, POSTCODE, COUNTY, REGION, COUNTRY)
    VALUES(?, ?, ?, ?, ?, ?, ?, ?)""";

    public static final String FIND_ADDRESS_SQL = """
    SELECT ID, STREET_ADDRESS, ADDRESS2, CITY, STATE, POSTCODE, COUNTY, REGION, COUNTRY
    FROM ADDRESSES WHERE ID = ?""";

    protected AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(crudOperation = CrudOperation.FIND_BY_ID, value = FIND_ADDRESS_SQL)
    Address extractEntityFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("ID");
        String streetAddress = resultSet.getString("STREET_ADDRESS");
        String address2 = resultSet.getString("ADDRESS2");
        String city = resultSet.getString("CITY");
        String state = resultSet.getString("STATE");
        String postcode = resultSet.getString("POSTCODE");
        String county = resultSet.getString("COUNTY");
        Region region = Region.valueOf(resultSet.getString("REGION").toUpperCase());
        String country = resultSet.getString("COUNTRY");
        return new Address(id, streetAddress, address2, city, state, postcode, country, county, region);
    }

    @Override
    @SQL(crudOperation = CrudOperation.SAVE, value = SAVE_ADDRESS_SQL)
    protected void mapForSave(Address address, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, address.streetAddress());
        preparedStatement.setString(2, address.address2());
        preparedStatement.setString(3, address.city());
        preparedStatement.setString(4,address.state());
        preparedStatement.setString(5,address.postcode());
        preparedStatement.setString(6,address.county());
        preparedStatement.setString(7,address.region().toString());
        preparedStatement.setString(8,address.country());

    }

    @Override
    protected void mapForUpdate(Address entity, PreparedStatement preparedStatement) {

    }
}
