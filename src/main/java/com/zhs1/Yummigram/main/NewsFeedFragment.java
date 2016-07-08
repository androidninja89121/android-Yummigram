package com.zhs1.Yummigram.main;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;

import java.util.ArrayList;
import java.util.Arrays;

import info.hoang8f.android.segmented.SegmentedGroup;

import static com.zhs1.Yummigram.R.*;

public class NewsFeedFragment extends Fragment implements OnClickListener {
	LinearLayout lyContent;
	ScrollView scrollView;
	ArrayList<WallImage> arrEveryone, arrFollowing, arrData;
	Commons.ViewMode viewMode = Commons.ViewMode.viewEveryone;
	ImageView imgvSpinner;

	RelativeLayout rlyEveryone, rlyFollowing;
	ImageView imgvEveryone, imgvFollowing;

	boolean isLoadingMore = false, isGettingMore = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
	{
		View v = inflater.inflate(layout.fragment_newsfeed, null);

		rlyEveryone = (RelativeLayout)v.findViewById(id.rlyEveryOneNewsfeed);
		rlyFollowing = (RelativeLayout)v.findViewById(id.rlyFollowingNewsfeed);

		imgvEveryone = (ImageView)v.findViewById(id.imgvEveryOneNewsfeed);
		imgvFollowing = (ImageView)v.findViewById(id.imgvFollowingNewsfeed);

		rlyEveryone.setOnClickListener(this);
		rlyFollowing.setOnClickListener(this);

		imgvSpinner = (ImageView)v.findViewById(id.imgvSpinnerNewsFeed);
		lyContent = (LinearLayout)v.findViewById(id.lyContentNewsFeed);
		scrollView = (ScrollView)v.findViewById(id.scrollViewNewsFeed);

		scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

			@Override
			public void onScrollChanged() {
				int scrollY = scrollView.getScrollY();

				//DO SOMETHING WITH THE SCROLL COORDINATES
				if(scrollY < - 15 && !isLoadingMore){
					isLoadingMore = true;
					loadMore();
					return;
				}

				View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

				// Calculate the scrolldiff
				int diff = (view.getBottom() - (scrollView.getHeight() + scrollY));

				// if diff is zero, then the bottom has been reached
				if( diff == 0 && !isGettingMore)
				{
					// notify that we have reached the bottom
					isGettingMore = true;
					imgvSpinner.setVisibility(View.VISIBLE);
					Commons.getWallImagesForNewsFeed(NewsFeedFragment.this, Constants.LIMIT_NUMBER_LIST);
				}
			}
		});

		init();
		loadData();

		return v;
	}

	public void loadMore(){
		Commons.loadMoreForNewsFeed(NewsFeedFragment.this, Constants.LIMIT_NUMBER_LIST);
	}

	public void didGetWallImageForNewsFeed(){
		isGettingMore = false;
		imgvSpinner.setVisibility(View.GONE);

		loadData();
	}

	public void didLoadMoreForNewsFeed(){
		isLoadingMore = false;

		loadData();
	}

	public void init(){
		arrEveryone = new ArrayList<WallImage>();
		arrFollowing = new ArrayList<WallImage>();
		arrData = new ArrayList<WallImage>();

		AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
		spinner.start();

		imgvSpinner.setVisibility(View.GONE);
	}

	public void loadData(){
		arrEveryone = (ArrayList<WallImage>)DataStore.getInstance().wallImagesForNewsFeed.clone();
		arrFollowing.clear();

		for(WallImage wallImage : arrEveryone){
			if(Global.myInfo.arrFollowing.contains(wallImage.strImageObjId)) arrFollowing.add(wallImage);
		}

		loadInitData();
	}

	public void loadInitData(){
		if(viewMode == Commons.ViewMode.viewEveryone)
			arrData = arrEveryone;
		else
			arrData = arrFollowing;

		presentData();
	}

	public void scrollToTop(){
		scrollView.fullScroll(ScrollView.FOCUS_UP);
	}

	public void presentData(){
		lyContent.removeAllViews();

		for(int i = 0; i < arrData.size(); i ++){
			if(i > 0){
				LinearLayout betweenLayout = new LinearLayout(getActivity());
				betweenLayout.setLayoutParams(new LinearLayout.LayoutParams(100, 5));

				lyContent.addView(betweenLayout);
			}

			final WallImage wallImage = arrData.get(i);
			final LinearLayout newCell = (LinearLayout)(View.inflate(getActivity(), layout.cell_wall_image, null));

			CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(id.imgbUserPhoto);
			Button 			  btnFullName = (Button)newCell.findViewById(id.btnFullName);
			TextView          tvTime = (TextView)newCell.findViewById(id.tvTime);
			Button            btnRecipe = (Button)newCell.findViewById(id.btnRecipe);
			TextView          tvSelfComment = (TextView)newCell.findViewById(id.tvSelfComment);
			final ImageView         imgvWall = (ImageView)newCell.findViewById(id.imgvWall);
			final Button            btnRecipeRequest = (Button)newCell.findViewById(id.btnRecipeRequest);
			Button            btnShareFacebook = (Button)newCell.findViewById(id.btnShareFacebook);
			RelativeLayout    rlyLike = (RelativeLayout)newCell.findViewById(id.rlyLike);
			RelativeLayout    rlyComment = (RelativeLayout)newCell.findViewById(id.rlyComment);
			RelativeLayout    rlyFavorite = (RelativeLayout)newCell.findViewById(id.rlyFavorite);
			TextView          tvLike = (TextView)newCell.findViewById(id.tvLike);
			TextView          tvComment = (TextView)newCell.findViewById(id.tvComment);
			final ImageView   imgvLike = (ImageView)newCell.findViewById(id.imgvLike);
			ImageView         imgvComment = (ImageView)newCell.findViewById(id.imgvComment);
			final ImageView   imgvFavorite = (ImageView)newCell.findViewById(id.imgvFavorite);

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
						btnRecipeRequest.setText(string.recipe_requested);
						btnRecipeRequest.setBackgroundResource(drawable.borders_recipe_requested);
						btnRecipeRequest.setTextColor(getResources().getColor(color.white));
					} else {
						btnRecipeRequest.setText(string.recipe_request);
						btnRecipeRequest.setBackgroundResource(drawable.borders_recipe_request);
						btnRecipeRequest.setTextColor(getResources().getColor(color.red_app));
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
							Commons.onTagSelected(getActivity(), strTag);
						}
					};

					ss.setSpan(clickableSpan, nPos, nPos + strTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				tvSelfComment.setText(ss);
                tvSelfComment.setMovementMethod(LinkMovementMethod.getInstance());
			}
