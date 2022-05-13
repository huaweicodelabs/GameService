/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package com.gamearchive.huawei;

import android.app.Application;

import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.huawei.hms.jos.games.ArchivesClient;
import com.huawei.hms.jos.games.archive.ArchiveSummary;

import java.util.List;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HuaweiMobileServicesUtil.setApplication(this);
    }
}
