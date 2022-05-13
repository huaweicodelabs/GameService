/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hms.codelab.game.achievement;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hms.codelab.game.R;
import com.huawei.hms.jos.games.achievement.Achievement;

import java.util.ArrayList;
import java.util.List;

/**
 * 成就列表适配器
 */
public class AchievementListAdapter extends RecyclerView.Adapter<AchievementListAdapter.ViewHolder> {
    private static final String TAG = "AchievementListAdapter";
    private final Context context;
    private OnBtnClickListener mBtnClickListener;
    private List<Achievement> achievementList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView achievementImage;
        CheckBox cBox;
        TextView achievementName;
        TextView achievementDes;
        TextView achievementUnlock;
        TextView achievementReveal;

        public ViewHolder(View view) {
            super(view);
            cBox = view.findViewById(R.id.cbox);
            achievementImage = view.findViewById(R.id.achievement_image);
            achievementName = view.findViewById(R.id.achievement_name);
            achievementDes = view.findViewById(R.id.achievement_des);
            achievementUnlock = view.findViewById(R.id.achievement_unlock);
            achievementReveal = view.findViewById(R.id.achievement_reveal);
        }
    }

    public AchievementListAdapter(Context mContext, List<Achievement> achievements, OnBtnClickListener btnClickListener) {
        context = mContext;
        achievementList = achievements;
        mBtnClickListener = btnClickListener;
    }

    public void setData(ArrayList<Achievement> achievements) {
        achievementList = achievements;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.achievement_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Achievement achievement = achievementList.get(position);
        // state 成就状态 1：显示 2：隐藏 3：解锁
        int state = achievement.getState();
        if (state == 2) {
            holder.achievementUnlock.setVisibility(View.GONE);
            holder.achievementReveal.setVisibility(View.VISIBLE);
        } else if (state == 1) {
            holder.achievementUnlock.setVisibility(View.VISIBLE);
            holder.achievementReveal.setVisibility(View.GONE);
        } else {
            holder.achievementUnlock.setVisibility(View.GONE);
            holder.achievementReveal.setVisibility(View.GONE);
        }
        if (allBtnInvisibility(holder.achievementUnlock, holder.achievementReveal)) {
            holder.cBox.setVisibility(View.GONE);
        } else {
            holder.cBox.setVisibility(View.VISIBLE);
        }
        final String achievementId = achievement.getId();

        Glide.with(context).load(achievement.getReachedThumbnailUri()).into(holder.achievementImage);

        holder.achievementName.setText(achievement.getDisplayName());
        holder.achievementDes.setText(achievement.getDescInfo());

        holder.itemView.setOnClickListener(v -> mBtnClickListener.onItemClick(position));

        holder.achievementUnlock.setOnClickListener(v -> mBtnClickListener.Unlock(achievementId, holder.cBox.isChecked()));
        holder.achievementReveal.setOnClickListener(v -> mBtnClickListener.reveal(achievementId, holder.cBox.isChecked()));
    }

    private boolean allBtnInvisibility(TextView achievementUnlock, TextView achievementReveal) {
        if (achievementUnlock.getVisibility() == View.GONE
                && achievementReveal.getVisibility() == View.GONE) {
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return achievementList.size();

    }

    public interface OnBtnClickListener {
        void onItemClick(int position);

        void Unlock(String achievementId, boolean isChecked);

        void reveal(String achievementId, boolean isChecked);
    }
}
