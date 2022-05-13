/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamebuoy.huawei;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

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
import com.huawei.hms.jos.games.GamesStatusCodes;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.utils.ResourceLoaderUtil;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean hasInit = false;//是否初始化成功标识
    private static final String TAG = "MainActivity";
    StringBuffer sbLog = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        findViewById(R.id.btn_init).setOnClickListener(this);
        findViewById(R.id.btn_showWindow).setOnClickListener(this);
        findViewById(R.id.btn_hideWindow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_init){
            init();
        }else if (v.getId() == R.id.btn_showWindow){
            showFloatWindowNewWay();
        }else if (v.getId() == R.id.btn_hideWindow){
            hideFloatWindowNewWay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFloatWindowNewWay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideFloatWindowNewWay();
    }

    /**
     * SDK initialization. This API needs to be invoked when the app home page is started.
     * The game buoy and other SDK functions can be used only after the SDK is invoked.
     * *
     * SDK初始化，需要在应用首页启动时调用, 调用后才能正常使用游戏浮标和SDK其他功能。
     */
    private void init(){
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        //设置防沉迷提示语的context，此行必须添加
        ResourceLoaderUtil.setmContext(this);
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
                // Make sure that the interface of showFloatWindow() is successfully called once after the game has been initialized successfully
                // 游戏初始化成功后务必成功调用过一次浮标显示接口
                showFloatWindowNewWay();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) {
                        // Error code 7401 indicates that the user did not agree to Huawei joint operations privacy agreement
                        // 错误码为7401时表示用户未同意华为联运隐私协议
                        showLog("has reject the protocol");
                        // You need to prohibit players from entering the game here.
                        // 此处您需禁止玩家进入游戏
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
     * Show the game buoy.
     * *
     * 显示游戏浮标。
     */
    private void showFloatWindowNewWay() {
        if (hasInit) {
            // 请务必在init成功后，调用浮标接口
            Games.getBuoyClient(this).showFloatWindow();
            showLog("show floatWindow");
        }
    }

    /**
     * Hide the displayed game buoy.
     * *
     * 隐藏已经显示的游戏浮标。
     */
    private void hideFloatWindowNewWay() {
        Games.getBuoyClient(this).hideFloatWindow();
        showLog("hide floatWindow");
    }

    public void showLog(String logLine) {
        Log.e(TAG,logLine);
    }

}