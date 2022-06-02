/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.example.lianyungame.gamedemo;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.annotation.Nullable;//
import com.huawei.hmf.tasks.OnCanceledListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.RankingsClient;
import com.huawei.hms.jos.games.ranking.Ranking;
import com.huawei.hms.jos.games.ranking.RankingScore;
import com.huawei.hms.jos.games.ranking.RankingVariant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RankingDataActivity extends BaseActivity {

    @BindView(R.id.et_ranking_id)
    EditText etRankingId;

    @BindView(R.id.sp_time_dimension)
    Spinner timeSpinner;

    @BindView(R.id.ed_offsetPlayerRank)
    EditText etOffsetPlayerRank;

    @BindView(R.id.ed_maxResults)
    EditText etMaxResults;

    @BindView(R.id.ed_pageDirection)
    EditText etPageDirection;

    @BindView(R.id.sp_isRealTime)
    Spinner spIsRealTimeSpinner;

    private ArrayAdapter<String> adapter;

    private RankingsClient rankingsClient;

    private int getMaxResults() {
        try {
            return Integer.parseInt(etMaxResults.getText().toString());
        } catch (Exception ex) {
            return -1;
        }
    }

    private long getOffsetPlayerRank() {
        try {
            return Long.parseLong(etOffsetPlayerRank.getText().toString());
        } catch (Exception ex) {
            return -1;
        }
    }

    public int getPageDirection() {
        try {
            return Integer.parseInt(etPageDirection.getText().toString());
        } catch (Exception ex) {
            return -1;
        }
    }

    private boolean isRealTime() {
        int position = spIsRealTimeSpinner.getSelectedItemPosition();
        return position == 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_data);
        ButterKnife.bind(this);
        rankingsClient = Games.getRankingsClient(this);
        initTimeDimensionSpinner();
        initIsRealTimeSpinner();
    }

    /**
     * Init spinner for time dimension.
     * *
     * 初始化时间维度的下拉列表。
     */
    private void initTimeDimensionSpinner() {
        String[] ctype = new String[]{"day", "week", "all", "default", "invalid value"};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
    }

    /**
     * Init spinner for realtime.
     * *
     * 初始即时性选择的下拉列表
     */
    private void initIsRealTimeSpinner() {
        String[] ctype = new String[]{"true", "false",};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIsRealTimeSpinner.setAdapter(adapter);
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
     * Get the score information of the currently logged in player in the specified leaderboard,
     * and support the specified time dimension.
     * *
     * 获取当前登录玩家在指定排行榜中的分数信息，支持指定时间维度。
     */
    @OnClick(R.id.bnt_current_player_ranking_score)
    public void loadCurrentPlayerRankingScore() {
        String rankingId = etRankingId.getText().toString();
        int timeDimension = timeSpinner.getSelectedItemPosition();
        StringBuffer buffer = new StringBuffer();
        buffer.append("getCurrentPlayerRankingScore(").append(getShortRankingId(rankingId));
        buffer.append(",").append(timeDimension).append(")\n");

        Task<RankingScore> task = rankingsClient.getCurrentPlayerRankingScore(rankingId, timeDimension);
        addRankingScoreListener(task, buffer.toString());
    }

    private String getShortRankingId(String rankingId) {
        if (rankingId.length() > 4) {
            return "*" + rankingId.substring(rankingId.length() - 4);
        }
        return rankingId;
    }

    private void addRankingScoreListener(final Task<RankingScore> task, final String method) {

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                showLog(method + " failure. exception: " + e);
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<RankingScore>() {
            @Override
            public void onSuccess(RankingScore s) {
                showLog(method + " success. ");
                showScoreTaskLog(task);
            }
        });
        task.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                showLog(method + " canceled. ");
            }
        });
    }

    private void showScoreTaskLog(Task<RankingScore> task) {
        if (task.getResult() == null) {
            showLog("RankingScore result is null");
            return;
        }

        showLog("=======RankingScore=======");
        RankingScore s = task.getResult();
        printRankingScoreLog(s, 0);
        printScoreTaskException(task);
    }

    private void printRankingScoreLog(RankingScore s, int index) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("------RankingScore " + index + "------\n");
        if (s == null) {
            buffer.append("rankingScore is null\n");
            return;
        }

        String displayScore = s.getRankingDisplayScore();
        buffer.append("    DisplayScore: " + displayScore).append("\n");
        buffer.append("    TimeDimension: " + s.getTimeDimension()).append("\n");
        buffer.append("    RawPlayerScore: " + s.getPlayerRawScore()).append("\n");
        buffer.append("    PlayerRank: " + s.getPlayerRank()).append("\n");
        String displayRank = s.getDisplayRank();
        buffer.append("    getDisplayRank: " + displayRank).append("\n");
        buffer.append("    ScoreTag: " + s.getScoreTips()).append("\n");
        buffer.append("    updateTime: " + s.getScoreTimestamp()).append("\n");
        String playerDisplayName = s.getScoreOwnerDisplayName();
        buffer.append("    PlayerDisplayName: " + playerDisplayName).append("\n");
        buffer.append("    PlayerHiResImageUri: " + s.getScoreOwnerHiIconUri()).append("\n");
        buffer.append("    PlayerIconImageUri: " + s.getScoreOwnerIconUri()).append("\n\n");
        showLog(buffer.toString());
    }

    private void printRankingVariantList(ArrayList<RankingVariant> list, StringBuffer buffer) {
        if (list.size() == 0) {
            return;
        }
        int index = 0;
        for (RankingVariant variant : list) {
            if (variant != null) {
                buffer.append("---RankingVariant ").append(index).append("---\n");
                buffer.append("   getDisplayRanking:").append(variant.getDisplayRanking()).append("\n");
                buffer.append("   getPlayerDisplayScore:").append(variant.getPlayerDisplayScore()).append("\n");
                buffer.append("   getRankTotalScoreNum:").append(variant.getRankTotalScoreNum()).append("\n");
                buffer.append("   getPlayerRank:").append(variant.getPlayerRank()).append("\n");
                buffer.append("   getPlayerScoreTips:").append(variant.getPlayerScoreTips()).append("\n");
                buffer.append("   getPlayerRawScore:").append(variant.getPlayerRawScore()).append("\n");
                buffer.append("   timeDimension:").append(variant.timeDimension()).append("\n");
                buffer.append("   hasPlayerInfo:").append(variant.hasPlayerInfo()).append("\n");
            } else {
                buffer.append("---RankingVariant ").append(index).append(" is null ----\n");
            }
            index++;
        }
    }

    private void printScoreTaskException(Task<RankingScore> task) {
        if (task.getException() != null) {
            showLog(task.getException().getLocalizedMessage());
            showLog(task.getException().getMessage());
            showLog("task.getException().getCause() " + task.getException().getCause());
            showLog("task.getException().getStackTrace() " + Arrays.toString(task.getException().getStackTrace()));
            showLog("task.getException().getClass()" + task.getException().getClass());
        }
    }
}
