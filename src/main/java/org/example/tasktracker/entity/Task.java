package org.example.tasktracker.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name="tasks")
@Getter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    String name;

    public void setName(String name) {
        this.name = name;
    }

}
