package com.bright.peopledb.utilities;

/*
 * @Project Name: ProfessionalJavaDeveloper-PeopleDataBase
 * @Author: Okechukwu Bright Onwumere
 * @Created: 21/10/2022
 */


import com.github.javafaker.Address;
import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RandomAddress {

    private RandomAddress(){}

    public static Map<String, String> getRandomAddress(){
        Address address = new Faker(Locale.US).address();
        HashMap<String, String> homeAddress = new HashMap<>();
        homeAddress.putIfAbsent("strAdd", address.streetAddress());
        homeAddress.putIfAbsent("secAdd", address.secondaryAddress());
        homeAddress.putIfAbsent("city", address.city());
        String state = address.stateAbbr();
        homeAddress.putIfAbsent("state", state);
        homeAddress.putIfAbsent("zipCode", address.zipCodeByState(state));
        homeAddress.putIfAbsent("country", "United States");
        homeAddress.putIfAbsent("county", "Fulton County");
        return homeAddress;
    }
}
