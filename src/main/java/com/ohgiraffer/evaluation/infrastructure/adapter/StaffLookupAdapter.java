package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.application.port.StaffLookupPort;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StaffLookupAdapter implements StaffLookupPort {

    private final UserRepository userRepository;

    public StaffLookupAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Long> findActiveStaffIds() {
        List<Long> staffIds = new ArrayList<>();

        for (Role role : List.of(Role.INSTRUCTOR, Role.MANAGER)) {
            userRepository.findAllByRoleAndStatus(role, UserStatus.ACTIVE)
                    .stream()
                    .map(User::getId)
                    .forEach(staffIds::add);
        }

        return staffIds;
    }
}
