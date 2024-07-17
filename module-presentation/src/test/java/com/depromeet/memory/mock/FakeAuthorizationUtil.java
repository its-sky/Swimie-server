package com.depromeet.memory.mock;

import com.depromeet.security.AuthorizationUtil;

public class FakeAuthorizationUtil implements AuthorizationUtil {
    private long loginUserId;

    public FakeAuthorizationUtil(long loginUserId) {
        this.loginUserId = loginUserId;
    }

    @Override
    public Long getLoginId() {
        return loginUserId;
    }

    public void setLoginUserId(long loginUserId) {
        this.loginUserId = loginUserId;
    }
}
