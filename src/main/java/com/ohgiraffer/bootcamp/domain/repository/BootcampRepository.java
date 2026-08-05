package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.Bootcamp;

import java.util.Optional;

public interface BootcampRepository {
    Bootcamp save(Bootcamp target);
    Optional<Bootcamp> findById(Long id);
}
