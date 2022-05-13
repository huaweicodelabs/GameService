/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamearchive.huawei;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.ArchivesClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.archive.Archive;
import com.huawei.hms.jos.games.archive.ArchiveConstants;
import com.huawei.hms.jos.games.archive.ArchiveSummary;
import com.huawei.hms.jos.games.archive.OperationResult;

import java.util.List;

/**
 * Display the customized archive list.
 * 展示自定义的存档列表
 */
public class DisplayArchiveUserselfActivity extends AppCompatActivity {
    private static final String TAG = "DaUserselfActivity";
    private ArchivesClient mArchivesClient;

    private ArchiveAdapter mArchiveAdapter;
    private List<ArchiveSummary> mSummaries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_archive_userself);

        RecyclerView rcv_archive = findViewById(R.id.rcv_archive);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rcv_archive.setLayoutManager(manager);
        mArchivesClient = Games.getArchiveClient(this);

        Task<List<ArchiveSummary>> archiveSummaryListTask = mArchivesClient.getArchiveSummaryList(false);
        archiveSummaryListTask.addOnSuccessListener(new OnSuccessListener<List<ArchiveSummary>>() {
            @Override
            public void onSuccess(List<ArchiveSummary> archiveSummaries) {
                if (archiveSummaries != null) {
                    mSummaries = archiveSummaries;
                    Log.i(TAG, " archiveSummaries size" + archiveSummaries.size());
                    mArchiveAdapter = new ArchiveAdapter(archiveSummaries, DisplayArchiveUserselfActivity.this, DisplayArchiveUserselfActivity.this);
                    rcv_archive.setAdapter(mArchiveAdapter);
                    setOnItemClick();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e(TAG, "statusCode：" + apiException.getStatusCode());
                }
            }
        });


    }

    /**
     * Set list entry click event.
     * 设置列表条目点击事件
     */
    private void setOnItemClick() {
        if (mArchiveAdapter != null) {
            mArchiveAdapter.setOnItemClickListener(new ArchiveAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    ArchiveSummary archiveSummary = mSummaries.get(position);
                    String archiveId = archiveSummary.getId();
                    String descInfo = archiveSummary.getDescInfo();
                    String fileName = archiveSummary.getFileName();
                    Log.i(TAG, "click archiveId：" + archiveId + "，fileName：" + fileName+ "，descInfo：" + descInfo);
                    Task<OperationResult> operationResultTask = mArchivesClient.loadArchiveDetails(archiveSummary, ArchiveConstants.STRATEGY_TOTAL_PROGRESS);
                    operationResultTask.addOnSuccessListener(new OnSuccessListener<OperationResult>() {
                        @Override
                        public void onSuccess(OperationResult operationResult) {
                            Log.i(TAG, "isDifference:" + ((operationResult == null) ? "" : operationResult.isDifference()));
                            if (operationResult != null && !operationResult.isDifference()) {
                                Archive archive = operationResult.getArchive();
                                if (archive != null && archive.getSummary() != null) {
                                    ArchiveSummary summary = archive.getSummary();
                                    String archiveId = summary.getId();
                                    String descInfo = summary.getDescInfo();
                                    String fileName = summary.getFileName();
                                    Log.i(TAG, "get archive info success archiveId：" + archiveId + "，fileName：" + fileName+ "，descInfo：" + descInfo);
                                }
                            } else {
                                //处理冲突
                                Log.i(TAG, "Handling Conflicts");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                Log.e(TAG, "statusCode:" + apiException.getStatusCode());
                            }
                        }
                    });
                }
            });
        }
    }
}