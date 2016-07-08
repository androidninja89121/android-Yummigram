package com.zhs1.Yummigram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.other.ProfileActivity;

/**
 * Created by I54460 on 7/4/2015.
 */
public class ProfileGridAdapter extends BaseAdapter {
    private Context mContext;

    public ProfileGridAdapter(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {

        return DataStore.getInstance().wallImagesForMyOwn.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridCell;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final int nPos = position;

        final WallImage wallImage = DataStore.getInstance().wallImagesForMyOwn.get(position);

        gridCell = new View(mContext);
        gridCell = inflater.inflate(R.layout.grid_my_cell, null);

        final ImageView imageView = (ImageView)gridCell.findViewById(R.id.gridMyImageView);
        Button btnDrop = (Button)gridCell.findViewById(R.id.btnDropMyGrid);

        if(wallImage.bmpWall == null){
            ImageLoader.getInstance().loadImage(wallImage.strImgUrl, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                    wallImage.bmpWall = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imageView.setImageBitmap(wallImage.bmpWall);
        }

        btnDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mContext).nPosForProfile = nPos;

                boolean isNewRecipe = false;

                if(wallImage.strRecipe.length() == 0) isNewRecipe = true;

                ((MainActivity)mContext).onActionSheet(isNewRecipe);
            }
        });

        return gridCell;
    }
}