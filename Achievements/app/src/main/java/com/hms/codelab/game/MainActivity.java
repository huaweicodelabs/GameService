/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hms.codelab.game;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.hms.codelab.game.achievement.AchievementListActivity;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.AntiAddictionCallback;
import com.huawei.hms.jos.AppParams;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.JosStatusCodes;
import com.huawei.hms.jos.games.AchievementsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AccountAuthResult;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.utils.ResourceLoaderUtil;

import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 应用首页
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "codelab_achievement";
    private PlayersClient playersClient;
    private boolean hasInit = false;
    private static final int SIGN_IN_INTENT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.game_login).setOnClickListener(this);
        findViewById(R.id.show_achievement_list).setOnClickListener(this);
    }


    /**
     * @return 组装游戏登录scope配置对象
     */
    public AccountAuthParams getHuaweiIdParams() {
        return new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    /**
     * 游戏登录需要先调用华为账号认证登录完成之后，再调用getcurrentPlayer接口获取游戏玩家信息
     * 1.先调用华为账号登录接口的静默登录接口，此接口对于已经授权登录过的应用不会再次拉起登录页面。
     * 2.静默登录失败一般是由于需要首次登录需要授权，此时在回调中调用显示登录接口拉起登录授权页面进行登录认证。
     * 登录接口会在onActivity生命周期中获取，可在此时调用游戏获取玩家信息接口。
     */
    public void signIn() {
        showLog("begin login and current hasInit=" + hasInit);
        // 一定要在init成功后，才可以调用登录接口
        // Be sure to call the login API after the init is successful
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authAccountTask
                .addOnSuccessListener(
                        authAccount -> {
                            showLog("signIn success");
                            getGamePlayer();
                            SignInCenter.get().updateAuthAccount(authAccount);
                        })
                .addOnFailureListener(
                        e -> {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                showLog("signIn failed:" + apiException.getStatusCode());
                                showLog("start getSignInIntent");
                                signInNewWay();
                            }
                        });
    }

    /**
     * Obtain the Intent of the Huawei account login authorization page, and open the Huawei account
     * login authorization page by calling startActivityForResult(Intent, int).
     * *
     * 获取到华为帐号登录授权页面的Intent，并通过调用startActivityForResult(Intent, int)打开华为帐号登录授
     * 权页面。
     */
    public void signInNewWay() {
        Intent intent = AccountAuthManager.getService(MainActivity.this, getHuaweiIdParams()).getSignInIntent();
        startActivityForResult(intent, SIGN_IN_INTENT);
    }


    /**
     * 获取游戏玩家用户信息
     */
    public void getGamePlayer() {
        playersClient = Games.getPlayersClient(this);
        Task<Player> playerTask = playersClient.getGamePlayer();
        playerTask.addOnSuccessListener(player -> {
            String playerID = player.getPlayerId();
            showLog("getPlayerInfo Success");
        }).addOnFailureListener(e -> {
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
        });
    }


    /**
     * SDK初始化
     */
    private void init() {
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        Task<Void> initTask;
        // Set the anti-addiction prompt context, this line must be added
        // 设置防沉迷提示语的Context，此行必须添加
        ResourceLoaderUtil.setmContext(this);
        initTask = appsClient.init(
                new AppParams(params, () -> {
                    showLog("onExit");
                    // System.exit(0);
                    // The callback will return in two situations:
                    // 1. When a no-adult, real name user logs in to the game during the day, Huawei will pop up a box to remind the player that the game is not allowed. The player clicks "OK" and Huawei will return to the callback
                    // 2. The no-adult, real name user logs in the game at the time allowed by the state. At 9 p.m., Huawei will pop up a box to remind the player that it is time. The player clicks "I know" and Huawei will return to the callback
                    // You can realize the anti addiction function of the game here, such as saving the game, calling the account to exit the interface or directly the game process
                    // 该回调会在如下两种情况下返回:
                    // 1.未成年人实名帐号在白天登录游戏，华为会弹框提示玩家不允许游戏，玩家点击“确定”，华为返回回调
                    // 2.未成年实名帐号在国家允许的时间登录游戏，到晚上9点，华为会弹框提示玩家已到时间，玩家点击“知道了”，华为返回回调
                    // 您可在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口或直接游戏进程退出(如System.exit(0))
                }));
        initTask.addOnSuccessListener(aVoid -> {
            showLog("init success");
            hasInit = true;
            // Make sure that the interface of showFloatWindow() is successfully called once after the game has been initialized successfully
            // 游戏初始化成功后务必成功调用过一次浮标显示接口
            // 一定要在init成功后，才可以调用登录接口
            signIn();
        }).addOnFailureListener(
                e -> {
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
                });

    }


    /**
     * 日志输出
     */
    public void showLog(String logLine) {
        Log.i(TAG, logLine);
    }


    /**
     * 按钮点击监听事件
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.game_login) {
            init();
        } else if (v.getId() == R.id.show_achievement_list) {
            loadAchievement();
        }
    }

    private void loadAchievement() {
        if (SignInCenter.get().getAuthAccount() == null) {
            showLog("signIn first");
            return;
        }
        Intent intent = new Intent(this, AchievementListActivity.class);
        startActivity(intent);
    }

    /**
     * Get a data interface that contains a list of all game achievements of the current player.
     * Called only when the user views the list of achievements.
     * *
     * 获取包含当前玩家的所有游戏成就列表的数据界面。仅在用户查看成就列表时调用。
     */
    public void getAchievementIntent() {
        AchievementsClient client = Games.getAchievementsClient(this);
        Task<Intent> task = client.getShowAchievementListIntent();
        task.addOnSuccessListener(intent -> {
            if (intent == null) {
                showLog("intent = null");
            } else {
                try {
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    showLog("Achievement Activity is Invalid");
                }
            }
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                showLog(result);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SIGN_IN_INTENT == requestCode) {
            if (null == data) {
                showLog("signIn intent is null");
                return;
            }
            String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
            if (TextUtils.isEmpty(jsonSignInResult)) {
                showLog("SignIn result is empty");
                return;
            }
            try {
                AccountAuthResult signInResult = new AccountAuthResult().fromJson(jsonSignInResult);
                if (0 == signInResult.getStatus().getStatusCode()) {
                    SignInCenter.get().updateAuthAccount(signInResult.getAccount());
                    getGamePlayer();
                    showLog("Sign in success.");
                    showLog("Sign in result: " + signInResult.toJson());
                } else {
                    showLog("Sign in failed: " + signInResult.getStatus().getStatusCode());
                }
            } catch (JSONException var7) {
                showLog("Failed to convert json from signInResult.");
            }
        } else {
            showLog("unknown requestCode in onActivityResult");
        }
    }

}
