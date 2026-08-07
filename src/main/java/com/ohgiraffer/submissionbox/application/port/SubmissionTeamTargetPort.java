package com.ohgiraffer.submissionbox.application.port;

import java.util.List;

public interface SubmissionTeamTargetPort {

    List<TeamSubmissionTarget> findActiveTeams();
}