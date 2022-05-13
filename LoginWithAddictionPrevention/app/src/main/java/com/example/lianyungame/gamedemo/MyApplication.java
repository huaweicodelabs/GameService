/*
Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
*/
package com.example.lianyungame.gamedemo;
import android.app.Application;
import com.huawei.hms.api.HuaweiMobileServicesUtil;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HuaweiMobileServicesUtil.setApplication(this);
    }
}
