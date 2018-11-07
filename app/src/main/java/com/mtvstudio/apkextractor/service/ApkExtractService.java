package com.mtvstudio.apkextractor.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.support.v7.recyclerview.BuildConfig;
import android.widget.Toast;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.common.MyApplication;
import com.mtvstudio.apkextractor.common.Utility;
import com.mtvstudio.apkextractor.data.AppInfo;

public class ApkExtractService extends IntentService {
    public ApkExtractService() {
        super(BuildConfig.VERSION_NAME);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(intent.getStringExtra("package_name"), 0);
            final AppInfo app = new AppInfo(getBaseContext(), applicationInfo);
            //Check Storage permission
            if (Utility.hasWriteExternalPermission(MyApplication.getContext()) == false) {
                AppLog.d("There is no permission");
                return;
            }
            if (app.isCopyFile(getBaseContext())) {
                // create a handler to post messages to the main thread
                AppLog.d("copied");
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), getString(R.string.extract_to, app.getOutputDirectoryApk()), Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                AppLog.d("not copied");
            }
        } catch (NameNotFoundException e) {
            AppLog.e(e.toString());
        }
    }
}
