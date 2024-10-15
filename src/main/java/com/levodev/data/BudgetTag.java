package com.levodev.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

@Entity
public class BudgetTag extends AbstractEntity {

    @Lob
    @Column(length = 1000000)
    private byte[] icon;
    private String name;
    private Integer budget;

    public byte[] getIcon() {
        return icon;
    }
    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getBudget() {
        return budget;
    }
    public void setBudget(Integer budget) {
        this.budget = budget;
    }

}
