package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;
import com.ohgiraffer.approval.application.usecase.GetMyApprovalProfileUseCase;
import com.ohgiraffer.approval.application.usecase.UpdateMyApprovalProfileUseCase;
import com.ohgiraffer.approval.presentation.api.request.UpdateApprovalProfileRequest;
import com.ohgiraffer.approval.presentation.api.response.ApprovalProfileResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/approval-profile")
@RequiredArgsConstructor
public class ApprovalProfileController {

    private final GetMyApprovalProfileUseCase getMyApprovalProfileUseCase;
    private final UpdateMyApprovalProfileUseCase updateMyApprovalProfileUseCase;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApprovalProfileResponse getMyApprovalProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ApprovalProfileResult result =
                getMyApprovalProfileUseCase.getProfile(
                        principal.getId()
                );

        return ApprovalProfileResponse.from(
                result
        );
    }

    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApprovalProfileResponse updateMyApprovalProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateApprovalProfileRequest request
    ) {
        ApprovalProfileResult result =
                updateMyApprovalProfileUseCase.updateProfile(
                        principal.getId(),
                        request.birthDate()
                );

        return ApprovalProfileResponse.from(
                result
        );
    }
}