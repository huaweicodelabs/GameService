/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hms.codelab.game.achievement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hms.codelab.game.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.AchievementsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.achievement.Achievement;
import com.huawei.hms.support.account.result.AuthAccount;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 成就列表页面
 */
public class AchievementListActivity extends Activity implements AchievementListAdapter.OnBtnClickListener {
    public static final String TAG = "codelab_achievement";
    public RecyclerView recyclerView;
    private ArrayList<Achievement> achievements = new ArrayList<>();
    private AchievementsClient client;
    private AchievementListActivity mContext;
    private AchievementListAdapter adapter;
    public ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_list);
        mContext = this;
        initViews();
        client = Games.getAchievementsClient(this);
        requestData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AchievementListAdapter(mContext, achievements, mContext);
        recyclerView.setAdapter(adapter);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(view -> finish());
    }

    /**
     * Get a list of achievements on the server
     * *
     * 获取服务器端或本地客户端的成就列表。
     */
    private void requestData() {
        Task<List<Achievement>> task = client.getAchievementList(true);
        task.addOnSuccessListener(data -> {
            if (data == null) {
                showLog("achievementBuffer is null");
                return;
            }
            Iterator<Achievement> iterator = data.iterator();
            achievements.clear();
            while (iterator.hasNext()) {
                Achievement achievement = iterator.next();
                achievements.add(achievement);
            }
            adapter.setData(achievements);
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                showLog(result);
            }
        });

    }


    private void showLog(String result) {
        Log.d(TAG, result);
    }

    /**
     * Jump to the achievement details activity.
     * *
     * 跳转成就详情界面
     *
     * @param position Position
     */
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, AchievementDetailActivity.class);
        Achievement achievement = achievements.get(position);
        intent.putExtra("achievementName", achievement.getDisplayName());
        intent.putExtra("achievementDes", achievement.getDescInfo());
        intent.putExtra("unlockedImageUri", achievement.getReachedThumbnailUri());
        intent.putExtra("rvealedImageUri", achievement.getVisualizedThumbnailUri());
        startActivity(intent);

    }

    /**
     * Unlock an achievement for the current player. This method needs to be called only
     * when the player completes the requirements specified by the achievement.
     * *
     * 为当前玩家解锁某个成就。只有当玩家完成成就指定的要求时才需要调用此方法。
     *
     * @param achievementId Achievement ID
     * @param isChecked     Whether selected
     */
    @Override
    public void Unlock(String achievementId, boolean isChecked) {
        if (!isChecked) {
            client.reach(achievementId);
        } else {
            performUnlockImmediate(client, achievementId);
        }
    }

    /**
     * Reveal a hidden achievement of the game. This method needs to be called only when
     * the player enters the achievement preset scene. If this achievement is unlocked for the current
     * player, this method will not work.
     * *
     * 立即揭示游戏的某个隐藏的成就。只有当玩家进入到成就预设的场景时才需要调用此方法。如果此成就对于当前玩家已
     * 解锁，此方法将不起作用。
     *
     * @param achievementId Achievement ID
     * @param isChecked     Whether selected
     */
    @Override
    public void reveal(String achievementId, boolean isChecked) {
        if (!isChecked) {
            client.visualize(achievementId);
        } else {
            performRevealImmediate(client, achievementId);
        }
    }


    private void performRevealImmediate(AchievementsClient client, String achievementId) {
        Task<Void> task = client.visualizeWithResult(achievementId);
        task.addOnSuccessListener(v -> {
            showLog("revealAchievement isSuccess");
            requestData();
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                showLog("achievement is not hidden" + result);
            }
        });
    }

    private void performUnlockImmediate(AchievementsClient client, String achievementId) {
        Task<Void> task = client.reachWithResult(achievementId);

        task.addOnSuccessListener(v -> {
            showLog("UnlockAchievement isSuccess");
            requestData();
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                showLog("achievement has been already unlocked" + result);
            }
        });
    }
}
