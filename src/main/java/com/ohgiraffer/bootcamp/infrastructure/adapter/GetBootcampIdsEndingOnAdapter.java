package com.ohgiraffer.bootcamp.infrastructure.adapter;

import com.ohgiraffer.bootcamp.infrastructure.persistence.SpringDataBootcampRepository;
import com.ohgiraffer.user.application.port.GetBootcampIdsEndingOnPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetBootcampIdsEndingOnAdapter implements GetBootcampIdsEndingOnPort {

    private final SpringDataBootcampRepository springDataBootcampRepository;

    @Override
    public List<Long> findBootcampIdsEndingOn(LocalDate date) {
        return springDataBootcampRepository.findIdsByEndDate(date);
    }
}