package com.ohgiraffer.attendance.infrastructure.adapter;

import com.ohgiraffer.attendance.application.port.GetBootcampStaffIdsPort;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GetBootcampStaffIdsAdapter implements GetBootcampStaffIdsPort {

    private final UserRepository userRepository;

    @Override
    public List<Long> findStaffIdsByBootcampId(Long bootcampId) {
        return Stream.concat(
                userRepository.findAllByRoleAndStatusAndBootcampId(Role.INSTRUCTOR, UserStatus.ACTIVE, bootcampId).stream(),
                userRepository.findAllByRoleAndStatusAndBootcampId(Role.MANAGER, UserStatus.ACTIVE, bootcampId).stream()
        ).map(u -> u.getId()).toList();
    }

}
