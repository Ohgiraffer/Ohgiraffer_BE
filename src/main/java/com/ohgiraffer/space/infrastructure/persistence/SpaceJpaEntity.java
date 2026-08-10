package com.ohgiraffer.space.infrastructure.persistence;

import com.ohgiraffer.space.domain.model.Space;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "space_reservation")
public class SpaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long id;

    @Column(
            name = "space_name",
            nullable = false,
            length = 100,
            unique = true
    )
    private String name;

    @Column(
            name = "max_capacity",
            nullable = false
    )
    private int capacity;

    protected SpaceJpaEntity() {
    }

    private SpaceJpaEntity(
            Long id,
            String name,
            int capacity
    ) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public static SpaceJpaEntity from(
            Space space
    ) {
        return new SpaceJpaEntity(
                space.getId(),
                space.getName(),
                space.getCapacity()
        );
    }

    public Space toDomain() {
        return Space.restore(
                id,
                name,
                capacity
        );
    }
}