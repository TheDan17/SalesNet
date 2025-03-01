package com.thedan17.sales_net.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String secondName;
    private String type; // it should be not String in future

    public Account(Long id, String firstName, String secondName, String type){
        this.id = id;
        this.firstName = firstName;
        this.secondName = secondName;
        this.type = type;
    }
    public Account() {}

    public void setId(Long id) {this.id = id;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public void setSecondName(String secondName) {this.secondName = secondName;}
    public void setType(String type) {this.type = type;}

    public Long getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getSecondName() {
        return secondName;
    }
    public String getType() {
        return type;
    }
}