//			Display display = getActivity().getWindowManager().getDefaultDisplay();
//			int width = display.getWidth();
//			LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,width);
//
//			parms.setMargins(30, 5, 30, 0);
//			imgvWall.setLayoutParams(parms);

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
				imgvLike.setBackgroundResource(drawable.post_icons_heart_fill);
			else
				imgvLike.setBackgroundResource(drawable.post_icons_heart);

			if(wallImage.commented)
				imgvComment.setBackgroundResource(drawable.post_icons_comment_fill);
			else
				imgvComment.setBackgroundResource(drawable.post_icons_comment);

			if(wallImage.favorited)
				imgvFavorite.setBackgroundResource(drawable.post_icons_star_fill);
			else
				imgvFavorite.setBackgroundResource(drawable.post_icons_star);

			btnFullName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Commons.onOtherProfile(getActivity(), wallImage.strUserObjId);
				}
			});

			btnRecipe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Commons.onRecipeView(getActivity(), wallImage);
				}
			});

			rlyLike.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ParseObject pObj = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

					wallImage.liked = !wallImage.liked;

					if(wallImage.liked){
						imgvLike.setBackgroundResource(drawable.ic_liked);
						pObj.addUnique(Constants.pKeyLikes, Global.myInfo.strUserObjID);
						wallImage.nNumberLikes ++;
						Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyLiked);

					}else{
						imgvLike.setBackgroundResource(drawable.ic_like);
						pObj.removeAll(Constants.pKeyLikes, Arrays.asList(Global.myInfo.strUserObjID));
						wallImage.nNumberLikes --;
					}

					pObj.saveInBackground();
				}
			});

			rlyComment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Commons.onCommentView(getActivity(), wallImage);
				}
			});

			rlyFavorite.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ParseUser currentUser = ParseUser.getCurrentUser();
					wallImage.favorited = !wallImage.favorited;

					if(wallImage.favorited){
						imgvFavorite.setBackgroundResource(drawable.ic_favorited);
						Global.myInfo.arrFavorites.add(wallImage.strImageObjId);
						currentUser.addUnique(Constants.pKeyFavorites, wallImage.strImageObjId);
						Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyAddFavorite);
					}else{
						imgvFavorite.setBackgroundResource(drawable.ic_favorite);
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

					btnRecipeRequest.setText(string.recipe_requested);
					btnRecipeRequest.setBackgroundResource(drawable.borders_recipe_requested);
					btnRecipeRequest.setTextColor(getResources().getColor(color.white));

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
					Commons.onShareFacebook(getActivity(), wallImage);
				}
			});

			imgvUserPhoto.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Commons.onOtherProfile(getActivity(), wallImage.strUserObjId);
				}
			});

			imgvWall.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(wallImage.strRecipe.length() > 0)
						Commons.onRecipeView(getActivity(), wallImage);
					else
						Commons.onCommentView(getActivity(), wallImage);
				}
			});

			lyContent.addView(newCell);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if(v == rlyEveryone){
			viewMode = Commons.ViewMode.viewEveryone;
			imgvEveryone.setImageResource(R.drawable.two_tabs_1_active);
			imgvFollowing.setImageResource(R.drawable.two_tabs_2);
			loadInitData();
		}

		if(v == rlyFollowing){
			viewMode = Commons.ViewMode.viewFollowing;
			imgvEveryone.setImageResource(R.drawable.two_tabs_1);
			imgvFollowing.setImageResource(R.drawable.two_tabs_2_active);
			loadInitData();
		}
	}

}
