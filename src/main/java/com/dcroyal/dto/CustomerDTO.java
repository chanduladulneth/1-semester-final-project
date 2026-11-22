package com.dcroyal.dto;

/**
 * CustomerDTO.java
 * Data Transfer Object representing a Customer.
 */
public class CustomerDTO {

    private int customerId;
    private String customerCode;
    private String fullName;
    private String phone;
    private String username;
    private String address;
    private String city;
    private int createdBy;

    public CustomerDTO() {
    }

    public CustomerDTO(int customerId, String customerCode, String fullName, String phone, String username, String address, String city, int createdBy) {
        this.customerId = customerId;
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.phone = phone;
        this.username = username;
        this.address = address;
        this.city = city;
        this.createdBy = createdBy;
    }

    public CustomerDTO(String customerCode, String fullName, String phone, String username, String address, String city, int createdBy) {
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.phone = phone;
        this.username = username;
        this.address = address;
        this.city = city;
        this.createdBy = createdBy;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
