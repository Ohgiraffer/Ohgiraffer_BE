package com.ohgiraffer.auth.application.service;

import com.ohgiraffer.auth.application.usecase.AuthQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthQueryService implements AuthQueryUsecase {
}
