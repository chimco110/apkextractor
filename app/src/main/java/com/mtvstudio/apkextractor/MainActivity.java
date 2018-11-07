package com.mtvstudio.apkextractor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mtvstudio.apkextractor.activity.SettingsActivity;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.MyApplication;
import com.mtvstudio.apkextractor.common.Utility;
import com.google.android.gms.ads.AdView;
import com.mtvstudio.apkextractor.data.AppInfo;
import com.mtvstudio.apkextractor.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomeFragment.SelectionItemListener, View.OnClickListener {
    private Context mContext;

    Toolbar mToolbar;
    private ActionBar mActionbar;
    private RelativeLayout mSelectionActionbar;
    private NavigationMenu mNavigationMenu;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private ImageView mBackSlectionBtn, mDownloadSectionBtn;
    private CheckBox mSelectAll;
    private TextView mNumberApps;
    private long timeBackKey = 0;
    public static boolean mIsShowSelection;
    private ProgressDialog progressDialog;
    private List<String> mPackageList = new ArrayList<>();
    private HomeFragment mFragment;
    private int mInterstitialType;
    private static final int ALL_APP_AD_TYPE = 1;
    private static final int SYSTEM_APP_AD_TYPE = 2;
    private static final int INSTALLED_APP_AD_TYPE = 3;
    private static final int SETTING_AD_TYPE = 4;
    private static boolean mIsUpdatedFragment = true;

    AdListener mAdListener = new AdListener() {
        @Override
        public void onAdClosed() {
            reLoadAd();
            HomeFragment fragment = null;
            switch (mInterstitialType) {
                case ALL_APP_AD_TYPE:
                    mActionbar.setTitle(getString(R.string.all_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.ALL_APPS);
                    break;
                case SYSTEM_APP_AD_TYPE:
                    mActionbar.setTitle(getString(R.string.system_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.SYSTEM_APPS);
                    break;
                case INSTALLED_APP_AD_TYPE:
                    mActionbar.setTitle(getString(R.string.installed_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.INSTALLED_APPS);
                    break;
                case SETTING_AD_TYPE:
                    startActivity(new Intent(mContext, SettingsActivity.class));
                    break;
                default:
                    fragment = new HomeFragment();
                    break;
            }

            if (fragment == null)
                return;
            mFragment = fragment;
        }
    };

    private void reLoadAd() {
        if (Constant.DEBUG_MODE) {
            // For test
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE232").build();
            mInterstitialAd.loadAd(adRequest);
        } else {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add Banner
        mAdView = (AdView) findViewById(R.id.adView);
        if (Constant.DEBUG_MODE == false) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("8FEC12B85C156BA246E37A8F714212DC").build();
            mAdView.loadAd(adRequest);
        }

        // Add Interstitial ad
        mInterstitialAd = new InterstitialAd(this);
        if (Constant.DEBUG_MODE) {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        }
        mInterstitialAd.setAdListener(mAdListener);
        reLoadAd();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
////
        mActionbar = getSupportActionBar();
        if (mActionbar != null) {
            mActionbar.setDisplayShowTitleEnabled(true);
            mActionbar.setDisplayHomeAsUpEnabled(true);
            mActionbar.setHomeButtonEnabled(true);
            mActionbar.setHomeAsUpIndicator(R.drawable.ic_nav);
        }

        mSelectionActionbar = (RelativeLayout) findViewById(R.id.selection_toolbar);
        mSelectionActionbar.setVisibility(View.GONE);
        mBackSlectionBtn = (ImageView) findViewById(R.id.back_selection_btn);
        mDownloadSectionBtn = (ImageView) findViewById(R.id.download_selection_btn);
        mSelectAll = (CheckBox) findViewById(R.id.select_all_chk);
        mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mFragment != null)
                    mFragment.sendData(isChecked);
            }
        });
        mNumberApps = (TextView) findViewById(R.id.number_apps);

        mBackSlectionBtn.setOnClickListener(this);
        mDownloadSectionBtn.setOnClickListener(this);

        mNavigationMenu = new NavigationMenu(this);
        mNavigationMenu.setUpDrawer(mToolbar);
        mContext = this;
        checkFirstRun();
        if (Utility.checkWriteExternalPermission(mContext)) {
            // count number time open to app
            int numberOpen = Utility.getIntPref(this, Constant.PRE_NUMBER_TIME_OPEN_APP_KEY);
            numberOpen++;
            if (Utility.isShowRatePopup(mContext, numberOpen)) {
                showRateDialog();
            }
            Utility.setIntSharePref(this, Constant.PRE_NUMBER_TIME_OPEN_APP_KEY, numberOpen);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d("onResume");
        if (mIsUpdatedFragment == false) {
            udpateFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_setting:
                if (Utility.isNeedLoadAd(mContext) && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    mInterstitialType = SETTING_AD_TYPE;
                    return true;
                }
                startActivity(new Intent(mContext, SettingsActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsShowSelection) {
            showSelectionToolbar(false);
            if (mFragment != null) {
                mFragment.sendData(false);
            }
            return;
        }
        if (timeBackKey >= (System.currentTimeMillis() - 2000)) {
            finish();
        } else {
            timeBackKey = System.currentTimeMillis();
            Toast.makeText(mContext, getString(R.string.exit_app_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSelection(int totalApp, List<String> packages) {
        AppLog.d("onSelection:");
        int number = 0;
        if (packages != null) {
            number = packages.size();
        }
        mPackageList = packages;
        String showNumberApps = number + " / " + totalApp;
        mNumberApps.setText(showNumberApps);
        if (totalApp == number)
            mSelectAll.setChecked(true);
        else
            mSelectAll.setChecked(false);
        showSelectionToolbar(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.download_selection_btn:
                downloadApps();
                break;
            case R.id.back_selection_btn:
                AppLog.d("back_selection_btn");
                showSelectionToolbar(false);
                if (mFragment != null) {
                    mFragment.sendData(false);
                }
                break;
            default:
                break;
        }
    }

    private void showSelectionToolbar(boolean isShow) {
        mIsShowSelection = isShow;
        if (isShow) {
            mSelectionActionbar.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.INVISIBLE);
        } else {
            mSelectionActionbar.setVisibility(View.GONE);
            mToolbar.setVisibility(View.VISIBLE);
        }
        AppLog.d("showSelectionToolbar:" + mIsShowSelection);
    }

    private void downloadApps() {
        if (mPackageList == null || mPackageList.size() == 0)
            return;
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMax(mPackageList.size());
        progressDialog.setTitle("Extracting ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDialog.getProgress() <= progressDialog.getMax()) {
                        try {
                            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(mPackageList.get(progressDialog.getProgress()), 0);
                            final AppInfo app = new AppInfo(getBaseContext(), applicationInfo);
                            //Check Storage permission
                            if (Utility.hasWriteExternalPermission(MyApplication.getContext()) == false) {
                                AppLog.d("There is no permission");
                                return;
                            }
                            if (app.isCopyFile(getBaseContext())) {
                                // create a handler to post messages to the main thread
                                AppLog.d("copied :" + app.getPackageName());
                            } else {
                                AppLog.d("not copied" + app.getPackageName());
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            AppLog.e(e.toString());
                        }
                        handle.sendMessage(handle.obtainMessage());
                        if (progressDialog.getProgress() == progressDialog.getMax()) {
                            progressDialog.dismiss();
                            if (mContext == null)
                                return;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MyApplication.getContext(), getString(R.string.extract_to, Utility.getPath(MyApplication.getContext())), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    AppLog.e(e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.incrementProgressBy(1);
        }
    };

    private class NavigationMenu implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

        MainActivity mActivity;
        private DrawerLayout mDrawerLayout;
        private ActionBar mActionbar;

        private NavigationView mNavigationView;

        public NavigationMenu(MainActivity activity) {
            mActivity = activity;
        }

        public void setUpDrawer(Toolbar toolbar) {
            mActionbar = mActivity.getSupportActionBar();
            mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
//                setNotificationBadgetCount();
                }
            };
            mDrawerLayout.addDrawerListener(toggle);
//        mDrawerLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            toggle.syncState();

            mNavigationView = (NavigationView) mActivity.findViewById(R.id.navView);
            mNavigationView.setItemIconTintList(null);
            mNavigationView.setNavigationItemSelectedListener(this);
            mNavigationView.getHeaderView(0).findViewById(R.id.txt_login);
            mNavigationView.getHeaderView(0).findViewById(R.id.txt_login).setOnClickListener(this);

//        mLnLogin = (LinearLayout) mNavigationView.getHeaderView(0).findViewById(R.id.layout_login);
//        mBadgetCountTv = (TextView) mNavigationView.getMenu().getItem(0).getActionView().findViewById(R.id.nav_notification_badget_tv);
//        mImgAvatar.setOnClickListener(this);

            // initialize Home fragment
            mActionbar.setTitle(mActivity.getString(R.string.all_app));
            mIsUpdatedFragment = false;
            mFragment = HomeFragment.newInstance(Constant.ALL_APPS);
            udpateFragment();
        }


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            AppLog.d("onNavigationItemSelected");
            if (mDrawerLayout == null)
                return true;

            HomeFragment fragment = null;
            mDrawerLayout.closeDrawer(GravityCompat.START);
            switch ((item.getItemId())) {
                case R.id.nav_all_app:
                    if (Utility.isNeedLoadAd(mContext) && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        mInterstitialType = ALL_APP_AD_TYPE;
                        return true;
                    }
                    mActionbar.setTitle(mActivity.getString(R.string.all_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.ALL_APPS);
                    break;
                case R.id.nav_system_app:
                    if (Utility.isNeedLoadAd(mContext) && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        mInterstitialType = SYSTEM_APP_AD_TYPE;
                        return true;
                    }
                    mActionbar.setTitle(mActivity.getString(R.string.system_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.SYSTEM_APPS);
                    break;
                case R.id.nav_installed_app:
                    if (Utility.isNeedLoadAd(mContext) && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        mInterstitialType = INSTALLED_APP_AD_TYPE;
                        return true;
                    }
                    mActionbar.setTitle(mActivity.getString(R.string.installed_app));
                    mIsUpdatedFragment = false;
                    fragment = HomeFragment.newInstance(Constant.INSTALLED_APPS);
                    break;
                case R.id.nav_setting:
                    AppLog.d("nav_setting");
                    if (Utility.isNeedLoadAd(mContext) && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        mInterstitialType = SETTING_AD_TYPE;
                        return true;
                    }
                    mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
                    break;
                case R.id.nav_share_app:
                    Utility.shareApp(mContext);
                    break;
                case R.id.nav_feedback:
                    Utility.sendEmail(mContext);
                    break;
                case R.id.nav_rate_us:
                    Utility.rateApp(mContext);
                    break;
                case R.id.nav_more_app:
                    Utility.moreApp(mContext);
                    break;
                default:
                    fragment = new HomeFragment();
                    break;
            }

            if (fragment == null)
                return false;
            mFragment = fragment;
            // Insert the fragment by replacing any existing fragment
            udpateFragment();
            return true;
        }

        @Override
        public void onClick(View view) {
            if (mDrawerLayout != null) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
//        switch (view.getId()) {
//            case R.id.img_avatar:
//                if (isLogin) {
//                    AppLog.d(NavigationPresenter.TAG, "click enter user profile");
//                    ApplicationUtils.startActivity(mActivity, new Intent(mActivity, UserProfileActivity.class));
//                } else {
//                    Intent intent_login = new Intent(mActivity, UserLoginActivity.class);
//                    ApplicationUtils.startActivity(mActivity, intent_login);
//                }
//                break;
//            default:
//                break;
//        }
        }
    }

    private void udpateFragment() {
        mIsUpdatedFragment = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, mFragment).commit();
    }

    private void checkFirstRun() {
        final int DOESNT_EXIST = -1;
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        // Get saved version code
        int savedVersionCode = Utility.getIntPref(this, Constant.PRE_VERSION_CODE_KEY);
        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {
            // TODO This is a new install (or the user cleared the shared preferences)
            Utility.setBooleanSharePref(mContext, Constant.PRE_FILE_NAME_PACKAGE_NAME_KEY, true);
        } else if (currentVersionCode > savedVersionCode) {
            // TODO This is an upgrade
        }
        // Update the shared preferences with the current version code
        Utility.setIntSharePref(this, Constant.PRE_VERSION_CODE_KEY, currentVersionCode);
    }

    private void showRateDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.custom_dialog_layout);

        // set values for custom dialog components - text, image and button
        TextView yes = (TextView) dialog.findViewById(R.id.btn_yes);
        TextView maybe = (TextView) dialog.findViewById(R.id.btn_maybe);
        TextView no = (TextView) dialog.findViewById(R.id.btn_no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Utility.setIntSharePref(mContext, Constant.PRE_RATE_TYPE_KEY, Constant.TYPE_YES);
                Utility.rateApp(mContext);
            }
        });
        maybe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.setIntSharePref(mContext, Constant.PRE_RATE_TYPE_KEY, Constant.TYPE_MAYBE);
                dialog.dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.setIntSharePref(mContext, Constant.PRE_RATE_TYPE_KEY, Constant.TYPE_NO);
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Utility.setIntSharePref(mContext, Constant.PRE_RATE_TYPE_KEY, Constant.TYPE_MAYBE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
