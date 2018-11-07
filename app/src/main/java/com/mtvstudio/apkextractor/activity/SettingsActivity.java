package com.mtvstudio.apkextractor.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.common.Utility;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.common.Constant;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int AUTO_BACKUP_TYPE = 1;
    public static final int BACKUP_NOTIFICATION_TYPE = 2;
    public static final int REQ_OVERLAY = 10;

    private Context mContext;
    private ActionBar mActionbar;

    LinearLayout mSavePathLn, mFileNameLn;
    RelativeLayout mAutoBackupRl, mNotificationRl;
    Switch mSwitchAutoBackup, mSwitchNotification;
    TextView mSavedPath;
    CheckBox chkAppName, chkPackageName, chkVersionName, chkVersionNumber;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mActionbar = getSupportActionBar();
        if (mActionbar != null) {
            mActionbar.setDisplayShowTitleEnabled(true);
            mActionbar.setDisplayHomeAsUpEnabled(true);
//            mActionbar.setDisplayShowHomeEnabled(true);
            mActionbar.setTitle(getString(R.string.settings));
        }

        mSavePathLn = (LinearLayout) findViewById(R.id.ln_saved_path);
        mFileNameLn = (LinearLayout) findViewById(R.id.ln_file_name);
        mAutoBackupRl = (RelativeLayout) findViewById(R.id.rl_auto_backup);
        mNotificationRl = (RelativeLayout) findViewById(R.id.rl_notification);
        mSwitchAutoBackup = (Switch) findViewById(R.id.btn_auto_backup);
        mSwitchNotification = (Switch) findViewById(R.id.btn_backup_notification);
        mSavedPath = (TextView) findViewById(R.id.saved_path);

        mSavePathLn.setOnClickListener(this);
        mFileNameLn.setOnClickListener(this);
        checkBackupApk();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSavedPath.setText(Utility.getPath(this));
        checkBackupApk();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppLog.d("home");
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ln_file_name:
                showSelectFileNameDialog();
                break;
            case R.id.ln_saved_path:
                startActivity(new Intent(this, SavedPathActivity.class));
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY) {
            AppLog.d("hasOverlayPermission: "+Utility.hasOverlayPermission(mContext));
            mSwitchNotification.setChecked(Utility.hasOverlayPermission(mContext));
            Utility.setBooleanSharePref(mContext, Constant.PRE_BACKUP_NOTIFICATION, Utility.hasOverlayPermission(mContext));
        }
    }

    private class SwitchToggleListener implements CompoundButton.OnCheckedChangeListener {
        private Switch mSwitch;
        private int mType;

        public SwitchToggleListener(int type) {
            mType = type;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (mType == AUTO_BACKUP_TYPE) {
                Utility.setBooleanSharePref(mContext, Constant.PRE_AUTO_BACKUP, isChecked);
                checkBackupApk();
            } else if (mType == BACKUP_NOTIFICATION_TYPE) {
                if (isChecked && !Utility.hasOverlayPermission(mContext)){
                    Toast.makeText(mContext, getString(R.string.permission_enable_overlay_popup), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQ_OVERLAY);
                }else {
                    Utility.setBooleanSharePref(mContext, Constant.PRE_BACKUP_NOTIFICATION, isChecked);
                }
            }
        }
    }

    private void checkBackupApk() {
        boolean isBackup = Utility.getBooleanSharePref(mContext, Constant.PRE_AUTO_BACKUP);

        mSwitchAutoBackup.setOnCheckedChangeListener(null);
        mSwitchAutoBackup.setChecked(isBackup);
        mSwitchAutoBackup.setOnCheckedChangeListener(new SwitchToggleListener(AUTO_BACKUP_TYPE));

        mSwitchNotification.setOnCheckedChangeListener(null);
        if (isBackup) {
            mNotificationRl.setVisibility(View.VISIBLE);
            boolean isNotify = Utility.getBooleanSharePref(mContext, Constant.PRE_BACKUP_NOTIFICATION);
            mSwitchNotification.setChecked(isNotify);
            if (!Utility.hasOverlayPermission(mContext)){
                mSwitchNotification.setChecked(false);
                Utility.setBooleanSharePref(mContext, Constant.PRE_BACKUP_NOTIFICATION, false);
            }
            mSwitchNotification.setOnCheckedChangeListener(new SwitchToggleListener(BACKUP_NOTIFICATION_TYPE));
        } else {
            mNotificationRl.setVisibility(View.GONE);
            Utility.setBooleanSharePref(mContext, Constant.PRE_BACKUP_NOTIFICATION, false);
        }
    }

    private void showSelectFileNameDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.select_file_name_dialog_layout);

        // set values for custom dialog components - text, image and button
        TextView yes = (TextView) dialog.findViewById(R.id.button1);
        chkAppName = (CheckBox) dialog.findViewById(R.id.checkBox1);
        chkPackageName = (CheckBox) dialog.findViewById(R.id.checkBox2);
        chkVersionName = (CheckBox) dialog.findViewById(R.id.checkBox3);
        chkVersionNumber = (CheckBox) dialog.findViewById(R.id.checkBox4);

        chkAppName.setChecked(Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_APP_NAME_KEY));
        chkPackageName.setChecked(Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_PACKAGE_NAME_KEY));
        chkVersionName.setChecked(Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NAME_KEY));
        chkVersionNumber.setChecked(Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NUMBER_KEY));

        // check enable or disable
        if (getNumberPrimaryFormatName() == 1){
            if (chkAppName.isChecked())
                chkAppName.setEnabled(false);
            if (chkPackageName.isChecked())
                chkPackageName.setEnabled(false);
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        chkAppName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utility.setBooleanSharePref(mContext, Constant.PRE_FILE_NAME_APP_NAME_KEY, isChecked);
                if (isChecked == true)
                    chkPackageName.setEnabled(true);
                else
                    chkPackageName.setEnabled(false);
            }
        });
        chkPackageName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utility.setBooleanSharePref(mContext, Constant.PRE_FILE_NAME_PACKAGE_NAME_KEY, isChecked);
                if (isChecked == true)
                    chkAppName.setEnabled(true);
                else
                    chkAppName.setEnabled(false);
            }
        });
        chkVersionName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utility.setBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NAME_KEY, isChecked);
            }
        });
        chkVersionNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utility.setBooleanSharePref(mContext, Constant.PRE_FILE_NAME_VERSION_NUMBER_KEY, isChecked);
            }
        });
        dialog.show();
    }

    private int getNumberPrimaryFormatName() {
        // app name, package name
        int number = 0;
        if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_APP_NAME_KEY))
            number++;
        if (Utility.getBooleanSharePref(mContext, Constant.PRE_FILE_NAME_PACKAGE_NAME_KEY))
            number++;
        return number;
    }
}
