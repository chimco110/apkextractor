package com.mtvstudio.apkextractor.fragment;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.MainActivity;
import com.mtvstudio.apkextractor.R;
import com.mtvstudio.apkextractor.activity.SettingsActivity;
import com.mtvstudio.apkextractor.adapter.AppInfoAdapter;
import com.mtvstudio.apkextractor.common.Constant;
import com.mtvstudio.apkextractor.common.MoreBottomSheet;
import com.mtvstudio.apkextractor.common.MyApplication;
import com.mtvstudio.apkextractor.common.Utility;
import com.mtvstudio.apkextractor.data.AppInfo;
import com.mtvstudio.apkextractor.service.NotifyNewAppService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppInfoAdapter mAdapter;
    private HandleAppInfo mHandler;
    private SearchView searchView;
    private List<AppInfo> mAllApps = new ArrayList<>();
    private List<AppInfo> mCurrentAppList = new ArrayList<>();
    private int mTypeApp;
    private Menu mMenu;
    private SelectionItemListener mSelectionListener;
    private MoreBottomSheet mBottomSheetDialog;
    private ProgressDialog dialogExtrackApk;
    final static int TYPE_EXTRACT = 1;
    final static int TYPE_SHARE = 2;
    private MoreBottomSheet.ClickListener mMoreListener = new MoreBottomSheet.ClickListener() {
        @Override
        public void onClick(AppInfo app, int type) {
            switch (type) {
                case MoreBottomSheet.EXTRACT_APK_TYPE:
//                    Intent intent1 = new Intent(getContext(), NotifyNewAppService.class);
//                    intent1.putExtra(Constant.EXTRA_PACKAGE_NAME, app.getPackageName());
//                    getContext().startService(intent1);
                    extractAPK(app.getPackageName());
                    break;
                case MoreBottomSheet.SHARE_APK_TYPE:
                    shareAPK(app);
                    break;
                case MoreBottomSheet.RUN_APP_TYPE:
                    runApp(app);
                    break;
                case MoreBottomSheet.APP_INFO_TYPE:
                    showAppInfo(app);
                    break;
                case MoreBottomSheet.UNINSTALL_APP_TYPE:
                    uninstallApp(app);
                    break;
                default:
                    break;
            }
            mBottomSheetDialog.dismissPopupWindow();
        }
    };

    public interface SelectionItemListener {
        void onSelection(int totalApp, List<String> packages);
    }

    public static HomeFragment newInstance(int type) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(Constant.TYPE_APP, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mTypeApp = getArguments().getInt(Constant.TYPE_APP, Constant.ALL_APPS);
        } else
            mTypeApp = Constant.ALL_APPS;

        mSelectionListener = (SelectionItemListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.d("on create view");
        View view = inflater.inflate(R.layout.fragment_home, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout.setRefreshing(true);

        mAdapter = new AppInfoAdapter(getContext(), null);
        mAdapter.setListener(mItemListener);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d("on Resume");
        if (mHandler == null) {
            mHandler = new HandleAppInfo();
            mHandler.execute();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        mMenu = menu;
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //some operation
                    return false;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //some operationspl
                    AppLog.d("onClick");
                }
            });
            EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint("Search");
            View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // use this method when query submitted
                    Toast.makeText(getContext(), query, Toast.LENGTH_SHORT).show();

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    AppLog.d("onQueryTextChange");
                    if (TextUtils.isEmpty(newText))
                        if (mAdapter != null) {
                            mCurrentAppList.clear();
                            mCurrentAppList.addAll(mAllApps);
                            mAdapter.updateContents(mCurrentAppList);
                        }
                    if (mAllApps != null && mAllApps.size() >= 0) {
                        ArrayList<AppInfo> newList = new ArrayList();
                        for (AppInfo app : mAllApps) {
                            if (app.getName().toLowerCase().contains(newText.toLowerCase())) {
                                newList.add(app);
                            }
                        }
                        if (mAdapter != null) {
                            mCurrentAppList.clear();
                            mCurrentAppList.addAll(newList);
                            mAdapter.updateContents(mCurrentAppList);
                        }
                    }
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }
    }

    @Override
    public void onRefresh() {
        AppLog.d("on Refresh");
        if (MainActivity.mIsShowSelection) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        mSwipeRefreshLayout.setRefreshing(true);
        mHandler = new HandleAppInfo();
        mHandler.execute();
    }

    private class HandleAppInfo extends AsyncTask<String, Void, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(String... params) {
            AppLog.d("do In Back ground");
            return Utility.getListAppInfo(MyApplication.getContext(), mTypeApp);
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            AppLog.d("on Post Execute");
            if (mRecyclerView == null) {
                AppLog.e("recycler view is null");
                return;
            }
            mAllApps = result;
            mCurrentAppList.clear();
            mCurrentAppList.addAll(mAllApps);
            mAdapter.updateContents(mCurrentAppList);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onPreExecute() {
            AppLog.d("on Pre Execute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            AppLog.d("on Progress Update");
        }
    }

    private class DownloadApk extends AsyncTask<String, Integer, String> {
        int mType;

        public DownloadApk(int type) {
            mType = type;
        }

        protected String doInBackground(String... packageName) {
            try {
                ApplicationInfo applicationInfo = getContext().getPackageManager().getApplicationInfo(packageName[0], 0);
                final AppInfo app = new AppInfo(getContext(), applicationInfo);
                //Check Storage permission
                if (Utility.hasWriteExternalPermission(MyApplication.getContext()) == false) {
                    AppLog.d("There is no permission");
                    return null;
                }
                if (app.isCopyFile(getContext())) {
                    // create a handler to post messages to the main thread
                    AppLog.d("copied");
                    return app.getOutputDirectoryApk();
                } else {
                    AppLog.d("not copied");
                }
            } catch (PackageManager.NameNotFoundException e) {
                AppLog.e(e.toString());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {

            if (dialogExtrackApk != null && dialogExtrackApk.isShowing())
                dialogExtrackApk.dismiss();
            if (TextUtils.isEmpty(result))
                return;
            Toast.makeText(MyApplication.getContext(), getString(R.string.extract_to, result), Toast.LENGTH_LONG).show();
            if (mType == TYPE_SHARE) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                File fileWithinMyDir = new File(result);
                if (fileWithinMyDir.exists()) {
                    intentShareFile.setType("application/apk");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + result));
                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
                    startActivity(Intent.createChooser(intentShareFile, "Share APK"));
                }
            }
        }
    }

    private final AppInfoAdapter.OnItemListener mItemListener = new AppInfoAdapter.OnItemListener() {
        @Override
        public void onClick(View v, int pos, int type) {
            AppLog.d("onClick:" + pos);
            if (pos >= mCurrentAppList.size())
                return;
            AppInfo app = mCurrentAppList.get(pos);
            if (type == AppInfoAdapter.TYPE_CLICK_ITEM_VIEW) {
                if (MainActivity.mIsShowSelection) {
                    app.setIsChecked(!app.getIsChecked());
                    mAdapter.updateContents(mCurrentAppList);
                    mSelectionListener.onSelection(mCurrentAppList.size(), getPackages());
                } else {
                    extractAPK(app.getPackageName());
                }
            } else if (type == AppInfoAdapter.TYPE_CLICK_MORE_BUTTON) {
                mBottomSheetDialog = new MoreBottomSheet(getContext(), mMoreListener);
                mBottomSheetDialog.showPopup(v, app);
            } else if (type == AppInfoAdapter.TYPE_CHECK_BOX_ITEM) {
                app.setIsChecked(!app.getIsChecked());
                mAdapter.updateContents(mCurrentAppList);
                mSelectionListener.onSelection(mCurrentAppList.size(), getPackages());
            }
        }

        @Override
        public void onLongClick(AppInfo app) {
            if (!MainActivity.mIsShowSelection) {
                AppLog.d("onLongClick:" + app.getName());
                app.setIsChecked(true);
                mSelectionListener.onSelection(mCurrentAppList.size(), getPackages());
                mAdapter.updateContents(mCurrentAppList);
            }
        }

        @Override
        public void onCheckBoxChecked(AppInfo app, boolean ischeck) {
            AppLog.d("onCheckBoxChecked:" + app.getName());
            app.setIsChecked(ischeck);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLog.e("on Destroy View");
        if (mHandler != null)
            mHandler.cancel(true);
    }

    private void extractAPK(String packageName) {
        dialogExtrackApk = new ProgressDialog(getContext());
        dialogExtrackApk.setMessage("Extracting ...");
        dialogExtrackApk.setCancelable(false);
        dialogExtrackApk.show();
        new DownloadApk(TYPE_EXTRACT).execute(packageName);
    }

    private void shareAPK(AppInfo app) {
        dialogExtrackApk = new ProgressDialog(getContext());
        dialogExtrackApk.setMessage("Extracting ...");
        dialogExtrackApk.setCancelable(false);
        dialogExtrackApk.show();
        new DownloadApk(TYPE_SHARE).execute(app.getPackageName());

    }

    private void runApp(AppInfo app) {
        Intent launchIntent = MyApplication.getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    private void showAppInfo(AppInfo app) {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + app.getPackageName()));
        startActivity(i);
    }

    private void uninstallApp(AppInfo app) {
        Uri packageUri = Uri.parse("package:" + app.getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivity(uninstallIntent);
    }

    private List<String> getPackages() {
        List<String> pagkages = new ArrayList<>();
        for (AppInfo appInfo : mCurrentAppList) {
            if (appInfo.getIsChecked())
                pagkages.add(appInfo.getPackageName());
        }
        return pagkages;
    }

    public void sendData(boolean isCheckAll) {
        for (AppInfo appInfo : mCurrentAppList) {
            appInfo.setIsChecked(isCheckAll);
        }
        mAdapter.updateContents(mCurrentAppList);
        if (MainActivity.mIsShowSelection)
            mSelectionListener.onSelection(mCurrentAppList.size(), getPackages());
    }
}
