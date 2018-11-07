package com.mtvstudio.apkextractor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtvstudio.apkextractor.AppLog;
import com.mtvstudio.apkextractor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<File> mFiles;
    private FolderClickListener mClickListener;

    public interface FolderClickListener{
        void onClick(File file);
    }

    public PathAdapter(Context context, List<File> files){
        mFiles = new ArrayList<>();
        if(files != null)
            mFiles.addAll(files);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_directory, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        final File file = mFiles.get(position);
        if(file == null)
            return;


        viewHolder.name.setText(file.getName());
        if(file.isDirectory() == false){
            viewHolder.img_icon.setImageResource(R.drawable.icon_file);
            viewHolder.numberItems.setVisibility(View.INVISIBLE);
//            viewHolder.numberItems.setText(String.valueOf(file.length()));
        }else{
            viewHolder.img_icon.setImageResource(R.drawable.icon_folder);
            viewHolder.numberItems.setVisibility(View.VISIBLE);
            viewHolder.numberItems.setText(file.listFiles() == null ? String.valueOf(0) : String.format(mContext.getString(R.string.number_items), String.valueOf(file.listFiles().length)));
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.isDirectory()){
                    if(mClickListener != null)
                        mClickListener.onClick(file);
                    else
                        AppLog.e("listener is null");
                }else {
                    Toast.makeText(mContext, "This is not folder", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mFiles != null)
            return mFiles.size();
        return 0;
    }

    public void setListener(FolderClickListener listener){
        mClickListener = listener;
    }

    public void updateContents(List<File> files) {
        mFiles.clear();
        if (files != null)
            mFiles.addAll(files);

        AppLog.d("size:"+mFiles.size());

        AppLog.d("update Contents ");
        notifyDataSetChanged();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_icon;
        TextView name, numberItems;
        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            img_icon = (ImageView) itemView.findViewById(R.id.icon_folder);
            name = (TextView) itemView.findViewById(R.id.name_folder);
            numberItems = (TextView)itemView.findViewById(R.id.number_items);
        }
    }
}
