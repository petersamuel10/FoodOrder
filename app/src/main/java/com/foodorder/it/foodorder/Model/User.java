package com.foodorder.it.foodorder.Model;

public class User {

    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;

    public User() {
    }

    public User(String name, String password,String isStaff) {
        Name = name;
        Password = password;
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

}
