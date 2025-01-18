package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Size;

@Document(collection = "Users")
public class User {
    @Id
    private String id;

    @Size(min=3, max=20)
    private String name;

    @Indexed(unique = true, background = true)
    @Size(max=40)
    private String email;

    @Size(min=5, max=30)
    private String password;

    private float saldo;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public float getSaldo() {
        return saldo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}