/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.example.lianyungame.gamedemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.support.annotation.Nullable;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.GamesStatusCodes;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.utils.ResourceLoaderUtil;
import org.json.JSONException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "Game_codeLab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFloatWindowNewWay();
        Log.e(TAG, "onResume");
    }

    private void showFloatWindowNewWay() {
        if (hasInit) {
            // 请务必在init成功后，调用浮标接口
            Games.getBuoyClient(this).showFloatWindow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideFloatWindowNewWay();
    }

    private void hideFloatWindowNewWay() {
        Games.getBuoyClient(this).hideFloatWindow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_INTENT) {
            if (null == data) {
                showLog("signIn intent is null");
                return;
            }
            String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
            if (TextUtils.isEmpty(jsonSignInResult)) {
                showLog("signIn result is empty");
                return;
            }
            try {
                HuaweiIdAuthResult signInResult = new HuaweiIdAuthResult().fromJson(jsonSignInResult);
                if (0 == signInResult.getStatus().getStatusCode()) {
                    showLog("signIn success,result: " + signInResult.toJson());
                } else {
                    showLog("signIn failed: " + signInResult.getStatus().getStatusCode());
                }
            } catch (JSONException var7) {
                showLog("Failed to convert json from signInResult.");
            }
        }
    }

    private void initView() {
        findViewById(R.id.init_btn).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.btn_ranking).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.init_btn:
                init();
                break;
            case R.id.login_btn:
                signIn();
                break;
            case R.id.btn_ranking:
                clickRankingMenu();
                break;
            default:
                break;
        }
    }
}