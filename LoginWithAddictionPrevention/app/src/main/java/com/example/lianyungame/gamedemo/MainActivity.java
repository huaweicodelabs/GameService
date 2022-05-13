/*
Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
*/
package com.example.lianyungame.gamedemo;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.utils.ResourceLoaderUtil;
import org.json.JSONException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/*
game login main class.
*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "Game_codeLab";
    private final static int SIGN_IN_INTENT = 3000;
    private PlayersClient playersClient;
    public String playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void init() {
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
                playersClient = Games.getPlayersClient(MainActivity.this);
                // The login interface can be invoked only after the init is successful. Otherwise, error code 7018 is displayed.
                // 一定要在init成功后，才可以调用登录接口，否者登录会提示7018错误码
                // signIn();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                showLog("init failed, " + e.getMessage());
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

    /**
     * To log in to a game, you need to invoke the Huawei ID to authenticate the login, and then invoke the getCurrentPlayer or getGamePlayer interface to obtain the player information.
     * *1. Invoke the silent login interface of the Huawei ID login interface. This interface does not open the login page for authorized applications.
     * 2. The silent login failure is usually caused by authorization. In this case, the system invokes the display login interface in the callback interface to start the login authorization page for login authentication.
     * The login result is obtained from the onActivityResult callback. In this case, you can invoke the game interface for obtaining player information.
     * 游戏登录需要先调用华为帐号认证登录完成之后，再调用getCurrentPlayer或者getGamePlayer接口获取游戏玩家信息
     * 1.先调用华为帐号登录接口的静默登录接口，此接口对于已经授权登录过的应用不会再次拉起登录页面。
     * 2.静默登录失败一般是由于登录需要授权，此时在回调中调用显示登录接口拉起登录授权页面进行登录认证。
     * 登录结果会在onActivityResult回调中获取，可在此时调用游戏获取玩家信息接口。
     */
    public void signIn() {
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authAccountTask.addOnSuccessListener(
                authAccount -> {
                    showLog("signIn success");
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    Toast.makeText(MainActivity.this, "signIn failed:" + apiException.getStatusCode(), Toast.LENGTH_LONG).show();
                                    showLog("signIn failed:" + apiException.getStatusCode());
                                    signInNewWay();
                                }
                            }
                        });
    }

    /**
     * get the Intent of the Huawei ID login authorization page and invoke startActivityForResult(Intent, int) to open the Huawei ID login authorization page.
     * 获取到华为帐号登录授权页面的Intent，并通过调用startActivityForResult(Intent, int)打开华为帐号登录授
     * 权页面。
     */
    public void signInNewWay() {
        Intent intent = AccountAuthManager.getService(MainActivity.this, getHuaweiIdParams()).getSignInIntent();//获取华为账号登录请求页面
        startActivityForResult(intent, SIGN_IN_INTENT);
    }

    public AccountAuthParams getHuaweiIdParams() {
        return new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    /**
     * Obtains user information about gamers.
     * 获取游戏玩家用户信息
     */
    public void getGamePlayer() {
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
                        // error code 7400 indicates that the user has not agreed to the joint operations privacy agreement
                        // error code 7018 indicates that the init API is not called.
                        // 7400表示用户未签署联运协议，需要继续调用init接口
                        // 7018表示初始化失败，需要继续调用init接口
                        init();
                    } else if (GamesStatusCodes.GAME_STATE_NETWORK_ERROR == ((ApiException) e).getStatusCode()) {
                        // Error code 7002 indicates that the network is abnormal. You can prompt the player to check the network.
                        // 错误码7002表示网络异常，此处您可提示玩家检查网络
                        showLog("Network error");
                    }
                }
            }
        });
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
                    showLog("signIn success");
                } else {
                    showLog("signIn failed: " + signInResult.getStatus().getStatusCode());
                }
            } catch (JSONException var7) {
                showLog("Failed to convert json from signInResult.");
            }
        }
    }

    /**
     * Log output
     * 日志输出
     */
    public void showLog(String logLine) {
        StringBuilder sbLog = new StringBuilder();
        DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String time = format.format(new Date());
        sbLog.append(time).append(":").append(logLine);
        Log.i(TAG, sbLog.toString());
    }

    private void initView() {
        findViewById(R.id.init_btn).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.player1_btn).setOnClickListener(this);
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
            case R.id.player1_btn:
                getGamePlayer();
                break;
            default:
                break;
        }
    }
}