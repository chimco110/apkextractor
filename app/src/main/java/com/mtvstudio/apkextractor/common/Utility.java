package com.mtvstudio.apkextractor.common;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.BuildConfig;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.service.ApkExtractService;
import com.mtvstudio.apkextractor.data.AppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utility {
    public static void setIntSharePref(Context context, String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setBooleanSharePref(Context context, String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static int getIntPref(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        int value = pref.getInt(key, -1);
        return value;
    }

    public static boolean getBooleanSharePref(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        boolean value = pref.getBoolean(key, false);
        return value;
    }

    public static void setPath(Context context, String path) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constant.PRE_PATH_KEY, path);
        editor.apply();
    }

    public static String getPath(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        String path = pref.getString(Constant.PRE_PATH_KEY, Constant.APK_PATH);
        return path;
    }

    public static boolean getSystemApp(Context context) {
        return true;
    }

    public static List<AppInfo> getListAppInfo(Context context, int type) {
        ArrayList<AppInfo> listApp = new ArrayList();
        List<ApplicationInfo> apps = getListApp(context, type);
        for (ApplicationInfo app : apps) {
            listApp.add(new AppInfo(context, app));
        }

        Collections.sort(listApp, new Comparator<AppInfo>() {
            public int compare(AppInfo obj1, AppInfo obj2) {
                // ## Ascending order
                return obj1.getName().compareToIgnoreCase(obj2.getName()); // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
            }
        });

        return listApp;
    }

    private static List<ApplicationInfo> getListApp(Context context, int type) {
        // need to ignore paid apps

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<ApplicationInfo> installedApps = new ArrayList<>();
        AppLog.d("size app:" + apps.size());
        for (ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                if (type != Constant.INSTALLED_APPS)
                    installedApps.add(app);
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
                if (type != Constant.INSTALLED_APPS)
                    installedApps.add(app);
            } else {
                if (type != Constant.SYSTEM_APPS) {
                    installedApps.add(app);
                }
            }
        }
        AppLog.d("size installedApps:" + installedApps.size());
//        for (ApplicationInfo app : installedApps){
//            AppLog.d("package name:" + app.packageName);
//            AppLog.d("loadLabel:" + app.loadLabel(pm));
//            AppLog.d("Apk file path:" + app.sourceDir);
////            copyFile(app.sourceDir, "/sdcard/myappFolder/", app.packageName + ".apk");
//        }
        return installedApps;
    }

    public static List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {

            if (file.isDirectory()) {
                AppLog.d("getListFiles Directory name:" + file.getName());
                inFiles.addAll(getListFiles(file));
            } else {
                AppLog.d("getListFiles name:" + file.getName());
                if (file.getName().endsWith(".csv")) {
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    public static void createFolder(Context context) {
//        String path = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/ApkExtractor";
        File folder = new File(Constant.APK_PATH);
        if (!folder.exists()) {
            boolean isCreated = folder.mkdir();
            AppLog.d("created Folder:" + isCreated);
        } else {
            AppLog.d("folder is exists");
        }
//        File directory = new File("/sdcard/myappFolder/");
//        AppLog.d("createFolder directory.isDirectory():" + directory.isDirectory());
//        if(directory.isDirectory() == false){
//            directory.mkdirs();
//        }
//        AppLog.d("createFolder directory.isAbsolute():" + directory.isAbsolute());
//        AppLog.d("createFolder directory.isFile():" + directory.isFile());
    }


    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public static boolean copyFile(String inputPath, String outputPath, String outputFileName) {
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + outputFileName);

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

        } catch (FileNotFoundException fnfe1) {
            AppLog.e(fnfe1.toString());
            return false;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return false;
        }
        return true;
    }

    public static void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        } catch (FileNotFoundException fnfe1) {
            AppLog.e(fnfe1.toString());
        } catch (Exception e) {
            AppLog.e(e.toString());
        }

    }

    public static void deleteFile(String inputPath, String inputFile) {
        try {
            // delete the original file
            new File(inputPath + inputFile).delete();
        } catch (Exception e) {
            AppLog.e(e.toString());
        }
    }

    public static boolean hasWriteExternalPermission(Context context) {
        int res = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkWriteExternalPermission(Context context) {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AppLog.d("permission is denied");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                AppLog.d("permission2 is denied");
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//                AppLog.d("permission is granted");
//
//                // No explanation needed, we can request the permission.
//
////                ActivityCompat.requestPermissions(context,
////                        new String[]{Manifest.permission.READ_CONTACTS},
////                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
        }
        return true;
    }

    public static void extractApk(Context context, String str) {
        Intent intent = new Intent(context, ApkExtractService.class);
        intent.putExtra("package_name", str);
        context.startService(intent);
    }

    public static boolean isNeedLoadAd(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        int number = pref.getInt(Constant.PRE_AD_COUNT_KEY, 0);
        if (number >= 2) {
            putAd(context, 1);
            return true;
        }
        putAd(context, number + 1);
        return false;
    }

    private static void putAd(Context context, int count) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARE_PRE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constant.PRE_AD_COUNT_KEY, count);
        editor.apply();
    }

    public static void moreApp(Context context) {
        Uri uri = Uri.parse("market://dev?id=MTV+studio");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/developer?id=MTV+studio")));
        }
    }

    public static void shareApp(Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
            String link = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
            i.putExtra(Intent.EXTRA_TEXT, link);
            context.startActivity(Intent.createChooser(i, context.getString(R.string.share)));
        } catch(Exception e) {
            Toast.makeText(context, context.getString(R.string.error_share), Toast.LENGTH_SHORT).show();
        }
    }

    public static void rateApp(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public static void sendEmail(Context context) {
        String title = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " " + context.getString(R.string.feedback);

        Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, getDeviceInfo());
        intent.setData(Uri.parse("mailto:mtvstudio2017@gmail.com")); // or just "mailto:" for blank
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
        context.startActivity(intent);
    }

    private static String getDeviceInfo() {
        String content = Build.BRAND + " " + Build.MODEL
                + " \nAndroid OS: version " + Build.VERSION.SDK_INT;
        return content;
    }

    public static boolean isShowRatePopup(Context context, int numberOpen) {
        boolean isShow = false;
        int type = getIntPref(context, Constant.PRE_RATE_TYPE_KEY);
        if (type == Constant.TYPE_YES) {
            isShow = false;
        } else if (type == Constant.TYPE_MAYBE) {
            if (numberOpen % 5 == 2)
                isShow = true;
        } else if (type == Constant.TYPE_NO) {
            if (numberOpen % 10 == 7)
                isShow = true;
        } else {
            if (numberOpen % 5 == 2)
                isShow = true;
        }
        return isShow;
    }

    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, 0);
            return false;
        }
        return true;
    }
}
