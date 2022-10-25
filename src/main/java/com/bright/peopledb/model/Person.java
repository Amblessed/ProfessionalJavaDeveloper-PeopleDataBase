package com.bright.peopledb.model;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 14/10/2022
 */


import com.bright.peopledb.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@ToString
public class Person {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private ZonedDateTime dateOfBirth;
    @Setter
    private BigDecimal salary = new BigDecimal("0");

    private String email;
    private Optional<Address> homeAddress = Optional.empty();
    private Optional<Address> businessAddress = Optional.empty();

    private Set<Person> children = new HashSet<>();
    private Optional<Person> parent = Optional.empty();

    public Person(Long id, String firstName, String lastName, ZonedDateTime dob, BigDecimal salary) {
        this(id, firstName, lastName, dob);
        this.salary = salary;
    }

    public Person(Long id, String firstName, String lastName, ZonedDateTime dateOfBirth) {
        this(firstName, lastName, dateOfBirth);
        this.id = id;
    }

    public Person(String firstName, String lastName, ZonedDateTime dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public Long getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && firstName.equals(person.firstName) && lastName.equals(person.lastName) &&
                dateOfBirth.withZoneSameInstant(ZoneId.of("+0")).equals(person.dateOfBirth.withZoneSameInstant(ZoneId.of("+0")));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, dateOfBirth);
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = Optional.ofNullable(homeAddress);
    }

    public Optional<Address> getHomeAddress() {
        return homeAddress;
    }

    public void setBusinessAddress(Address businessAddress) {
        this.businessAddress = Optional.ofNullable(businessAddress);
    }

    public Optional<Address> getBusinessAddress() {
        return businessAddress;
    }

    public void addChild(Person child) {
        children.add(child);
        child.setParent(this);
    }

    public void setParent(Person parent) {
        this.parent = Optional.ofNullable(parent);
    }

    public Optional<Person> getParent() {
        return parent;
    }

//    public Set<Person> getChildren() {
//        return children;
//    }
}
