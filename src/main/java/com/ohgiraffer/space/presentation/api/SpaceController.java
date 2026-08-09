package com.ohgiraffer.space.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.space.application.command.CreateSpaceCommand;
import com.ohgiraffer.space.application.command.UpdateMyLocationCommand;
import com.ohgiraffer.space.application.usecase.CreateSpaceResult;
import com.ohgiraffer.space.application.usecase.CreateSpaceUseCase;
import com.ohgiraffer.space.application.usecase.DeleteSpaceUseCase;
import com.ohgiraffer.space.application.usecase.GetSpaceStatusUseCase;
import com.ohgiraffer.space.application.usecase.MyLocationResult;
import com.ohgiraffer.space.application.usecase.SpaceStatusResult;
import com.ohgiraffer.space.application.usecase.UpdateMyLocationUseCase;
import com.ohgiraffer.space.presentation.api.request.CreateSpaceRequest;
import com.ohgiraffer.space.presentation.api.request.UpdateMyLocationRequest;
import com.ohgiraffer.space.presentation.api.response.CreateSpaceResponse;
import com.ohgiraffer.space.presentation.api.response.MyLocationResponse;
import com.ohgiraffer.space.presentation.api.response.SpaceStatusListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final GetSpaceStatusUseCase
            getSpaceStatusUseCase;

    private final CreateSpaceUseCase
            createSpaceUseCase;

    private final DeleteSpaceUseCase
            deleteSpaceUseCase;

    private final UpdateMyLocationUseCase
            updateMyLocationUseCase;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')"
    )
    public ResponseEntity<SpaceStatusListResponse>
    getSpaceStatuses(
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        List<SpaceStatusResult> results =
                getSpaceStatusUseCase.getSpaceStatuses(
                        principal.getId()
                );

        return ResponseEntity.ok(
                SpaceStatusListResponse.from(results)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<CreateSpaceResponse>
    createSpace(
            @Valid
            @RequestBody
            CreateSpaceRequest request,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        CreateSpaceCommand command =
                request.toCommand();

        CreateSpaceResult result =
                createSpaceUseCase.create(
                        command,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CreateSpaceResponse.from(result)
                );
    }

    @DeleteMapping("/{spaceId}")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<Void>
    deleteSpace(
            @PathVariable
            Long spaceId,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        deleteSpaceUseCase.delete(
                spaceId,
                principal.getId(),
                principal.getRole()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/my-location")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')"
    )
    public ResponseEntity<MyLocationResponse>
    updateMyLocation(
            @RequestBody
            UpdateMyLocationRequest request,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        UpdateMyLocationCommand command =
                request.toCommand();

        MyLocationResult result =
                updateMyLocationUseCase.update(
                        command,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                MyLocationResponse.from(result)
        );
    }
}