package com.zhs1.Yummigram.other;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseObject;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.NotifyPost;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.utils.CustomActivity;

import it.sephiroth.android.library.exif2.ExifInterface;

/**
 * Created by I54460 on 6/30/2015.
 */
public class NotifyActivity extends CustomActivity implements View.OnClickListener {
    Button btnBack;
    LinearLayout lyContent;
    ScrollView scrollView;
    String[] arrNotifyCategory = {"started following ", "liked ", "commented to ", "added to favorites ", "requested the recipe to "};
    String[] arrNotifyTerminal = {"you", "your post", "your post", "your post", "your post"};
    ImageView imgvSpinner;
    boolean isGettingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_notify);

        btnBack = (Button)findViewById(R.id.btnBackNotify);
        lyContent = (LinearLayout)findViewById(R.id.lyContentNotify);

        btnBack.setOnClickListener(this);

        imgvSpinner = (ImageView)findViewById(R.id.imgvSpinnerNotify);

        AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
        spinner.start();

        imgvSpinner.setVisibility(View.GONE);

        scrollView = (ScrollView)findViewById(R.id.scrollViewNotify);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();

                //DO SOMETHING WITH THE SCROLL COORDINATES

                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

                if(view.getBottom() < scrollView.getHeight()) return;
                // Calculate the scrolldiff
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollY));

                // if diff is zero, then the bottom has been reached
                if (diff < -10 && !isGettingMore) {
                    // notify that we have reached the bottom
                    isGettingMore = true;
                    imgvSpinner.setVisibility(View.VISIBLE);

                    loadNextData();
                }
            }
        });

        if(DataStore.getInstance().notifyPosts.size() == 0)
            Commons.getNotifyPosts(NotifyActivity.this, Constants.LIMIT_NUMBER_GRID);

        presentData();
    }

    public void presentData(){
        lyContent.removeAllViews();

        for(int i = 0; i < DataStore.getInstance().notifyPosts.size(); i ++){

            final NotifyPost notifyPost = DataStore.getInstance().notifyPosts.get(i);
            final RelativeLayout newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_notify, null));

            CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhotoNotify);
            Button btnFullName = (Button)newCell.findViewById(R.id.btnFullNameNotify);
            TextView tvDescriptionFirst = (TextView)newCell.findViewById(R.id.tvDescFirstNotify);
            TextView tvDescriptionSecond = (TextView)newCell.findViewById(R.id.tvDescSecondNotify);
            final ImageView imgvWall = (ImageView)newCell.findViewById(R.id.imgvWallNotify);
            TextView tvTime = (TextView)newCell.findViewById(R.id.tvTimeNotify);

            if(notifyPost.nType == Commons.NotifyType.notifyFollowing){
                imgvWall.setVisibility(View.INVISIBLE);
            }else{
                final WallImage wallImage = Commons.getImageDataWith(notifyPost.strImageObjId);

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
                            imgvWall.setImageBitmap(bitmap);
                            wallImage.bmpWall = bitmap;
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });
                }else{
                    imgvWall.setImageBitmap(wallImage.bmpWall);
                }
            }

            final UserInfo userInfo = Commons.getUserInfoFrom(notifyPost.strOtherUserObjId);

            if(userInfo.bmpPhoto == null){
                imgvUserPhoto.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            btnFullName.setText(userInfo.strUserFirstName + " " + userInfo.strUserLastName);

            String strNotifyCat = arrNotifyCategory[notifyPost.nType.ordinal()];
            String strTerminal = arrNotifyTerminal[notifyPost.nType.ordinal()];

            tvDescriptionFirst.setText(strNotifyCat);
            tvDescriptionSecond.setText(strTerminal);

            tvTime.setText(Commons.getTime(notifyPost.createdDate));

            if(notifyPost.viewed){
                newCell.setBackgroundResource(R.drawable.conversations_cell);
                tvTime.setTextColor(Color.rgb(143, 142, 140));
            }else{
                newCell.setBackgroundResource(R.drawable.conversations_cell_unread);
                tvTime.setTextColor(Color.rgb(194, 169, 132));
            }

            btnFullName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(NotifyActivity.this, userInfo.strUserObjID);
                }
            });

            imgvUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(NotifyActivity.this, userInfo.strUserObjID);
                }
            });

            newCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(notifyPost.nType == Commons.NotifyType.notifyFollowing){
                        Commons.onOtherProfile(NotifyActivity.this, notifyPost.strOtherUserObjId);
                    }else{
                        WallImage wallImage = Commons.getImageDataWith(notifyPost.strImageObjId);

                        Commons.onCommentView(NotifyActivity.this, wallImage);
                    }

                    if(!notifyPost.viewed){
                        notifyPost.viewed = true;

                        ParseObject pObjNotify = DataStore.getInstance().userNotifyPostPFObjectMap.get(notifyPost.strObjId);

                        pObjNotify.put(Constants.pKeyViewed, true);

                        pObjNotify.saveInBackground();

                        presentData();
                    }
                }
            });

            lyContent.addView(newCell);
        }
    }

    public void didGetNotifyPosts(){
        isGettingMore = false;
        imgvSpinner.setVisibility(View.GONE);

        presentData();
    }

    public void refreshData(){
        DataStore.getInstance().notifyPosts.clear();

        Commons.getNotifyPosts(NotifyActivity.this, 15);
    }

    public void loadNextData(){
        Commons.getNotifyPosts(NotifyActivity.this, Constants.LIMIT_NUMBER_GRID);
    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Global.notifyActivity = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.notifyActivity = NotifyActivity.this;
    }
}
