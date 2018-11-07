package com.mtvstudio.apkextractor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.MainActivity;
import com.mtvstudio.apkextractor.data.AppInfo;
import com.mtvstudio.apkextractor.R;

import java.util.ArrayList;
import java.util.List;

public class AppInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private ArrayList<AppInfo> mApps;

    private OnItemListener mListener;

    private boolean isCheck;

    public final static int TYPE_CLICK_MORE_BUTTON = 1;
    public final static int TYPE_CLICK_ITEM_VIEW = 2;
    public final static int TYPE_CHECK_BOX_ITEM = 3;

    public interface OnItemListener {
        void onClick(View view, int position, int type);

        void onLongClick(AppInfo app);

        void onCheckBoxChecked(AppInfo app, boolean ischeck);
    }

    public AppInfoAdapter(Context context, List<AppInfo> apps) {
        mApps = new ArrayList<>();
        if (apps != null)
            mApps.addAll(apps);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final AppInfo app = mApps.get(position);
        if (app == null)
            return;

        viewHolder.packageName.setText(app.getPackageName());
        viewHolder.name.setText(app.getName());
        if (MainActivity.mIsShowSelection) {
            viewHolder.btn.setVisibility(View.GONE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnClickListener(this);

            viewHolder.checkBox.setChecked(app.getIsChecked());
            viewHolder.checkBox.setTag(position);
        } else {
            viewHolder.btn.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.btn.setOnClickListener(this);
            viewHolder.btn.setTag(position);
        }

        if (app.getIcon() != null) {
            try {
                viewHolder.img_icon.setImageDrawable(app.getIcon());
            } catch (OutOfMemoryError e) {
                AppLog.e("OutOfMemoryError:" + e.toString());
            }
        }
        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onLongClick(app);
                return false;
            }
        });
        viewHolder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        if (mApps != null)
            return mApps.size();
        return 0;
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        AppLog.d("pos:" + pos);
        switch (v.getId()) {
            case R.id.item_layout:
                mListener.onClick(v, pos, TYPE_CLICK_ITEM_VIEW);
                break;
            case R.id.item_checkbox:
                mListener.onClick(v, pos, TYPE_CHECK_BOX_ITEM);
                break;
            case R.id.start_btn:
                mListener.onClick(v, pos, TYPE_CLICK_MORE_BUTTON);
                break;
            default:
                break;
        }
    }

    public void updateContents(List<AppInfo> apps) {
        mApps.clear();
        if (apps != null)
            mApps.addAll(apps);
        AppLog.d("size:" + mApps.size());
        notifyDataSetChanged();
    }

    public void setListener(OnItemListener listener) {
        mListener = listener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_icon;
        ImageView btn;
        TextView name, packageName;
        View itemView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            img_icon = (ImageView) itemView.findViewById(R.id.icon_app);
            btn = (ImageView) itemView.findViewById(R.id.start_btn);
            name = (TextView) itemView.findViewById(R.id.name_app);
            packageName = (TextView) itemView.findViewById(R.id.package_app);
            checkBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
        }
    }
}
