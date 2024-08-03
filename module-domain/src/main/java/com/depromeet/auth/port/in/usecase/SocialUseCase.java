package com.depromeet.auth.port.in.usecase;

import com.depromeet.auth.vo.kakao.KakaoAccountProfile;
import com.depromeet.dto.auth.AccountProfileResponse;

public interface SocialUseCase {
    AccountProfileResponse getGoogleAccountProfile(String code, String origin);

    KakaoAccountProfile getKakaoAccountProfile(String code, String origin);
}
