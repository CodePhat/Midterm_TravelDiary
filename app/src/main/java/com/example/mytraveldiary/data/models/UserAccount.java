package com.example.mytraveldiary.data.models;

import java.io.Serializable;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String email;
    private final String password;

    public UserAccount(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}