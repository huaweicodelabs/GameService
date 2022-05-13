/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hms.codelab.game.achievement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hms.codelab.game.R;

/**
 * 成就详情页面
 */
public class AchievementDetailActivity extends Activity {
    private ImageView achievementImageView;
    private TextView achievementNameTextView;
    private TextView achievementDesTextView;
    private TextView achievementStepTextView;
    private ImageView ivBackImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_detail);
        initView();
        initData();
    }

    private void initView() {
        achievementImageView = findViewById(R.id.achievement_image);
        achievementNameTextView = findViewById(R.id.achievement_name);
        achievementDesTextView = findViewById(R.id.achievement_des);
        achievementStepTextView = findViewById(R.id.achievement_step);
        ivBackImageView = findViewById(R.id.iv_back);
        ivBackImageView.setOnClickListener(view -> finish());
    }

    private void initData() {
        Intent intent = getIntent();
        String achievementName = intent.getStringExtra("achievementName");
        String achievementDes = intent.getStringExtra("achievementDes");
        Uri unlockedImageUri = (Uri) intent.getParcelableExtra("unlockedImageUri");
        Glide.with(this).load(unlockedImageUri).into(achievementImageView);
        achievementNameTextView.setText(achievementName);
        achievementDesTextView.setText(achievementDes);
    }

}
