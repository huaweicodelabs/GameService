/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamearchive.huawei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.AntiAddictionCallback;
import com.huawei.hms.jos.AppParams;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.JosStatusCodes;
import com.huawei.hms.jos.games.ArchivesClient;
import com.huawei.hms.jos.games.GameScopes;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.GamesStatusCodes;
import com.huawei.hms.jos.games.archive.ArchiveConstants;
import com.huawei.hms.jos.games.archive.ArchiveSummary;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AccountAuthResult;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.utils.ResourceLoaderUtil;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Program home page, which provides the entry for creating and displaying archives.
 * 程序主页，提供新建存档、展示存档的入口
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int SIGN_IN_INTENT = 3000;

    private boolean hasInit = false;

    private ArchivesClient mClient;

    private AccountAuthParams mAuthParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        findViewById(R.id.btn_init).setOnClickListener(this);
        findViewById(R.id.btn_createArchive).setOnClickListener(this);
        findViewById(R.id.btn_displayByAppAssistant).setOnClickListener(this);
        findViewById(R.id.btn_displayUserSelf).setOnClickListener(this);
        mClient = Games.getArchiveClient(MainActivity.this);

        List<Scope> scopes = new ArrayList<>();
        scopes.add(GameScopes.DRIVE_APP_DATA);
        mAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).setScopeList(scopes).createParams();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_init) {
            init();
        } else if (v.getId() == R.id.btn_createArchive) {
            startActivity(new Intent(this, CreateArchiveActivity.class));
        } else if (v.getId() == R.id.btn_displayByAppAssistant) {
            Task<Intent> showArchiveListIntentTask = mClient.getShowArchiveListIntent("游戏存档", true, true, -1);
            showArchiveListIntentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    if (intent != null) {
                        startActivityForResult(intent, 1);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        Log.e(TAG, "statusCode：" + apiException.getStatusCode());
                    }
                }
            });
        } else if (v.getId() == R.id.btn_displayUserSelf) {
            startActivity(new Intent(MainActivity.this, DisplayArchiveUserselfActivity.class));
        }
    }

    /**
     * Initialization of SDK, this method should be called while the main page of your application starting.
     * Then you can use functions of Game Setvice SDK and notice will show(if there is a notice).
     * *
     * SDK初始化，需要在应用首页启动时调用, 调用后才能正常使用SDK其他功能和展示公告。
     */
    private void init() {
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        Task<Void> initTask = appsClient.init(new AppParams(params, new AntiAddictionCallback() {
            @Override
            public void onExit() {
                // System.exit(0);
                // The callback will return in two situations:
                // 1. When a no-adult, real name user logs in to the game during the day, Huawei will pop up a box to remind the player that the game is not allowed. The player clicks "OK" and Huawei will return to the callback
                // 2. The no-adult, real name user logs in the game at the time allowed by the state. At 9 p.m., Huawei will pop up a box to remind the player that it is time. The player clicks "I know" and Huawei will return to the callback
                // You can realize the anti addiction function of the game here, such as saving the game, calling the account to exit the interface or directly the game process
                // 该回调会在如下两种情况下返回:
                // 1.未成年人实名帐号在白天登录游戏，华为会弹框提示玩家不允许游戏，玩家点击“确定”，华为返回回调
                // 2.未成年实名帐号在国家允许的时间登录游戏，到晚上9点，华为会弹框提示玩家已到时间，玩家点击“知道了”，华为返回回调
                // 您可在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口或直接游戏进程退出(如System.exit(0))
            }
        }));
        initTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showLog("init success");
                hasInit = true;
                // 一定要在init成功后，才可以调用登录接口
                signIn();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            showLog("-----init statusCode----" + statusCode);
                            // Error code 7401 indicates that the user did not agree to Huawei joint operations privacy agreement
                            // 错误码为7401时表示用户未同意华为联运隐私协议
                            if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) {
                                showLog("has reject the protocol");
                                // You can exit the game or re-call the init interface.
                                // 在此处实现退出游戏或者重新调用初始化接口
                            } else if (statusCode == GamesStatusCodes.GAME_STATE_NETWORK_ERROR) {
                                // Error code 7002 indicates network error
                                // 错误码7002表示网络异常
                                showLog("network error");
                                // 此处您可提示玩家检查网络，请不要重复调用init接口，否则断网情况下可能会造成手机高耗电。
                                // You can ask the player to check the network. Do not invoke the init interface repeatedly. Otherwise, the phone may consume a lot of power if the network is disconnected.
                            } else if (statusCode == 907135003) {
                                // 907135003表示玩家取消HMS Core升级或组件升级
                                // 907135003 indicates that user rejected the installation or upgrade of HMS Core.
                                showLog("init statusCode=" + statusCode);
                                init();
                            } else {
                                // Handle other error codes
                                // 在此处实现其他错误码的处理
                            }
                        }
                    }
                });
    }

    /**
     * Log in to the app. The login information (or error information) of the Huawei account that has logged in to the app is returned.
     * 登录，返回已登录此应用的华为帐号登录信息(或者错误信息)
     */
    private void signIn() {
        showLog("begin login and current hasInit=" + hasInit);
        // 一定要在init成功后，才可以调用登录接口
        // Be sure to call the login API after the init is successful
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(this, mAuthParams).silentSignIn();
        authAccountTask.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                showLog("signIn success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    if (2002 == statusCode) {
                        showLog("start getSignInIntent");
                        signInNewWay();
                    } else {
                        showLog("signIn failed:" + statusCode);
                    }
                }
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
    private void signInNewWay() {
        Intent intent = AccountAuthManager.getService(MainActivity.this, mAuthParams).getSignInIntent();
        startActivityForResult(intent, SIGN_IN_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (SIGN_IN_INTENT == requestCode) {
            handleSignInResult(data);
        } else if (1 == requestCode) {
            if (data.hasExtra(ArchiveConstants.ARCHIVE_SELECT)) {
                Bundle bundle = data.getParcelableExtra(ArchiveConstants.ARCHIVE_SELECT);
                Task<ArchiveSummary> archiveSummaryTask = mClient.parseSummary(bundle);
                archiveSummaryTask.addOnSuccessListener(new OnSuccessListener<ArchiveSummary>() {
                    @Override
                    public void onSuccess(ArchiveSummary archiveSummary) {
                        if (archiveSummary != null) {
                            String archiveId = archiveSummary.getId();
                            String descInfo = archiveSummary.getDescInfo();
                            String fileName = archiveSummary.getFileName();
                            showLog("get archive info success archiveId：" + archiveId + "，fileName：" + fileName+ "，descInfo：" + descInfo);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.e(TAG, "statusCode：" + apiException.getStatusCode());
                        }
                    }
                });
            } else if (data.hasExtra(ArchiveConstants.ARCHIVE_ADD)) {
                // 调用addArchive添加存档
            }
        } else {
            showLog("unknown requestCode in onActivityResult");
        }
    }

    /**
     * Login authorization result response processing method.
     * *
     * 登录授权的结果响应处理方法
     *
     * @param data Data
     */
    private void handleSignInResult(Intent data) {
        String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
        if (TextUtils.isEmpty(jsonSignInResult)) {
            showLog("SignIn result is empty");
            return;
        }
        try {
            AccountAuthResult signInResult = new AccountAuthResult().fromJson(jsonSignInResult);
            if (0 == signInResult.getStatus().getStatusCode()) {
                showLog("Sign in success.");
            } else {
                showLog("Sign in failed: " + signInResult.getStatus().getStatusCode());
            }
        } catch (JSONException var7) {
            showLog("Failed to convert json from signInResult.");
        }
    }

    /**
     * Print logs
     * 打印日志
     *
     * @param msg
     */
    private void showLog(String msg) {
        Log.d(TAG, msg);
    }
}