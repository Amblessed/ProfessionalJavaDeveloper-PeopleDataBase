package com.bright.peopledb.repository;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 18/10/2022
 */


import com.bright.peopledb.annotation.Id;
import com.bright.peopledb.annotation.MultiSQL;
import com.bright.peopledb.annotation.SQL;
import com.bright.peopledb.model.CrudOperation;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

abstract class CRUDRepository<T> {

    protected final Connection connection;
    private final String sqlStatementNotDefined = "SQL Statement not defined";

    protected CRUDRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param entity The person to be saved inside the database
     * @return The saved person in the database
     */
    public T save(T entity){
        try(PreparedStatement preparedStatement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.SAVE, this::getSaveSQL), Statement.RETURN_GENERATED_KEYS)) {
            mapForSave(entity, preparedStatement);
            int recordsAffected = preparedStatement.executeUpdate(); //to execute the query
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()){
                long id = resultSet.getLong(1);
                setIdByAnnotation(id, entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    /**
     * @param id of the user to find
     * @return the person if found else null
     */
    public Optional<T> findByID(Long id) {
        T entity = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_BY_ID, this::getFindByIDSql))) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                entity = extractEntityFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    /**
     * @return Returns all the entities in the database.
     */
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_ALL, this::getFindAllSql))){
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
     * @return Returns the number of entities in the database
     */
    public long count(){
        long total = 0;
        try(PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.COUNT, this::getCountSql))){
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
     * @param entity the entity to be deleted from the database
     */
    public void delete(T entity) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.DELETE_ONE, this::getDeleteSql))){
            prepareStatement.setLong(1, findIdByAnnotation(entity));
            int affectedRecordCount = prepareStatement.executeUpdate();
            System.out.println("affectedRecordCount: " + affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param entities the list of users to be deleted from the database
     */
    @SafeVarargs
    public final void delete(T... entities) {
        try(Statement statement = connection.createStatement()) {
            String ids = Arrays.stream(entities)
                    .map(this::findIdByAnnotation)
                    .map(String::valueOf)
                    .collect(joining(","));
            int affectedRecordCount = statement.executeUpdate(getSqlByAnnotation(CrudOperation.DELETE_MANY, this::getDeleteManySql).replace(":ids", ids));
            System.out.printf("Affected Records Count: %d%n", affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param entity the user whose details are to be updated in the database.
     */
    public void update(T entity) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.UPDATE, this::getUpdateSql))) {
            mapForUpdate(entity, preparedStatement);
            preparedStatement.setLong(5, findIdByAnnotation(entity));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void alterTable() {
        try(PreparedStatement preparedStatement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.ALTER, this::getAlterTableSql))) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSqlByAnnotation(CrudOperation crudOperation, Supplier<String> sqlGetter){
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));

        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));

        return Stream.concat(multiSqlStream, sqlStream)
                .filter(a -> a.crudOperation().equals(crudOperation))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);
    }

    private Long findIdByAnnotation(T entity){
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(f -> {
                    f.setAccessible(true);  // to get the value of id since it is private
                    long id = -888L;
                    try {
                        id = (long) f.get(entity);
                    } catch (IllegalAccessException e) {
                       e.printStackTrace();
                    }
                    return id;
                })
                .findFirst().orElseThrow(() -> new RuntimeException("No ID annotated field found"));
    }

    private void setIdByAnnotation(Long id, T entity) {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .forEach(f -> {
                    f.setAccessible(true); //Not a good practice though to change visibility of field via reflection
                    try {
                        f.set(entity, id);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to set ID field value");
                    }
                });
    }


    protected String getSaveSQL() { throw new IllegalArgumentException(sqlStatementNotDefined);}

    /**
     * @return A String that represents the SQL statement needed to
     * retrieve one entity from the database. The SQL statement must contain
     * one SQL parameter, i.e "?", that will bind to the entity's ID.
     */
    protected String getFindByIDSql(){ throw new IllegalArgumentException(sqlStatementNotDefined);}

    protected String getUpdateSql(){ throw new IllegalArgumentException(sqlStatementNotDefined);}
    protected String getAlterTableSql(){ throw new IllegalArgumentException(sqlStatementNotDefined);}

    /**
     * @return A SQL statement for deleting multiple entities
     * from the database. The SQL statement should be like:
     * "DELETE FROM *TABLENAME* WHERE ID IN (:ids)"
     * Be sure to include the '(:ids)' named parameter and call it 'ids'
     */
    protected String getDeleteManySql() { throw new IllegalArgumentException(sqlStatementNotDefined);}

    /**
     * @return The SQL Statement for deleting an entity
     * in the database
     */
    protected String getDeleteSql() { throw new IllegalArgumentException(sqlStatementNotDefined);}

    /**
     * @return The SQL Statement to for getting the count
     * of the entities in the database
     */
    protected String getCountSql() { throw new IllegalArgumentException(sqlStatementNotDefined);}

    /**
     * @return The SQL Statement for getting all entities in the database
     */
    protected String getFindAllSql() { throw new IllegalArgumentException(sqlStatementNotDefined);}

    abstract T extractEntityFromResultSet(ResultSet resultSet) throws SQLException;

    protected abstract void mapForSave(T entity, PreparedStatement preparedStatement) throws SQLException;
    protected abstract void mapForUpdate(T entity, PreparedStatement preparedStatement) throws SQLException;

}
