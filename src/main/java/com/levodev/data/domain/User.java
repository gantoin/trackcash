package com.levodev.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "application_user")
@Getter
@Setter
public class User extends AbstractEntity {

    private String username;

    private String name;

    @JsonIgnore
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<RoleEnum> roleEnums;

    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    @Email
    private String email;

    @OneToMany
    private Set<Transaction> transactions = new HashSet<>();

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", profilePicture=" + Arrays.toString(profilePicture) +
                ", roleEnums=" + roleEnums +
                ", hashedPassword='" + hashedPassword + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
