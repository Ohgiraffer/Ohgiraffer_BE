package com.ohgiraffer.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        if (deletedAt == null) {
            deletedAt = Instant.now();
        }
    }

    public void restore() {
        deletedAt = null;
    }
}