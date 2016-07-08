package com.zhs1.Yummigram.other;

import android.content.Intent;
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
import com.zhs1.Yummigram.model.TotalMsg;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.utils.CustomActivity;

import java.util.Calendar;

/**
 * Created by I54460 on 6/30/2015.
 */
public class TotalChatActivity extends CustomActivity {
    Button btnBack;
    LinearLayout lyContent;
    ScrollView scrollView;
    ImageView imgvSpinner;
    boolean isGettingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_total_chat);

        btnBack = (Button)findViewById(R.id.btnBackTotalChat);
        lyContent = (LinearLayout)findViewById(R.id.lyContentTotalChat);

        btnBack.setOnClickListener(this);

        imgvSpinner = (ImageView)findViewById(R.id.imgvSpinnerTotalChat);

        AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
        spinner.start();

        imgvSpinner.setVisibility(View.GONE);

        scrollView = (ScrollView)findViewById(R.id.scrollViewTotalChat);

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

        Global.lastTotalMsgUpdate = Calendar.getInstance().getTime();

        if(DataStore.getInstance().totalMsg.size() == 0) Commons.getTotalMsg(TotalChatActivity.this, Constants.LIMIT_NUMBER_GRID);

        presentData();
    }

    public void presentData(){
        lyContent.removeAllViews();

        for(int i = 0; i < DataStore.getInstance().totalMsg.size(); i ++){
            final TotalMsg totalMsg = DataStore.getInstance().totalMsg.get(i);
            final RelativeLayout newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_total_chat, null));

            CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhotoTotalChat);
            Button btnFullName = (Button)newCell.findViewById(R.id.btnFullNameTotalChat);
            TextView tvYou = (TextView)newCell.findViewById(R.id.tvYouTotalChat);
            TextView tvConversation = (TextView)newCell.findViewById(R.id.tvConversationTotalChat);
            TextView tvTime = (TextView)newCell.findViewById(R.id.tvTimeTotalChat);

            final UserInfo userInfo = Commons.getUserInfoFrom(totalMsg.strOtherUserObjId);

            imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);

            if(userInfo.bmpPhoto == null){
                imgvUserPhoto.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            btnFullName.setText(userInfo.strUserFirstName + " " + userInfo.strUserLastName);
            tvTime.setText(Commons.getTime(totalMsg.updatedDate));
            tvConversation.setText(totalMsg.strMessage);

            if(totalMsg.isFromMe){
                tvYou.setText("You: ");
            }else{
                tvYou.setText("");

                if(totalMsg.viewed){
                    newCell.setBackgroundResource(R.drawable.conversations_cell);
                    tvTime.setTextColor(Color.rgb(143, 142, 140));
                }else{
                    newCell.setBackgroundResource(R.drawable.conversations_cell_unread);
                    tvTime.setTextColor(Color.rgb(194, 169, 132));
                }
            }


            btnFullName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(TotalChatActivity.this, userInfo.strUserObjID);
                }
            });

            imgvUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(TotalChatActivity.this, userInfo.strUserObjID);
                }
            });

            newCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(TotalChatActivity.this, DetailChatActivity.class);

                    myIntent.putExtra(Constants.eKeyUserObjId, totalMsg.strOtherUserObjId);

                    startActivity(myIntent);

                    if(!totalMsg.viewed){
                        totalMsg.viewed = true;

                        ParseObject pObjTotalMsg = DataStore.getInstance().totalMsgPFObjectMap.get(totalMsg.strObjId);

                        pObjTotalMsg.put(Constants.pKeyViewed, true);

                        pObjTotalMsg.saveInBackground();

                        presentData();
                    }
                }
            });

            lyContent.addView(newCell);
        }
    }

    public void refreshData(){
        DataStore.getInstance().totalMsg.clear();
        Global.lastTotalMsgUpdate = Calendar.getInstance().getTime();
        Commons.getTotalMsg(TotalChatActivity.this, 10);
    }

    public void didGetTotalMsg(){
        isGettingMore = false;
        imgvSpinner.setVisibility(View.GONE);

        presentData();
    }

    public void loadNextData(){
        Commons.getTotalMsg(TotalChatActivity.this, Constants.LIMIT_NUMBER_GRID);
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
        Global.totalChatActivity = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.totalChatActivity = TotalChatActivity.this;
    }
}
