package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submission.application.command.CreateSubmissionCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CreateSubmissionUseCase {

    CreateSubmissionResult create(
            CreateSubmissionCommand command,
            List<MultipartFile> files
    );
}