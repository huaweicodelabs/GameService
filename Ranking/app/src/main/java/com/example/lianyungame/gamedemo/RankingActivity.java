/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.example.lianyungame.gamedemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.RankingsClient;
import com.huawei.hms.jos.games.ranking.ScoreSubmissionInfo;
import android.support.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 提交排行榜
 */
public class RankingActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        initView();
    }

    /**
     * 点击按钮，跳转排行榜列表
     */
    public void onClickGetRankingIntent() {
        startActivity(new Intent(this, RankingIntentActivity.class));
    }

    /**
     * 点击按钮，跳转展示排行榜分数
     */
    public void getRankingData() {
        startActivity(new Intent(this, RankingDataActivity.class));
    }

    /**
     * Get the leaderboard switch status. Whether the player agrees to report his data to the
     * leaderboard,the switch is off when logging in for the first time.
     * *
     * 获取排行榜开关状态。即玩家是否同意将自己的数据上报到排行榜，首次登录时开关为关。
     */
    public void onClickGetStatus() {
        RankingsClient rankingsClient = Games.getRankingsClient(this);
        Task<Integer> task = rankingsClient.getRankingSwitchStatus();
        task.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                // 查询排行榜开关状态值为0，表示玩家未设置在排行榜中公开自己的分数。您可以在游戏中设置开关选项供玩家打开开关。排行榜开关状态值为1时，可以调用提交玩家分数。
                show("getRankingSwitchStatus success gameActivities:" + integer);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                    showLog(result);
                }
            }
        });
    }

    /**
     * Set the leaderboard switch status.
     * *
     * 设置排行榜开关状态。
     */
    public void onClickSetStatus() {
        EditText editText = findViewById(R.id.ranking_status_input);
        RankingsClient rankingsClient = Games.getRankingsClient(this);
        String numText = editText.getText().toString();
        if (TextUtils.isEmpty(numText)) {
            Toast.makeText(this, "Demo Input empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int stateValue = 0;
        try {
            stateValue = Integer.parseInt(numText);
        } catch (Exception e) {
            Toast.makeText(this, "Demo Input error", Toast.LENGTH_SHORT).show();
            return;
        }

        Task<Integer> task = rankingsClient.setRankingSwitchStatus(stateValue);
        task.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                show("setRankingSwitchStatus.onSuccess:" + integer);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                    showLog(result);
                }
            }
        });
    }

    /**
     * Asynchronously submit scores without custom units.
     * *
     * 以异步方式提交无自定义单位的分数。
     */
    public void onClickSubmitScore() {
        String rankingId = getRankingId();
        int score = getScores();
        RankingsClient rankingsClient = Games.getRankingsClient(this);
        StringBuffer buffer = new StringBuffer();
        buffer.append("Demo call submitScore(")
                .append(getShortRankingId(rankingId))
                .append(",")
                .append(score)
                .append(")");
        showLog(buffer.toString());
        rankingsClient.submitRankingScore(rankingId, score);
    }

    private String getShortRankingId(String rankingId) {
        if (rankingId.length() > 4) {
            return "*" + rankingId.substring(rankingId.length() - 4);
        }
        return rankingId;
    }

    private void showScoreSubmissionData(ScoreSubmissionInfo scoreSubmissionData) {
        StringBuffer buf = new StringBuffer();
        buf.append("getPlayerId():").append(scoreSubmissionData.getPlayerId()).append("\n");
        buf.append("getRankingId():").append(scoreSubmissionData.getRankingId()).append("\n");
        ScoreSubmissionInfo.Result result;
        for (int i = 0; i < 3; i++) {
            result = scoreSubmissionData.getSubmissionScoreResult(i);
            if (result != null) {
                buf.append("ScoreSubmissionInfo.Result->").append(i).append("\n");
                buf.append("displayScore:").append(result.displayScore).append(",isBest:").append(result.isBest);
                buf.append(",playerRawScore:")
                        .append(result.playerRawScore)
                        .append(",scoreTips:")
                        .append(result.scoreTips)
                        .append("\n");
            } else {
                buf.append("ScoreSubmissionInfo.Result->").append(i).append(" is null");
            }
        }
        showLog(buf.toString());
    }

    private int getScores() {
        EditText editText = findViewById(R.id.ranking_score_input);
        String scoreText = editText.getText().toString();
        int score = 0;
        try {
            score = Integer.parseInt(scoreText);
        } catch (Exception e) {

        }
        return score;
    }

    private String getRankingId() {
        EditText editText = findViewById(R.id.ranking_id_input);
        return editText.getText().toString();
    }

    private void initView() {
        findViewById(R.id.btn_get_ranking_intent).setOnClickListener(this);
        findViewById(R.id.btn_get_ranking_data).setOnClickListener(this);
        findViewById(R.id.btn_get_ranking_switch).setOnClickListener(this);
        findViewById(R.id.btn_set_ranking_switch).setOnClickListener(this);
        findViewById(R.id.btn_submitScore).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.init_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_ranking_intent:
                onClickGetRankingIntent();
                break;
            case R.id.btn_get_ranking_data:
                getRankingData();
                break;
            case R.id.btn_get_ranking_switch:
                onClickGetStatus();
                break;
            case R.id.btn_set_ranking_switch:
                onClickSetStatus();
                break;
            case R.id.btn_submitScore:
                onClickSubmitScore();
                break;
            case R.id.init_btn:
                super.init();
                break;
            case R.id.login_btn:
                signIn();
                break;
            default:
                break;
        }
    }
}