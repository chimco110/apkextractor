package com.mtvstudio.apkextractor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.Utility;
import com.mtvstudio.apkextractor.service.NotifyNewAppService;


public class AppListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppLog.d("action:" + action);
        if (TextUtils.isEmpty(action)) {
            AppLog.e("action is null");
            return;
        }

        if (Utility.getBooleanSharePref(context, Constant.PRE_AUTO_BACKUP) == false) {
            AppLog.e("no auto backup");
            return;
        }
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        AppLog.d("packageName:" + packageName);
        AppLog.d("packageName111:" + Utility.getBooleanSharePref(context, Constant.PRE_BACKUP_NOTIFICATION));
        if (Utility.getBooleanSharePref(context, Constant.PRE_BACKUP_NOTIFICATION)) {

            Intent intent1 = new Intent(context, NotifyNewAppService.class);
            intent1.putExtra(Constant.EXTRA_PACKAGE_NAME, packageName);
            context.startService(intent1);
            return;
        }
        Utility.extractApk(context, packageName);
    }

}
