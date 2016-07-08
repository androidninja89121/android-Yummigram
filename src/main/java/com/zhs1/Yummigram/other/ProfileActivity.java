package com.zhs1.Yummigram.other;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.adapter.OtherProfileGridAdapter;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.utils.CustomActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by I54460 on 6/30/2015.
 */
public class ProfileActivity extends CustomActivity implements OnClickListener{
    UserInfo userInfo;
    Button btnBack, btnUserFullName , btnSendMsg, btnFollowing;
    GridViewWithHeaderAndFooter gridView;
    CircularImageView imgvUserPhoto;
    boolean isGridGettingMore = false, isListGettingMore = false, isGridView = true;
    public ArrayList<WallImage> arrWallImage;
    ImageView imgvGridViewForGrid, imgvListViewForGrid, imgvGridViewForList, imgvListViewForList, imgvSpinner;
    OtherProfileGridAdapter adapterGrid;
    ScrollView scrollView;
    LinearLayout lyContent;
    boolean isFollowing;

    ImageView imgvPhotos, imgvFollowers, imgvFollowings;
    TextView tvPhotos, tvFollowers, tvFollowings;
    RelativeLayout rlyPhotos, rlyFollowers, rlyFollowings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profile);

        arrWallImage = new ArrayList<WallImage>();

        Intent myIntent = getIntent(); // gets the previously created intent
        String strUserObjId = myIntent.getStringExtra(Constants.eKeyUserObjId);
        userInfo = DataStore.getInstance().userInfoMap.get(strUserObjId);

        imgvUserPhoto = (CircularImageView)findViewById(R.id.imgbUserPhotoOtherProfile);

        if(userInfo.bmpPhoto == null){
            imgvUserPhoto.setImageResource(R.drawable.btn_profile);
        }else{
            imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
        }

        btnUserFullName = (Button)findViewById(R.id.btnFullNameOtherProfile);

        btnUserFullName.setText(userInfo.strUserFirstName + " " + userInfo.strUserLastName );

        btnSendMsg = (Button)findViewById(R.id.btnSendMessage);
        btnFollowing = (Button)findViewById(R.id.btnFollowing);

        btnSendMsg.setOnClickListener(this);
        btnFollowing.setOnClickListener(this);

        if(Global.myInfo.arrFollowing.contains(strUserObjId))
            isFollowing = true;
        else
            isFollowing = false;

        if(isFollowing){
            btnFollowing.setBackgroundResource(R.drawable.follow_button_active);
            btnFollowing.setText("Following");
        }else{
            btnFollowing.setBackgroundResource(R.drawable.follow_button);
            btnFollowing.setText("Follow");
        }

        gridView = (GridViewWithHeaderAndFooter)findViewById(R.id.gridViewOtherProfile);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

        imgvGridViewForGrid = (ImageView)headerView.findViewById(R.id.imgvGridView);
        imgvListViewForGrid = (ImageView)headerView.findViewById(R.id.imgvListView);

        imgvListViewForGrid.setOnClickListener(this);

        gridView.addHeaderView(headerView);

        adapterGrid = new OtherProfileGridAdapter(ProfileActivity.this);

        gridView.setAdapter(adapterGrid);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                WallImage wallImage = arrWallImage.get(position);

                if (wallImage.strRecipe.length() > 0)
                    Commons.onRecipeView(ProfileActivity.this, wallImage);
                else
                    Commons.onCommentView(ProfileActivity.this, wallImage);

            }
        });

        gridView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = gridView.getScrollY();

                //DO SOMETHING WITH THE SCROLL COORDINATES

                View view = (View) gridView.getChildAt(gridView.getChildCount() - 1);

                // Calculate the scrolldiff
                int diff = (view.getBottom() - (gridView.getHeight() + scrollY));

                // if diff is zero, then the bottom has been reached
                if (diff < -10 && !isGridGettingMore) {
                    if (arrWallImage.size() == userInfo.arrWallImages.size()) return;

                    // notify that we have reached the bottom
                    isGridGettingMore = true;
                    imgvSpinner.setVisibility(View.VISIBLE);

                    loadNextData();
                }
            }
        });

        btnBack = (Button)findViewById(R.id.btnBackProfile);
        btnBack.setOnClickListener(this);

        imgvSpinner = (ImageView)findViewById(R.id.imgvSpinnerOtherProfile);

        AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
        spinner.start();

        imgvSpinner.setVisibility(View.GONE);

        lyContent = (LinearLayout)findViewById(R.id.lyContentOtherProfile);
        scrollView = (ScrollView)findViewById(R.id.scrollViewOtherProfile);

        scrollView.setVisibility(View.INVISIBLE);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();

                //DO SOMETHING WITH THE SCROLL COORDINATES

                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

                // Calculate the scrolldiff
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollY));

                // if diff is zero, then the bottom has been reached
                if (diff < -10 && !isListGettingMore) {
                    if (arrWallImage.size() == userInfo.arrWallImages.size()) return;

                    // notify that we have reached the bottom
                    isListGettingMore = true;
                    imgvSpinner.setVisibility(View.VISIBLE);

                    loadNextData();
                }
            }
        });

        rlyPhotos = (RelativeLayout)findViewById(R.id.rlyPhotosProfile);
        rlyFollowers = (RelativeLayout)findViewById(R.id.rlyFollowersProfile);
        rlyFollowings = (RelativeLayout)findViewById(R.id.rlyFollowingsProfile);

        rlyPhotos.setOnClickListener(this);
        rlyFollowers.setOnClickListener(this);
        rlyFollowings.setOnClickListener(this);

        imgvPhotos = (ImageView)findViewById(R.id.imgvPhotosProfile);
        imgvFollowers = (ImageView)findViewById(R.id.imgvFollowersProfile);
        imgvFollowings = (ImageView)findViewById(R.id.imgvFollowingsProfile);

        tvPhotos = (TextView)findViewById(R.id.tvPhotosProfile);
        tvFollowers = (TextView)findViewById(R.id.tvFollowersProfile);
        tvFollowings = (TextView)findViewById(R.id.tvFollowingsProfile);

        initSegmentedGroup();
        imgvPhotos.setImageResource(R.drawable.three_tabs_1_active);

        loadData();
    }

    public void initSegmentedGroup(){
        updateSegmentedGroup();

        imgvPhotos.setImageResource(R.drawable.three_tabs_1);
        imgvFollowers.setImageResource(R.drawable.three_tabs_2);
        imgvFollowings.setImageResource(R.drawable.three_tabs_3);
    }

    public void updateSegmentedGroup(){
        tvPhotos.setText(userInfo.arrWallImages.size() + " photos");
        tvFollowers.setText(userInfo.arrFollower.size() + " Followers");
        tvFollowings.setText(userInfo.arrFollowing.size() + " Followings");
    }

    public void loadData(){

        arrWallImage.clear();

        for(String strObjId : userInfo.arrWallImages){
            WallImage wallImage = DataStore.getInstance().wallImageMap.get(strObjId);

            if(wallImage == null) continue;

            arrWallImage.add(wallImage);
        }

        Collections.sort(arrWallImage, new Commons.CreatedDateComparator());

        adapterGrid.notifyDataSetChanged();
        gridView.invalidateViews();

        presentDataForPhoto();
    }

    public void presentDataForFollower(){
        lyContent.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        for(String strObj : userInfo.arrFollower){
            final UserInfo userInfo = Commons.getUserInfoFrom(strObj);

            View cell = layoutInflater.inflate(R.layout.cell_user, null);

            CircularImageView imgvUserPhoto = (CircularImageView)cell.findViewById(R.id.imgbUserPhotoForUserCell);
            TextView tvUserName = (TextView)cell.findViewById(R.id.tvUserNameForUserCell);
            TextView tvPosts = (TextView)cell.findViewById(R.id.tvPostsForUserCell);
            TextView tvFollowers = (TextView)cell.findViewById(R.id.tvFollowersForUserCell);
            TextView tvFollowings = (TextView)cell.findViewById(R.id.tvFollowingsForUserCell);

            if(userInfo.bmpPhoto == null){
                imgvUserPhoto.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            tvUserName.setText(userInfo.strUserFirstName + " " + userInfo.strUserLastName);
            tvPosts.setText(userInfo.arrWallImages.size() + " Posts");
            tvFollowers.setText(userInfo.arrFollower.size() + " Followers");
            tvFollowings.setText(userInfo.arrFollowing.size() + " Following");

            cell.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(ProfileActivity.this, userInfo.strUserObjID);
                }
            });

            lyContent.addView(cell);
        }
    }

    public void presentDataForFollowing(){
        lyContent.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        for(String strObj : userInfo.arrFollowing){
            final UserInfo userInfo = Commons.getUserInfoFrom(strObj);

            View cell = layoutInflater.inflate(R.layout.cell_user, null);

            CircularImageView imgvUserPhoto = (CircularImageView)cell.findViewById(R.id.imgbUserPhotoForUserCell);
            TextView tvUserName = (TextView)cell.findViewById(R.id.tvUserNameForUserCell);
            TextView tvPosts = (TextView)cell.findViewById(R.id.tvPostsForUserCell);
            TextView tvFollowers = (TextView)cell.findViewById(R.id.tvFollowersForUserCell);
            TextView tvFollowings = (TextView)cell.findViewById(R.id.tvFollowingsForUserCell);

            if(userInfo.bmpPhoto == null){
                imgvUserPhoto.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            tvUserName.setText(userInfo.strUserFirstName + " " + userInfo.strUserLastName);
            tvPosts.setText(userInfo.arrWallImages.size() + " Posts");
            tvFollowers.setText(userInfo.arrFollower.size() + " Followers");
            tvFollowings.setText(userInfo.arrFollowing.size() + " Following");

            cell.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(ProfileActivity.this, userInfo.strUserObjID);
                }
            });

            lyContent.addView(cell);
        }
    }

    public void presentDataForPhoto(){
        lyContent.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

        imgvGridViewForList = (ImageView)headerView.findViewById(R.id.imgvGridView);
        imgvListViewForList = (ImageView)headerView.findViewById(R.id.imgvListView);

        imgvGridViewForList.setOnClickListener(this);

        imgvGridViewForList.setImageResource(R.drawable.photos_table_view);
        imgvListViewForList.setImageResource(R.drawable.photos_list_view_active);

        lyContent.addView(headerView);

        for(int i = 0; i < arrWallImage.size(); i ++){
            if(i > 0){
                LinearLayout betweenLayout = new LinearLayout(ProfileActivity.this);
                betweenLayout.setLayoutParams(new LinearLayout.LayoutParams(100, 5));

                lyContent.addView(betweenLayout);
            }

            final WallImage wallImage = arrWallImage.get(i);
            final LinearLayout newCell = (LinearLayout)(View.inflate(ProfileActivity.this, R.layout.cell_wall_image, null));

            CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhoto);
            Button 			  btnFullName = (Button)newCell.findViewById(R.id.btnFullName);
            TextView tvTime = (TextView)newCell.findViewById(R.id.tvTime);
            Button            btnRecipe = (Button)newCell.findViewById(R.id.btnRecipe);
            TextView          tvSelfComment = (TextView)newCell.findViewById(R.id.tvSelfComment);
            final ImageView         imgvWall = (ImageView)newCell.findViewById(R.id.imgvWall);
            final Button            btnRecipeRequest = (Button)newCell.findViewById(R.id.btnRecipeRequest);
            Button            btnShareFacebook = (Button)newCell.findViewById(R.id.btnShareFacebook);
            RelativeLayout rlyLike = (RelativeLayout)newCell.findViewById(R.id.rlyLike);
            RelativeLayout    rlyComment = (RelativeLayout)newCell.findViewById(R.id.rlyComment);
            RelativeLayout    rlyFavorite = (RelativeLayout)newCell.findViewById(R.id.rlyFavorite);
            TextView          tvLike = (TextView)newCell.findViewById(R.id.tvLike);
            TextView          tvComment = (TextView)newCell.findViewById(R.id.tvComment);
            final ImageView   imgvLike = (ImageView)newCell.findViewById(R.id.imgvLike);
            ImageView         imgvComment = (ImageView)newCell.findViewById(R.id.imgvComment);
            final ImageView   imgvFavorite = (ImageView)newCell.findViewById(R.id.imgvFavorite);

            UserInfo userInfo = Commons.getUserInfoFrom(wallImage.strUserObjId);

            if(userInfo.bmpPhoto == null){
                imgvUserPhoto.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            btnFullName.setText(wallImage.strUserFullName);
            tvTime.setText(Commons.getTime(wallImage.createdDate) + " in " + wallImage.strCity + ", " + wallImage.strCountry);
            if(wallImage.strRecipe.length() > 0){
                btnRecipe.setVisibility(View.VISIBLE);
                btnRecipeRequest.setVisibility(View.INVISIBLE);
            }else {
                btnRecipe.setVisibility(View.INVISIBLE);

                if (wallImage.strUserObjId.equals(Global.myInfo.strUserObjID)) {
                    btnRecipeRequest.setVisibility(View.INVISIBLE);
                } else {
                    btnRecipeRequest.setVisibility(View.VISIBLE);

                    if (Global.pref.getBoolean(wallImage.strImageObjId, false)) {
                        btnRecipeRequest.setText(R.string.recipe_requested);
                        btnRecipeRequest.setBackgroundResource(R.drawable.borders_recipe_requested);
                        btnRecipeRequest.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        btnRecipeRequest.setText(R.string.recipe_request);
                        btnRecipeRequest.setBackgroundResource(R.drawable.borders_recipe_request);
                        btnRecipeRequest.setTextColor(getResources().getColor(R.color.red_app));
                    }
                }
            }

            String strSelfComment = wallImage.strSelfComments;

            if(strSelfComment.length() == 0){
                tvSelfComment.setLayoutParams(new LinearLayout.LayoutParams(100, 10));
            }else{
                SpannableString ss = new SpannableString(strSelfComment);

                String strLowerCaseSelfComment = wallImage.strSelfComments.toLowerCase();

                for(final String strTag : wallImage.arrTag){
                    int nPos = strLowerCaseSelfComment.indexOf(strTag);

                    if(nPos == -1) continue;

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            Commons.onTagSelected(ProfileActivity.this, strTag);
                        }
                    };

                    ss.setSpan(clickableSpan, nPos, nPos + strTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                tvSelfComment.setText(ss);
            }
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,width);

            imgvWall.setLayoutParams(parms);

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

            tvLike.setText(wallImage.nNumberLikes + "");
            tvComment.setText(wallImage.arrComments.size() + "");

            if(wallImage.liked)
                imgvLike.setBackgroundResource(R.drawable.post_icons_heart_fill);
            else
                imgvLike.setBackgroundResource(R.drawable.post_icons_heart);

            if(wallImage.commented)
                imgvComment.setBackgroundResource(R.drawable.post_icons_comment_fill);
            else
                imgvComment.setBackgroundResource(R.drawable.post_icons_comment);

            if(wallImage.favorited)
                imgvFavorite.setBackgroundResource(R.drawable.post_icons_star_fill);
            else
                imgvFavorite.setBackgroundResource(R.drawable.post_icons_star);

            btnFullName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(ProfileActivity.this, wallImage.strUserObjId);
                }
            });

            btnRecipe.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onRecipeView(ProfileActivity.this, wallImage);
                }
            });

            rlyLike.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseObject pObj = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

                    wallImage.liked = !wallImage.liked;

                    if(wallImage.liked){
                        imgvLike.setBackgroundResource(R.drawable.ic_liked);
                        pObj.addUnique(Constants.pKeyLikes, Global.myInfo.strUserObjID);
                        wallImage.nNumberLikes ++;
                        Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyLiked);

                    }else{
                        imgvLike.setBackgroundResource(R.drawable.ic_like);
                        pObj.removeAll(Constants.pKeyLikes, Arrays.asList(Global.myInfo.strUserObjID));
                        wallImage.nNumberLikes --;
                    }

                    pObj.saveInBackground();
                }
            });

            rlyComment.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onCommentView(ProfileActivity.this, wallImage);
                }
            });

            rlyFavorite.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    wallImage.favorited = !wallImage.favorited;

                    if(wallImage.favorited){
                        imgvFavorite.setBackgroundResource(R.drawable.ic_favorited);
                        Global.myInfo.arrFavorites.add(wallImage.strImageObjId);
                        currentUser.addUnique(Constants.pKeyFavorites, wallImage.strImageObjId);
                        Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyAddFavorite);
                    }else{
                        imgvFavorite.setBackgroundResource(R.drawable.ic_favorite);
                        Global.myInfo.arrFavorites.remove(wallImage.strImageObjId);
                        currentUser.removeAll(Constants.pKeyFavorites, Arrays.asList(wallImage.strImageObjId));
                    }

                    currentUser.saveInBackground();
                }
            });

            btnRecipeRequest.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Global.pref.getBoolean(wallImage.strImageObjId, false)) return;

                    SharedPreferences.Editor editor = Global.pref.edit();

                    editor.putBoolean(wallImage.strImageObjId, true);
                    editor.commit();

                    btnRecipeRequest.setText(R.string.recipe_requested);
                    btnRecipeRequest.setBackgroundResource(R.drawable.borders_recipe_requested);
                    btnRecipeRequest.setTextColor(getResources().getColor(R.color.white));

                    wallImage.nNumberRecipeRequests ++;

                    ParseObject pObjWallImage = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

                    pObjWallImage.put(Constants.pKeyRequestRecipe, wallImage.nNumberRecipeRequests);

                    pObjWallImage.saveInBackground();

                    Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyRequestRecipe);
                }
            });

            btnShareFacebook.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onShareFacebook(ProfileActivity.this, wallImage);
                }
            });

            imgvUserPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(ProfileActivity.this, wallImage.strUserObjId);
                }
            });

            imgvWall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(wallImage.strRecipe.length() > 0)
                        Commons.onRecipeView(ProfileActivity.this, wallImage);
                    else
                        Commons.onCommentView(ProfileActivity.this, wallImage);
                }
            });

            lyContent.addView(newCell);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }

        if(v == imgvListViewForGrid){
            isGridView = false;

            scrollView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);

            presentDataForPhoto();
        }

        if(v == imgvGridViewForList){
            isGridView = true;

            scrollView.setVisibility(View.INVISIBLE);
            gridView.setVisibility(View.VISIBLE);
        }

        if(v == btnSendMsg){
            Intent myIntent = new Intent(ProfileActivity.this, DetailChatActivity.class);

            myIntent.putExtra(Constants.eKeyUserObjId, userInfo.strUserObjID);

            startActivity(myIntent);
        }

        if(v == btnFollowing)
        {
            isFollowing = !isFollowing;

            if(isFollowing){
                btnFollowing.setBackgroundResource(R.drawable.follow_button_active);
                btnFollowing.setText("Following");

                Global.myInfo.arrFollowing.add(userInfo.strUserObjID);
                userInfo.arrFollower.add(Global.myInfo.strUserObjID);

                ParseUser pUserOther = DataStore.getInstance().userInfoPFObjectMap.get(userInfo.strUserObjID);

                boolean isNotifyFollow = pUserOther.getBoolean(Constants.pKeyNotifyFollow);

                if(!isNotifyFollow) return;

                ParseObject pObjNotify = ParseObject.create(Constants.pClassNotifyPost);

                pObjNotify.put(Constants.pKeyUserObjId, userInfo.strUserObjID);
                pObjNotify.put(Constants.pKeyNotifyType, Commons.NotifyType.notifyFollowing.ordinal());
                pObjNotify.put(Constants.pKeyOtherUserObjId, Global.myInfo.strUserObjID);
                pObjNotify.put(Constants.pKeyImageObjId, "");

                pObjNotify.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            try {
                                ParseQuery queryInstallation = ParseInstallation.getQuery();

                                queryInstallation.whereEqualTo(Constants.pKeyUserObjId, userInfo.strUserObjID);

                                String strFullName = Global.myInfo.strUserFirstName + " " + Global.myInfo.strUserLastName;
                                String strAlert = strFullName + " started following you.";

                                JSONObject data = new JSONObject();

                                data.put(Constants.pnMode, Constants.PN_NOTIFY);
                                data.put(Constants.pnBadge, Constants.PN_INCREMENT);
                                data.put(Constants.pnAlert, strAlert);
                                data.put(Constants.pnNotifyType, Commons.NotifyType.notifyFollowing.ordinal());
                                data.put(Constants.pnImageId, "");
                                data.put(Constants.pnUserObjId, Global.myInfo.strUserObjID);
                                data.put(Constants.pnSound, "Notify_1.wav");

                                ParsePush push = new ParsePush();

                                push.setQuery(queryInstallation);
                                push.setData(data);

                                push.sendInBackground();

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

            }else{
                btnFollowing.setBackgroundResource(R.drawable.follow_button);
                btnFollowing.setText("Follow");

                Global.myInfo.arrFollowing.remove(userInfo.strUserObjID);
                userInfo.arrFollower.remove(Global.myInfo.strUserObjID);
            }

            ParseUser currentUser = ParseUser.getCurrentUser();

            currentUser.put(Constants.pKeyFollowing, Global.myInfo.arrFollowing);

            currentUser.saveInBackground();

            ParseUser pUserOther = DataStore.getInstance().userInfoPFObjectMap.get(userInfo.strUserObjID);

            pUserOther.put(Constants.pKeyFollower, userInfo.arrFollower);

            pUserOther.saveInBackground();

            updateSegmentedGroup();
        }

        if (v == rlyPhotos) {
            initSegmentedGroup();
            imgvPhotos.setImageResource(R.drawable.three_tabs_1_active);

            scrollView.setVisibility(View.INVISIBLE);
            gridView.setVisibility(View.VISIBLE);
            isGridView = true;
        } else if (v == rlyFollowers) {
            initSegmentedGroup();
            imgvFollowers.setImageResource(R.drawable.three_tabs_2_active);

            scrollView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);
            presentDataForFollower();

        } else if (v == rlyFollowings) {
            initSegmentedGroup();
            imgvFollowings.setImageResource(R.drawable.three_tabs_3_active);

            scrollView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);
            presentDataForFollowing();
        }
    }

    public void loadNextData(){
        Global.otherInfo = userInfo;

        Commons.getWallImagesForMyOwn(ProfileActivity.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForMyOwn(){
        if(isGridView){
            isGridGettingMore = false;
        }else{
            isListGettingMore = false;
        }

        imgvSpinner.setVisibility(View.GONE);

        loadData();
    }
}
