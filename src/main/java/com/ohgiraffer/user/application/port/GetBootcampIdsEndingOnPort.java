package com.ohgiraffer.user.application.port;

import java.time.LocalDate;
import java.util.List;

public interface GetBootcampIdsEndingOnPort {
    List<Long> findBootcampIdsEndingOn(LocalDate date);
}
