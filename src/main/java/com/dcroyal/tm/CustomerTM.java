package com.dcroyal.tm;

/**
 * CustomerTM.java
 * Table Model class used to display customer records in JavaFX TableView.
 */
public class CustomerTM {

    private String customerCode;
    private String fullName;
    private String phone;
    private String email;
    private String city;
    private String address;

    public CustomerTM() {
    }

    public CustomerTM(String customerCode, String fullName, String phone, String email, String city, String address) {
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.address = address;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
