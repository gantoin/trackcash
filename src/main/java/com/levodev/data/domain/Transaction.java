package com.levodev.data.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction extends AbstractEntity {

    private String name;

    /**
     * Amount of the transaction, should be formatted as 0.00
     */
    private Float amount;

    private String currency;

    private LocalDateTime date = LocalDateTime.now();

    @ManyToOne
    private Location location;

    @ManyToOne
    private User user;

    @ManyToOne
    private BudgetTag budgetTag;

    @Override
    public String toString() {
        return "Transaction{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", location=" + location +
                ", user=" + user +
                ", budgetTag=" + budgetTag +
                '}';
    }
}
