package com.ohgiraffer.team.application.service;

import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamSendbirdChannelMirrorDeleteService {

    private final ChatChannelRepository chatChannelRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMirror(
            String sendbirdChannelUrl
    ) {
        if (sendbirdChannelUrl == null
                || sendbirdChannelUrl.isBlank()) {
            return;
        }

        String channelUrl =
                sendbirdChannelUrl.trim();

        chatChannelRepository.deleteMembersBySendbirdChannelUrl(
                channelUrl
        );

        chatChannelRepository.deleteBySendbirdChannelUrl(
                channelUrl
        );
    }
}