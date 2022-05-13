/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hms.codelab.game;

import com.huawei.hms.support.account.result.AuthAccount;

/**
 * 账号数据保存
 */
public class SignInCenter {
    private static SignInCenter INS = new SignInCenter();

    private static AuthAccount currentAuthAccount;

    public static SignInCenter get() {
        return INS;
    }

    public void updateAuthAccount(AuthAccount AuthAccount) {
        currentAuthAccount = AuthAccount;
    }

    public AuthAccount getAuthAccount() {
        return currentAuthAccount;
    }
}
