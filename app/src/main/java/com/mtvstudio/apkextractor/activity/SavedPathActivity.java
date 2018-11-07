package com.mtvstudio.apkextractor.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.adapter.PathAdapter;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SavedPathActivity extends AppCompatActivity {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private TextView mPathName;
    private ImageView mBackFolder;
    private PathAdapter mAdapter;
    private String mPath;
    private ActionBar mActionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_path);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mActionbar = getSupportActionBar();
        if (mActionbar != null) {
            mActionbar.setDisplayShowTitleEnabled(true);
            mActionbar.setDisplayHomeAsUpEnabled(true);
//            mActionbar.setDisplayShowHomeEnabled(true);
//            mActionbar.setTitle(mPath);
        }

        mPathName = (TextView) findViewById(R.id.path_name);
        mBackFolder = (ImageView) findViewById(R.id.btn_back_folder);

        mBackFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPath = getBackPath(mPath);
                if (TextUtils.isEmpty(newPath) == false) {
                    if (getListFile(newPath).size() >= 0) {
                        mPath = newPath;
                        updateContent();
                    } else {

                    }
                } else
                    finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPath.contains(Constant.APK_BASE_PATH)) {
                    Utility.setPath(mContext, mPath);
                }
                finish();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new PathAdapter(this, null);
        mAdapter.setListener(new PathAdapter.FolderClickListener() {
            @Override
            public void onClick(File file) {
                mPath = file.getPath();
                updateContent();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPath = Utility.getPath(this);
        updateContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<File> getListFile(String path) {
        AppLog.d("path:" + path);
        List<File> listFile = new ArrayList<>();
        File parentDir = new File(path);
        File[] files = parentDir.listFiles();
        if (files == null) {
            AppLog.d("cannot get list files");
            return listFile;
        }


        for (File file : files) {
            if (file.getName().startsWith("."))
                continue;
            listFile.add(file);
        }
        AppLog.d("size:" + listFile.size());
        return listFile;
    }

    private String getBackPath(String path) {
        String newPath = "/";
        String[] strs = path.split("/");
        if (strs != null && strs.length > 0) {
            for (int i = 0; i < strs.length - 1; i++) {
                if (TextUtils.isEmpty(strs[i]) == false)
                    newPath += strs[i] + "/";
            }
            return newPath;
        } else
            return null;

    }

    private void updateContent() {
        mPathName.setText(mPath);
        mAdapter.updateContents(getListFile(mPath));
    }
}
