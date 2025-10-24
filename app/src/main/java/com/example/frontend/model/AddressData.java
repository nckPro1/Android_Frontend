package com.example.frontend.model;

import java.util.List;

public class AddressData {
    private List<String> cities;
    private List<String> districts;
    private List<String> wards;

    public AddressData() {}

    public AddressData(List<String> cities, List<String> districts, List<String> wards) {
        this.cities = cities;
        this.districts = districts;
        this.wards = wards;
    }

    // Getters and Setters
    public List<String> getCities() { return cities; }
    public void setCities(List<String> cities) { this.cities = cities; }

    public List<String> getDistricts() { return districts; }
    public void setDistricts(List<String> districts) { this.districts = districts; }

    public List<String> getWards() { return wards; }
    public void setWards(List<String> wards) { this.wards = wards; }
}
