package com.ohgiraffer.space.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.space.application.command.CreateSpaceCommand;
import com.ohgiraffer.space.application.usecase.CreateSpaceResult;
import com.ohgiraffer.space.application.usecase.CreateSpaceUseCase;
import com.ohgiraffer.space.domain.model.Space;
import com.ohgiraffer.space.domain.repository.SpaceRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSpaceService
        implements CreateSpaceUseCase {

    private final SpaceRepository spaceRepository;

    @Override
    @Transactional
    public CreateSpaceResult create(
            CreateSpaceCommand command,
            Long requesterId,
            Role requesterRole
    ) {
        validateManagementAuthority(
                requesterId,
                requesterRole
        );

        String normalizedName =
                normalizeName(command.name());

        if (spaceRepository.existsByName(
                normalizedName
        )) {
            throw new BusinessException(
                    ErrorCode.SPACE_NAME_DUPLICATED
            );
        }

        Space space = Space.create(
                normalizedName,
                command.capacity()
        );

        Space savedSpace =
                spaceRepository.save(space);

        return CreateSpaceResult.from(savedSpace);
    }

    private String normalizeName(
            String name
    ) {
        if (name == null) {
            return null;
        }

        return name.trim();
    }

    private void validateManagementAuthority(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SPACE_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.SPACE_ACCESS_DENIED
            );
        }
    }
}