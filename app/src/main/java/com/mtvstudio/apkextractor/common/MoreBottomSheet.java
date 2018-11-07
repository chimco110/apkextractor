package com.mtvstudio.apkextractor.common;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.BottomSheetDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.data.AppInfo;
import com.mtvstudio.apkextractor.R;

public class MoreBottomSheet implements View.OnClickListener{
    private BottomSheetDialog mBottomSheetDialog;
    private static MoreBottomSheet mInstance;
    PopupWindow popupWindow;
    private ClickListener mListener;
    private AppInfo mApp;
    private Context mContext;

    public static final int EXTRACT_APK_TYPE = 1;
    public static final int SHARE_APK_TYPE = 2;
    public static final int RUN_APP_TYPE = 3;
    public static final int APP_INFO_TYPE = 4;
    public static final int UNINSTALL_APP_TYPE = 5;

    @Override
    public void onClick(View view) {
        if (mListener == null)
            return;
        switch (view.getId()){
            case R.id.extract_apk:
                mListener.onClick(mApp, EXTRACT_APK_TYPE);
                break;
            case R.id.share_apk:
                mListener.onClick(mApp, SHARE_APK_TYPE);
                break;
            case R.id.run_app:
                mListener.onClick(mApp, RUN_APP_TYPE);
                break;
            case R.id.app_info:
                mListener.onClick(mApp, APP_INFO_TYPE);
                break;
            case R.id.uninstall_app:
                mListener.onClick(mApp, UNINSTALL_APP_TYPE);
                break;
            default:
                break;
        }

    }

    public interface ClickListener{
        void onClick(AppInfo app, int type);
    }


    public static MoreBottomSheet getInstance(Context context, ClickListener listener){
        if (mInstance == null){
            mInstance = new MoreBottomSheet(context, listener);
        }
        return mInstance;
    }

    public MoreBottomSheet(Context context, ClickListener listener){
        mContext = context;
        mListener = listener;
    }

    public void setListener(ClickListener listener){
        mListener = listener;
    }

    public void showBottom(AppInfo app){
        mApp = app;
        if (mBottomSheetDialog != null &&  mBottomSheetDialog.isShowing()){
            mBottomSheetDialog.dismiss();
            return;
        }
        mBottomSheetDialog = new BottomSheetDialog(mContext);
        mBottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.bottom_sheet_more, null);
        mBottomSheetDialog.setContentView(view);
        LinearLayout extract = (LinearLayout) mBottomSheetDialog.findViewById(R.id.extract_apk);
        extract.setOnClickListener(this);
        LinearLayout share = (LinearLayout) mBottomSheetDialog.findViewById(R.id.share_apk);
        share.setOnClickListener(this);
        LinearLayout run = (LinearLayout) mBottomSheetDialog.findViewById(R.id.run_app);
        run.setOnClickListener(this);
        LinearLayout info = (LinearLayout) mBottomSheetDialog.findViewById(R.id.app_info);
        info.setOnClickListener(this);
        LinearLayout uninstall = (LinearLayout) mBottomSheetDialog.findViewById(R.id.uninstall_app);
        uninstall.setOnClickListener(this);

        mBottomSheetDialog.show();
    }

    public void showPopup(View anchorView, AppInfo app) {
        mApp = app;
        if (popupWindow != null &&  popupWindow.isShowing()){
            popupWindow.dismiss();
            return;
        }
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.bottom_sheet_more, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout extract = (LinearLayout) popupView.findViewById(R.id.extract_apk);
        extract.setOnClickListener(this);
        LinearLayout share = (LinearLayout) popupView.findViewById(R.id.share_apk);
        share.setOnClickListener(this);
        LinearLayout run = (LinearLayout) popupView.findViewById(R.id.run_app);
        run.setOnClickListener(this);
        LinearLayout info = (LinearLayout) popupView.findViewById(R.id.app_info);
        info.setOnClickListener(this);
        LinearLayout uninstall = (LinearLayout) popupView.findViewById(R.id.uninstall_app);
        uninstall.setOnClickListener(this);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        AppLog.d("x" + popupView.getWidth());
        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.LEFT | Gravity.TOP, location[0], location[1]);

    }

    public void dismiss(){
        if (mBottomSheetDialog == null ||  !mBottomSheetDialog.isShowing()){
            AppLog.e("bottom sheet is null or not showing");
            return;
        }
        mBottomSheetDialog.dismiss();
    }

    public void dismissPopupWindow(){
        if (popupWindow == null ||  !popupWindow.isShowing()){
            AppLog.e("bottom sheet is null or not showing");
            return;
        }
        popupWindow.dismiss();
    }

}
