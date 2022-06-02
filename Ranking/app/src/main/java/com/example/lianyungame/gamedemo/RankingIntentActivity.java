/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.example.lianyungame.gamedemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.annotation.Nullable;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.RankingsClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RankingIntentActivity extends BaseActivity {

    @BindView(R.id.et_ranking_id)
    EditText etRankingId;

    @BindView(R.id.sp_time_dimension)
    Spinner timeSpinner;

    private ArrayAdapter<String> adapter;

    private int choosedTimeDimension = 0;

    private RankingsClient rankingsClient;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_intent);
        ButterKnife.bind(this);
        initTimeDimensionSpinner();
        rankingsClient = Games.getRankingsClient(this);
    }

    /**
     * Init spinner for initial time dimension.
     * *
     * 初始化时间维度的下拉列表。
     */
    private void initTimeDimensionSpinner() {
        String[] ctype = new String[]{"day", "week", "all", "default", "invalid value"};
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosedTimeDimension = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.init_btn)
    public void init() {
        super.init();
    }

    @OnClick(R.id.login_btn)
    public void signIn() {
        super.signIn();
    }

    @OnClick(R.id.btn_ranking)
    public void ranking() {
        super.clickRankingMenu();
    }

    /**
     * Get the Intent object of the specified leaderboard page, support the specified time dimension.
     * *
     * 获取指定排行榜页面的Intent对象，支持指定时间维度。
     */
    @OnClick(R.id.btn_get_ranking)
    public void onClickGetIntent() {
        String rankingId = etRankingId.getText().toString();
        Task<Intent> allIntentTask = rankingsClient.getRankingIntent(rankingId, choosedTimeDimension);
        allIntentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                try {
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    showLog("startActivityForResult Exception");
                }
            }
        });
        allIntentTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                    showLog(result);
                }
            }
        });
    }
}
