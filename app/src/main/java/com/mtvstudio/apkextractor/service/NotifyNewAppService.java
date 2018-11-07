package com.mtvstudio.apkextractor.service;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.Utility;
import com.mtvstudio.apkextractor.data.AppInfo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class NotifyNewAppService extends Service {

    private WindowManager mWindowManager;
    private View mLockScreenView;
    private TextView mAppTitle, mOkBtn, mNoBtn;
    private Point szWindow = new Point();
    private AppInfo mAppInfo;

    @Override
    public IBinder onBind(Intent intent) {
        AppLog.d("onBind:");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLog.d("onStartCommand:");
        // draw app icon and name
        String packageName = intent.getStringExtra(Constant.EXTRA_PACKAGE_NAME);
        AppLog.d("onStartCommand:");
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e(e.toString());
            return START_STICKY;
        }
        mAppInfo = new AppInfo(this, applicationInfo);

        //Add the view to the window.
        WindowManager.LayoutParams windowManagerParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //Specify the view position
        windowManagerParams.gravity = Gravity.CENTER | Gravity.CENTER;

        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        getWindowManagerDefaultDisplay();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mLockScreenView = inflater.inflate(R.layout.lock_screen, null);
        mAppTitle = (TextView) mLockScreenView.findViewById(R.id.app_name);
        mOkBtn = (TextView) mLockScreenView.findViewById(R.id.btn_yes);
        mNoBtn = (TextView) mLockScreenView.findViewById(R.id.btn_no);
        mAppTitle.setText(mAppInfo.getName());
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.extractApk(NotifyNewAppService.this, mAppInfo.getPackageName());
                stopSelf();
            }
        });

        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        windowManagerParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mWindowManager.addView(mLockScreenView, windowManagerParams);

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.d("onCreate:");
    }


    private void closeService() {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.e("onDestroy:");
        /*  on destroy remove both view from window manager */
        if (mLockScreenView != null)
            mWindowManager.removeView(mLockScreenView);
    }


    private void getWindowManagerDefaultDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            mWindowManager.getDefaultDisplay().getSize(szWindow);
        else {
            int w = mWindowManager.getDefaultDisplay().getWidth();
            int h = mWindowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }
    }


}
