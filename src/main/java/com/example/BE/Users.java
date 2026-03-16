package com.example.BE;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String role;

    public Users() {}

    public Long getId()               { return id; }

    public String getUsername()       { return username; }
    public void setUsername(String u) { this.username = u; }

    public String getEmail()          { return email; }
    public void setEmail(String e)    { this.email = e; }

    public String getPassword()       { return password; }
    public void setPassword(String p) { this.password = p; }

    public String getRole()           { return role; }
    public void setRole(String r)     { this.role = r; }
}
