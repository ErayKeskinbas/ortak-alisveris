package com.eray.ortakalisveris;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String quantity;

    @Column(nullable = false)
    private boolean completed;

    protected Product() {
    }

    public Product(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}