package com.eray.ortakalisveris;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 32)
    private String inviteCode;

    protected Household() {
    }

    public Household(String name) {
        this.name = name;
        this.inviteCode = UUID.randomUUID()
            .toString()
            .replace("-", "");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInviteCode() {
        return inviteCode;
    }
}