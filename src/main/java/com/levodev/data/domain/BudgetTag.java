package com.levodev.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class BudgetTag extends AbstractEntity {

    @Lob
    @Column(length = 1000000)
    private byte[] icon;

    private String name;

    private Integer budget;

    @OneToMany
    @JsonIgnore
    private Set<Transaction> transactions = new HashSet<>();

    @Override
    public String toString() {
        return "BudgetTag{" +
                "budget=" + budget +
                ", name='" + name + '\'' +
                ", icon=" + Arrays.toString(icon) +
                '}';
    }
}
