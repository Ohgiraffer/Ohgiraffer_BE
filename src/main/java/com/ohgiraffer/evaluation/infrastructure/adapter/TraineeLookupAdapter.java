package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.application.port.TraineeLookupPort;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TraineeLookupAdapter implements TraineeLookupPort {

    private final UserRepository userRepository;

    public TraineeLookupAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Trainee> findTraineesByEmails(Collection<String> emails) {
        /*
         * 사용자 저장소에 이메일 목록 조회가 없어 하나씩 묻는다.
         * 다만 시트에는 같은 훈련생이 항목 수만큼 반복되므로, 중복을 먼저 걷어내면
         * 질의 수가 행 수가 아니라 사람 수만큼으로 줄어든다.
         *
         * 사용자 도메인에 목록 조회가 생기면 그 한 번으로 바꾼다.
         */
        Set<String> distinctEmails = emails.stream()
                .filter(Objects::nonNull)
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toSet());

        Map<String, Trainee> trainees = new HashMap<>();

        for (String email : distinctEmails) {
            userRepository.findByEmail(email)
                    .filter(user -> user.getRole() == Role.STUDENT)
                    .ifPresent(user -> trainees.put(
                            email,
                            new Trainee(user.getId(), user.getName())
                    ));
        }

        return trainees;
    }
}
