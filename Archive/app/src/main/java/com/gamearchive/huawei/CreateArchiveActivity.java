/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamearchive.huawei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.ArchivesClient;
import com.huawei.hms.jos.games.GameScopes;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.archive.ArchiveConstants;
import com.huawei.hms.jos.games.archive.ArchiveDetails;
import com.huawei.hms.jos.games.archive.ArchiveSummary;
import com.huawei.hms.jos.games.archive.ArchiveSummaryUpdate;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AccountAuthResult;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.api.entity.auth.Scope;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * create archive
 * 创建存档
 */
public class CreateArchiveActivity extends AppCompatActivity {
    private static final String TAG = "CreateArchiveActivity";
    private static final int SIGN_IN_INTENT = 1000;

    private boolean isCache = true;
    private boolean hasImage = true;

    private RadioGroup rgIsCache;
    private RadioGroup rgHasImage;

    private RadioButton rbCacheYes;
    private RadioButton rbCacheNo;
    private RadioButton rbHasImageYes;
    private RadioButton rbHasImageNo;

    private EditText etDes;
    private EditText etPlayedTime;
    private EditText etProgress;
    private ImageView ivCoverPic;

    private AccountAuthParams mAuthParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_archive);

        rgIsCache = findViewById(R.id.rg_isCache);
        rgHasImage = findViewById(R.id.rg_hasImage);

        rbCacheYes = findViewById(R.id.rb_cache_yes);
        rbCacheNo = findViewById(R.id.rb_cache_no);
        rbHasImageYes = findViewById(R.id.rb_hasImage_yes);
        rbHasImageNo = findViewById(R.id.rb_hasImage_no);

        etDes = findViewById(R.id.et_des);
        etPlayedTime = findViewById(R.id.et_playedTime);
        etProgress = findViewById(R.id.et_progress);
        ivCoverPic = findViewById(R.id.iv_cover_pic);

        rgIsCache.setOnCheckedChangeListener(cacheCheckChangeListener);
        rgHasImage.setOnCheckedChangeListener(hasImageCheckChangeListener);

        List<Scope> scopes = new ArrayList<>();
        scopes.add(GameScopes.DRIVE_APP_DATA);
        mAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).setScopeList(scopes).createParams();

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createArchive();
            }
        });
    }


    /**
     * Log in to the app. The login information (or error information) of the Huawei account that has logged in to the app is returned.
     * 登录，返回已登录此应用的华为帐号登录信息(或者错误信息)
     */
    private void signIn() {
        // 一定要在init成功后，才可以调用登录接口
        // Be sure to call the login API after the init is successful
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(this, mAuthParams).silentSignIn();
        authAccountTask.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                showLog("signIn success");
                createArchive();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    showLog("signIn failed:" + apiException.getStatusCode());
                    showLog("start getSignInIntent");
                    signInNewWay();
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
        Intent intent = AccountAuthManager.getService(CreateArchiveActivity.this, mAuthParams).getSignInIntent();
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
        if (null == data) {
            showLog("signIn inetnt is null");
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
                showLog("Sign in success.");
                createArchive();
            } else {
                showLog("Sign in failed: " + signInResult.getStatus().getStatusCode());
            }
        } catch (JSONException var7) {
            showLog("Failed to convert json from signInResult.");
        }
    }

    private void createArchive() {
        String description = etDes.getText().toString();
        long playedTime = Long.parseLong(TextUtils.isEmpty(etPlayedTime.getText().toString()) ? "0" : etPlayedTime.getText().toString());
        long progress = Long.parseLong(TextUtils.isEmpty(etProgress.getText().toString()) ? "0" : etProgress.getText().toString());
        ArchiveDetails archiveDetails = new ArchiveDetails.Builder().build();
        archiveDetails.set((progress + description + playedTime).getBytes());

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cover_picture);
        ArchiveSummaryUpdate.Builder builder = new ArchiveSummaryUpdate.Builder();
        builder.setActiveTime(playedTime)
                .setCurrentProgress(progress)
                .setDescInfo(description);
        ArchiveSummaryUpdate archiveSummaryUpdate;
        if (hasImage) {
            archiveSummaryUpdate = builder.setThumbnail(bitmap)
                    .setThumbnailMimeType("png").build();
        } else {
            archiveSummaryUpdate = builder.build();
        }
        ArchivesClient client = Games.getArchiveClient(this);
        Task<ArchiveSummary> archiveSummaryTask = client.addArchive(archiveDetails, archiveSummaryUpdate, isCache);
        archiveSummaryTask.addOnSuccessListener(new OnSuccessListener<ArchiveSummary>() {
            @Override
            public void onSuccess(ArchiveSummary archiveSummary) {
                if (archiveSummary != null) {
                    String fileName = archiveSummary.getFileName();
                    String archiveId = archiveSummary.getId();
                    String descInfo = archiveSummary.getDescInfo();
                    showLog("create archive success descInfo：" + descInfo + "，fileName：" + fileName+ "，archiveId：" + archiveId);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    showLog("createArchive failed: " + statusCode);
                    if (statusCode == 7216) {
                        Log.e(TAG, "Check whether the format of the archive cover image is correct.");
                    } else if (statusCode == 7218) {
                        Log.e(TAG, "The game service is disabled.");
                    } else if (statusCode == 7013) {
                        // 未登录华为帐号  You have not logged in to your Huawei ID.
                        signIn();
                    } else if (statusCode == 7213) {//存档个数达到上限 The number of archived files has reached the upper limit.
                        Log.e(TAG, "The number of archived files has reached the upper limit.");
                    } else {
                        Log.e(TAG, "statusCode：" + statusCode);
                    }
                }
            }
        });
    }

    /**
     * 监听是否需要缓存
     */
    private final RadioGroup.OnCheckedChangeListener cacheCheckChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_cache_yes) {
                rbCacheYes.setChecked(true);
                rbCacheNo.setChecked(false);
                isCache = true;
            } else if (checkedId == R.id.rb_cache_no) {
                rbCacheYes.setChecked(false);
                rbCacheNo.setChecked(true);
                isCache = false;
            }
        }
    };

    /**
     * 监听是否存在封面图片
     */
    private final RadioGroup.OnCheckedChangeListener hasImageCheckChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_hasImage_yes) {
                rbHasImageYes.setChecked(true);
                rbHasImageNo.setChecked(false);
                hasImage = true;
                ivCoverPic.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rb_hasImage_no) {
                rbHasImageYes.setChecked(false);
                rbHasImageNo.setChecked(true);
                hasImage = false;
                ivCoverPic.setVisibility(View.GONE);
            }
        }
    };

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