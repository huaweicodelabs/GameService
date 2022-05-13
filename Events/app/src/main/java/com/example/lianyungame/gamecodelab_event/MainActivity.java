/*
Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
*/
package com.example.lianyungame.gamecodelab_event;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
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
import com.huawei.hms.jos.games.EventsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.event.Event;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.utils.ResourceLoaderUtil;
import java.util.List;

/*
game event Main class.
*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GameDemo_Event";
    private final static int SIGN_IN_INTENT = 3000;
    private EventsClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.get_eventList_btn).setOnClickListener(this);
        findViewById(R.id.get_eventById_btn).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
    }

    private void getEventList() {
        if (client == null) {
            Log.e(TAG, "client == null");
            return;
        }
        // The forceReload parameter of the boolean type specifies whether to query data from the local cache of the Huawei game server or application client.
        // 通过boolean类型的forceReload参数指定是从华为游戏服务端还是从应用客户端本地缓存中查询数据
        Task<List<Event>> task = client.getEventList(true);
        task.addOnSuccessListener(new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> data) {
                if (data == null) {
                    Log.w(TAG, "event is null");
                    return;
                }
                for (Event event : data) {
                    Log.d(TAG, "event id:" + event.getEventId() + "; name:" + event.getName() + "; value:" + event.getValue());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:"
                            + ((ApiException) e).getStatusCode();
                    Log.e(TAG, result);
                }
            }
        });
    }

    private void getEventByIds() {
        if (client == null) {
            Log.e(TAG, "client == null");
            return;
        }
        // Replace this parameter with the game event ID configured on the AGC background.
        // 此处xxx需要替换为AGC后台配置的游戏事件ID
        String[] eventIds = {"xxx", "xxx"};
        // The forceReload parameter of the boolean type specifies whether to query data from the local cache of the Huawei game server or application client.
        // 通过boolean类型的forceReload参数指定是从华为游戏服务端还是从应用客户端本地缓存中查询数据
        Task<List<Event>> task = client.getEventListByIds(true, eventIds);
        task.addOnSuccessListener(new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> data) {
                if (data == null) {
                    Log.e(TAG, "event is null");
                    return;
                }
                for (Event event : data) {
                    Log.d(TAG, "event id:" + event.getEventId() + "; name:" + event.getName() + "; value:" + event.getValue());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:"
                            + ((ApiException) e).getStatusCode();
                    Log.e(TAG, result);
                }
            }
        });
    }

    private void submitEvent() {
        if (client == null) {
            Log.e(TAG, "client == null");
            return;
        }
        // Replace this parameter with the game event ID configured on the AGC background.
        // 此处xxx需要替换为AGC后台配置的游戏事件ID
        String eventId = "xxx";
        // You can set the number of events to be added. Here, set this parameter to 30.
        // 可以自行设置该事件增加数量，这里设置为30
        client.grow(eventId, 30);
    }

    public void login() {
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        // Set the anti-addiction prompt context
        // 设置防沉迷提示语的Context
        ResourceLoaderUtil.setmContext(this);
        Task<Void> initTask = appsClient.init(new AppParams(params, new AntiAddictionCallback() {
            @Override
            public void onExit() {
                // Implement the game addiction prevention function, such as saving the game and invoking the account exit interface.
                // 在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口
                Log.d(TAG, "onExit");
            }
        }));

        initTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "init success");
                // The login interface can be invoked only after the init is successful. Otherwise, error code 7018 is displayed.
                // 一定要在init成功后，才可以调用登录接口，否者登录会提示7018错误码
                signIn();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "init failed, " + e.getMessage());
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    // Error code 7401 indicates that the user did not agree to Huawei joint operations privacy agreement
                    // 错误码为7401时表示用户未同意华为联运隐私协议
                    if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) {
                        Log.e(TAG, "has reject the protocol");
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
     * To log in to a game, you need to invoke the Huawei ID to authenticate the login, and then invoke the getGamePlayer interface to obtain the player information.
     * *1. Invoke the silent login interface of the Huawei ID login interface. This interface does not open the login page for authorized applications.
     * 2. The silent login failure is usually caused by authorization. In this case, the system invokes the display login interface in the callback interface to start the login authorization page for login authentication.
     * The login result is obtained from the onActivityResult callback. In this case, you can invoke the game interface for obtaining player information.
     * 游戏登录需要先调用华为帐号认证登录完成之后，再调用getGamePlayer接口获取游戏玩家信息
     * 1.先调用华为帐号登录接口的静默登录接口，此接口对于已经授权登录过的应用不会再次拉起登录页面。
     * 2.静默登录失败一般是由于登录需要授权，此时在回调中调用显示登录接口拉起登录授权页面进行登录认证。
     * 登录结果会在onActivityResult回调中获取，可在此时调用游戏获取玩家信息接口。
     */
    public void signIn() {
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authAccountTask.addOnSuccessListener(
                authAccount -> {
                    client = Games.getEventsClient(MainActivity.this);
                    Toast.makeText(MainActivity.this, "signIn success", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "signIn success");
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    Toast.makeText(MainActivity.this, "signIn failed:" + apiException.getStatusCode(), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "signIn failed:" + apiException.getStatusCode());
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
        Intent intent = AccountAuthManager.getService(MainActivity.this, getHuaweiIdParams()).getSignInIntent();
        startActivityForResult(intent, SIGN_IN_INTENT);
    }

    public AccountAuthParams getHuaweiIdParams() {
        return new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.get_eventList_btn:
                getEventList();
                break;
            case R.id.get_eventById_btn:
                getEventByIds();
                break;
            case R.id.submit_btn:
                submitEvent();
                break;
            default:
                break;
        }
    }
}