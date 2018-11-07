package com.mtvstudio.apkextractor.data;

import com.mtvstudio.apkextractor.AppLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderInfo {
    private String mName;
    private int mNumberItems;
    private String mPath;

    public FolderInfo(String path){
        mPath = path;
    }

    public String getName() {
        return mName;
    }

    public int getNumberItems() {
        File parentDir = new File(mPath);
        File[] files = parentDir.listFiles();
        return files.length;
    }

    public String getPath() {
        return mPath;
    }

    private File[] getListFiles() {
        File parentDir = new File(mPath);
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.add(file);
            } else {
                AppLog.d("getListFiles name:" + file.getName());
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return files;
    }


    private List<File> getListFiles(File parentDir) {
        AppLog.d("path:" + parentDir.getPath());
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {

            if (file.isDirectory()) {
                AppLog.d("getListFiles Directory name:" + file.getName());
                inFiles.addAll(getListFiles(file));
            } else {
                AppLog.d("getListFiles name:" + file.getName());
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
