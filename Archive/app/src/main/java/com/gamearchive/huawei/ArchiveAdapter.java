/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamearchive.huawei;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.ArchivesClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.archive.ArchiveSummary;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom Archive List Adapter
 * 自定义存档列表适配器
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.MyViewHolder> {
    private final List<ArchiveSummary> summaries;
    private final Context context;
    private final ArchivesClient mClient;
    private View inflater;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ArchiveAdapter(List<ArchiveSummary> summaries, Context context, Activity activity) {
        this.summaries = summaries;
        this.context = context;
        mClient = Games.getArchiveClient(activity);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(context).inflate(R.layout.item_archive, parent, false);
        return new MyViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ArchiveSummary archiveSummary = summaries.get(position);
        holder.tv_archive_desc.setText(archiveSummary.getDescInfo());
        if (archiveSummary.hasThumbnail()) {
            Task<Bitmap> thumbnailTask = mClient.getThumbnail(archiveSummary.getId());
            thumbnailTask.addOnSuccessListener(new OnSuccessListener<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    holder.iv_archive_icon.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        Log.e("ArchiveAdapter", "statusCode：" + apiException.getStatusCode());
                    }
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public int getItemCount() {
        if (null != summaries) {
            return summaries.size();
        }
        return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_archive_icon;
        TextView tv_archive_desc;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_archive_icon = itemView.findViewById(R.id.iv_archive_icon);
            tv_archive_desc = itemView.findViewById(R.id.tv_archive_desc);
        }
    }
}
