package com.ohgiraffer.team.application.service;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamSendbirdChannelDeleteService {

    private final SendbirdApiPort sendbirdApiPort;
    private final TeamSendbirdChannelMirrorDeleteService teamSendbirdChannelMirrorDeleteService;

    public void delete(
            String sendbirdChannelUrl
    ) {
        if (sendbirdChannelUrl == null
                || sendbirdChannelUrl.isBlank()) {
            return;
        }

        String channelUrl =
                sendbirdChannelUrl.trim();

        sendbirdApiPort.deleteChannel(
                channelUrl
        );

        teamSendbirdChannelMirrorDeleteService.deleteMirror(
                channelUrl
        );
    }
}