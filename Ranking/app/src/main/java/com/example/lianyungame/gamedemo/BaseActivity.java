/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.example.lianyungame.gamedemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.annotation.Nullable;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.AntiAddictionCallback;
import com.huawei.hms.jos.AppParams;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.JosStatusCodes;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.archive.ArchiveSummary;
import com.huawei.hms.jos.games.gamesummary.GameSummary;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
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
import butterknife.OnClick;

/**
 *提供了初始化、登录的方法
 */
public class BaseActivity extends Activity {
    StringBuffer sbLog = new StringBuffer();
    public final static int SIGN_IN_INTENT = 3000;
    public boolean hasInit = false;
    private PlayersClient playersClient;
    public AccountAuthParams getHuaweiAccountAuthParams() {
        return new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void init() {
        android.util.Log.d("wll", "init");
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        // Set the anti-addiction prompt context, this line must be added
        // 设置防沉迷提示语的Context
        ResourceLoaderUtil.setmContext(this);
        Task<Void> initTask = appsClient.init(new AppParams(params, new AntiAddictionCallback() {
            @Override
            public void onExit() {
                // Implement the game addiction prevention function, such as saving the game and invoking the account exit interface.
                // 在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口
                showLog("onExit");
            }
        }));

        initTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showLog("init success");
                playersClient = Games.getPlayersClient(BaseActivity.this);
                // The login interface can be invoked only after the init is successful. Otherwise, error code 7018 is displayed.
                // 一定要在init成功后，才可以调用登录接口，否者登录会提示7018错误码
                // signIn();
                hasInit = true;
                if (null != mCallback) {
                    mCallback.onSuccess(getTime() + " init success");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                showLog("init failed, " + e.getMessage());
                if (null != mCallback) {
                    mCallback.onSuccess(getTime() + " init failed, " + e.getMessage());
                }
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    // Error code 7401 indicates that the user did not agree to Huawei joint operations privacy agreement
                    // 错误码为7401时表示用户未同意华为联运隐私协议
                    if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) {
                        showLog("has reject the protocol");
                        // You can exit the game or re-call the init interface.
                        // 在此处实现退出游戏或者重新调用初始化接口
                    }
                    // Handle other error codes.
                    // 在此处实现其他错误码的处理
                }
            }
        });
    }

    private Callback mCallback;
    public interface Callback {
        public void onSuccess(String signInResult);
        public void onFailed(String signInResult);
    }
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
    public Callback getSignInCallback(Callback callback) {
        return this.mCallback;
    }
    public void signIn() {
        Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                Toast.makeText(BaseActivity.this, "signIn success; display:" + authHuaweiId.getDisplayName(), Toast.LENGTH_LONG).show();
                showLog("signIn success");
                if (null != mCallback) {
                    mCallback.onSuccess(getTime() + " signIn success");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Toast.makeText(BaseActivity.this, "signIn failed:" + apiException.getStatusCode(), Toast.LENGTH_LONG).show();
                    showLog("signIn failed:" + apiException.getStatusCode());
                    signInNewWay();
                }
            }
        });
    }

    public void signInNewWay() {
        // 获取华为账号登录请求页面
        Intent intent = HuaweiIdAuthManager.getService(BaseActivity.this, getHuaweiIdParams()).getSignInIntent();
        startActivityForResult(intent, SIGN_IN_INTENT);
    }

    public HuaweiIdAuthParams getHuaweiIdParams() {
        return new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    public void clickRankingMenu() {
        startActivity(new Intent(this, RankingActivity.class));
        overridePendingTransition(0, 0);
        finish();
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
                    if (null != mCallback) {
                        mCallback.onSuccess(getTime() + " signIn success,result: " + signInResult.toJson());
                    }
                    showLog("signIn success");
                } else {
                    if (null != mCallback) {
                        mCallback.onFailed(getTime() + " signIn failed: " + signInResult.getStatus().getStatusCode());
                    }
                    showLog("signIn failed: " + signInResult.getStatus().getStatusCode());
                }
            } catch (JSONException var7) {
                showLog("Failed to convert json from signInResult.");
            }
        }
    }

    public void getGamePlayer() {
        // 获取游戏玩家用户信息
        playersClient = Games.getPlayersClient(this);
        Task<Player> playerTask = playersClient.getGamePlayer();
        playerTask.addOnSuccessListener(new OnSuccessListener<Player>() {
            @Override
            public void onSuccess(Player player) {
                showLog("getPlayerInfo Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 获取玩家信息失败
                if (e instanceof ApiException) {
                    showLog("getPlayerInfo failed, status: " + ((ApiException) e).getStatusCode());
                    if (7400 == ((ApiException) e).getStatusCode() || 7018 == ((ApiException) e).getStatusCode()) {
                        // 7400表示用户未签署联运协议，需要继续调用init接口
                        // 7018表示初始化失败，需要继续调用init接口
                        // error code 7400 indicates that the user has not agreed to the joint operations privacy agreement
                        // error code 7018 indicates that the init API is not called.
                        init();
                    }
                }
            }
        });
    }

    public void showLog(String logLine) {
        show(logLine);
    }

    public String getTime() {
        DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.ENGLISH);
        String time = format.format(new Date());
        return time;
    }

    protected void show(String logLine) {
        sbLog.append(getTime()).append(":").append(logLine);
        sbLog.append('\n');
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final TextView vText = (TextView) findViewById(R.id.tv_log);
                vText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vText.setText("");
                        sbLog = new StringBuffer();
                    }
                });
                vText.setText(sbLog.toString());
                View vScrool = findViewById(R.id.sv_log);
                if (vScrool instanceof ScrollView) {
                    ScrollView svLog = (ScrollView) vScrool;
                    svLog.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }

    protected AuthAccount getAuthHuaweiId() {
        return SignInCenter.get().getAuthAccount();
    }
}
