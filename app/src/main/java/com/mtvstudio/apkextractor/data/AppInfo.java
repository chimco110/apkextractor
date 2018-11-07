package com.mtvstudio.apkextractor.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.MyApplication;
import com.mtvstudio.apkextractor.common.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sev_user on 5/30/2017.
 */

public class AppInfo {
    private String mName;
    private String mPackageName;
    private String mSourceDir;
    private Drawable mIcon;
    private String mApkName;
    private boolean mIsChecked;
    private Context mContext;

    private AppInfo(){

    }

    public AppInfo(Context context, ApplicationInfo app){
        PackageManager pm = context.getPackageManager();
        mContext = context;
        mName = app.loadLabel(pm).toString();
        mPackageName = app.packageName;
        mSourceDir = app.sourceDir;
        mApkName = mPackageName + ".apk";
        mIsChecked = false;
    }

    public void setIsChecked(boolean isChecked){
        mIsChecked = isChecked;
    }

    public boolean getIsChecked(){
        return mIsChecked;
    }

    public Drawable getIcon() {
        if (mIcon != null)
            return mIcon;
        try {
            mIcon = MyApplication.getContext().getPackageManager().getApplicationIcon(mPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            mIcon = null;
            AppLog.e(e.toString());
        }
        return mIcon;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getSourceDir() {
        return mSourceDir;
    }

    public boolean isCopyFile(Context context){
        InputStream in = null;
        OutputStream out = null;
        Utility.createFolder(context);
        try {

            //create output directory if it doesn't exist
            File dir = new File (Constant.APK_PATH);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(mSourceDir);
            out = new FileOutputStream(getOutputDirectoryApk());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            AppLog.e(fnfe1.toString());
            return false;
        }
        catch (Exception e) {
            AppLog.e(e.toString());
            return false;
        }
        return true;
    }

    public String getOutputDirectoryApk(){
        String apkName = "";
        if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_APP_NAME_KEY)){
            apkName = addName(apkName, mName);
        }
        if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_PACKAGE_NAME_KEY)){
            apkName = addName(apkName, mPackageName);
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mPackageName, 0);
            if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NAME_KEY)){
                apkName = addName(apkName, packageInfo.versionName);
            }
            if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NUMBER_KEY)){
                apkName = addName(apkName, String.valueOf(packageInfo.versionCode));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        AppLog.d("apkName:"+apkName);
        return Utility.getPath(MyApplication.getContext()) + apkName + ".apk";
    }

    private String addName(String name, String subName){
        String newName;
        if (TextUtils.isEmpty(name)){
            newName = subName;
        }else
            newName = name + "_" + subName;
        return newName;
    }
}
