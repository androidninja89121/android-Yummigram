package com.zhs1.Yummigram.main;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.adapter.ProfileGridAdapter;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.other.EditCommentActivity;
import com.zhs1.Yummigram.other.EditRecipeActivity;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import info.hoang8f.android.segmented.SegmentedGroup;

public class ProfileFragment extends Fragment implements OnClickListener {
	GridViewWithHeaderAndFooter gridView;
	ImageView imgvGridViewForGrid, imgvListViewForGrid, imgvGridViewForList, imgvListViewForList, imgvSpinner;
	ProfileGridAdapter adapterGrid;
	boolean isGridGettingMore = false, isListGettingMore = false, isGridView = true;
	ScrollView scrollView;
	LinearLayout lyContent;
	Button btnUserFullName;
	CircularImageView imgvUserPhoto;

	ImageView imgvPhotos, imgvFollowers, imgvFollowings;
	TextView tvPhotos, tvFollowers, tvFollowings;
	RelativeLayout rlyPhotos, rlyFollowers, rlyFollowings;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_profile, null);

		imgvUserPhoto = (CircularImageView)v.findViewById(R.id.imgbUserPhotoProfile);

		if(Global.myInfo.bmpPhoto == null){
			imgvUserPhoto.setImageResource(R.drawable.btn_profile);
		}else{
			imgvUserPhoto.setImageBitmap(Global.myInfo.bmpPhoto);
		}

		btnUserFullName = (Button)v.findViewById(R.id.btnFullNameProfile);

		btnUserFullName.setText(Global.myInfo.strUserFirstName + " " + Global.myInfo.strUserLastName );

		gridView = (GridViewWithHeaderAndFooter)v.findViewById(R.id.gridViewProfile);

		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

		imgvGridViewForGrid = (ImageView)headerView.findViewById(R.id.imgvGridView);
		imgvListViewForGrid = (ImageView)headerView.findViewById(R.id.imgvListView);

		imgvListViewForGrid.setOnClickListener(this);

		gridView.addHeaderView(headerView);

		DataStore.getInstance().wallImagesForMyOwn.clear();

		adapterGrid = new ProfileGridAdapter(getActivity());

		gridView.setAdapter(adapterGrid);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

				WallImage wallImage = DataStore.getInstance().wallImagesForMyOwn.get(position);

				if (wallImage.strRecipe.length() > 0)
					Commons.onRecipeView(getActivity(), wallImage);
				else
					Commons.onCommentView(getActivity(), wallImage);

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
					if (DataStore.getInstance().wallImagesForMyOwn.size() == Global.myInfo.arrWallImages.size())
						return;

					// notify that we have reached the bottom
					isGridGettingMore = true;
					imgvSpinner.setVisibility(View.VISIBLE);

					loadNextData();
				}
			}
		});

		imgvSpinner = (ImageView)v.findViewById(R.id.imgvSpinnerProfile);

		AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
		spinner.start();

		imgvSpinner.setVisibility(View.GONE);

		lyContent = (LinearLayout)v.findViewById(R.id.lyContentProfile);
		scrollView = (ScrollView)v.findViewById(R.id.scrollViewProfile);

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
					if (DataStore.getInstance().wallImagesForMyOwn.size() == Global.myInfo.arrWallImages.size())
						return;

					// notify that we have reached the bottom
					isListGettingMore = true;
					imgvSpinner.setVisibility(View.VISIBLE);

					loadNextData();
				}
			}
		});

		rlyPhotos = (RelativeLayout)v.findViewById(R.id.rlyPhotosSelfProfile);
		rlyFollowers = (RelativeLayout)v.findViewById(R.id.rlyFollowersSelfProfile);
		rlyFollowings = (RelativeLayout)v.findViewById(R.id.rlyFollowingsSelfProfile);

		rlyPhotos.setOnClickListener(this);
		rlyFollowers.setOnClickListener(this);
		rlyFollowings.setOnClickListener(this);

		imgvPhotos = (ImageView)v.findViewById(R.id.imgvPhotosSelfProfile);
		imgvFollowers = (ImageView)v.findViewById(R.id.imgvFollowersSelfProfile);
		imgvFollowings = (ImageView)v.findViewById(R.id.imgvFollowingsSelfProfile);

		tvPhotos = (TextView)v.findViewById(R.id.tvPhotosSelfProfile);
		tvFollowers = (TextView)v.findViewById(R.id.tvFollowersSelfProfile);
		tvFollowings = (TextView)v.findViewById(R.id.tvFollowingsSelfProfile);

		initSegmentedGroup();
		imgvPhotos.setImageResource(R.drawable.three_tabs_1_active);

		loadData();

		return v;
	}

	public void initSegmentedGroup(){
		tvPhotos.setText(Global.myInfo.arrWallImages.size() + " photos");
		tvFollowers.setText(Global.myInfo.arrFollower.size() + " Followers");
		tvFollowings.setText(Global.myInfo.arrFollowing.size() + " Followings");

		imgvPhotos.setImageResource(R.drawable.three_tabs_1);
		imgvFollowers.setImageResource(R.drawable.three_tabs_2);
		imgvFollowings.setImageResource(R.drawable.three_tabs_3);
	}

	public void presentDataForFollower(){
		lyContent.removeAllViews();

		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		for(String strObj : Global.myInfo.arrFollower){
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
					Commons.onOtherProfile(getActivity(), userInfo.strUserObjID);
				}
			});

			lyContent.addView(cell);
		}
	}

	public void presentDataForFollowing(){
		lyContent.removeAllViews();

		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		for(String strObj : Global.myInfo.arrFollowing){
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
					Commons.onOtherProfile(getActivity(), userInfo.strUserObjID);
				}
			});

			lyContent.addView(cell);
		}
	}

	public void presentDataForPhoto(){
		lyContent.removeAllViews();

		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

		imgvGridViewForList = (ImageView)headerView.findViewById(R.id.imgvGridView);
		imgvListViewForList = (ImageView)headerView.findViewById(R.id.imgvListView);

		imgvGridViewForList.setOnClickListener(this);

		imgvGridViewForList.setImageResource(R.drawable.photos_table_view);
		imgvListViewForList.setImageResource(R.drawable.photos_list_view_active);

		lyContent.addView(headerView);

		for(int i = 0; i < DataStore.getInstance().wallImagesForMyOwn.size(); i ++){
			final int nIdx = i;

			if(i > 0){
				LinearLayout betweenLayout = new LinearLayout(getActivity());
				betweenLayout.setLayoutParams(new LinearLayout.LayoutParams(100, 5));

				lyContent.addView(betweenLayout);
			}

			final WallImage wallImage = DataStore.getInstance().wallImagesForMyOwn.get(i);
			final LinearLayout newCell = (LinearLayout)(View.inflate(getActivity(), R.layout.cell_wall_image_self, null));

			CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhotoSelf);
			Button 			  btnFullName = (Button)newCell.findViewById(R.id.btnFullNameSelf);
			TextView tvTime = (TextView)newCell.findViewById(R.id.tvTimeSelf);
			Button            btnRecipe = (Button)newCell.findViewById(R.id.btnRecipeSelf);
			TextView          tvSelfComment = (TextView)newCell.findViewById(R.id.tvSelfCommentSelf);
			final ImageView         imgvWall = (ImageView)newCell.findViewById(R.id.imgvWallSelf);
			Button            btnDrop = (Button)newCell.findViewById(R.id.btnDropSelf);
			Button            btnShareFacebook = (Button)newCell.findViewById(R.id.btnShareFacebookSelf);
			TextView          tvRecipeNum = (TextView)newCell.findViewById(R.id.tvRecipeNumberSelf);
			RelativeLayout rlyLike = (RelativeLayout)newCell.findViewById(R.id.rlyLikeSelf);
			RelativeLayout    rlyComment = (RelativeLayout)newCell.findViewById(R.id.rlyCommentSelf);
			RelativeLayout    rlyFavorite = (RelativeLayout)newCell.findViewById(R.id.rlyFavoriteSelf);
			TextView          tvLike = (TextView)newCell.findViewById(R.id.tvLikeSelf);
			TextView          tvComment = (TextView)newCell.findViewById(R.id.tvCommentSelf);
			final ImageView   imgvLike = (ImageView)newCell.findViewById(R.id.imgvLikeSelf);
			ImageView         imgvComment = (ImageView)newCell.findViewById(R.id.imgvCommentSelf);
			final ImageView   imgvFavorite = (ImageView)newCell.findViewById(R.id.imgvFavoriteSelf);

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
			}else {
				btnRecipe.setVisibility(View.INVISIBLE);
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

			if(wallImage.strRecipe.length() > 0)
				tvRecipeNum.setVisibility(View.INVISIBLE);
			else{
				if(wallImage.nNumberRecipeRequests == 0){
					tvRecipeNum.setVisibility(View.INVISIBLE);
				}else{
					tvRecipeNum.setVisibility(View.VISIBLE);
					tvRecipeNum.setText("Recipe Requests: " + wallImage.nNumberRecipeRequests);
				}
			}

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
					Commons.onCommentView(getActivity(), wallImage);
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

			btnDrop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainActivity)getActivity()).nPosForProfile = nIdx;

                    boolean isNewRecipe = false;

                    if(wallImage.strRecipe.length() == 0) isNewRecipe = true;

					((MainActivity)getActivity()).onActionSheet(isNewRecipe);
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

	public void loadData(){

		DataStore.getInstance().wallImagesForMyOwn.clear();

		for(String strObjId : Global.myInfo.arrWallImages){
			WallImage wallImage = DataStore.getInstance().wallImageMap.get(strObjId);

			if(wallImage == null) continue;

			DataStore.getInstance().wallImagesForMyOwn.add(wallImage);
		}

		Collections.sort(DataStore.getInstance().wallImagesForMyOwn, new Commons.CreatedDateComparator());

		adapterGrid.notifyDataSetChanged();
		gridView.invalidateViews();

		presentDataForPhoto();
	}

	public void loadNextData(){
		Global.otherInfo = Global.myInfo;

		Commons.getWallImagesForMyOwn(ProfileFragment.this, Constants.LIMIT_NUMBER_GRID);
	}

	public void scrollToTop(){
		Toast.makeText(getActivity(), "should scroll to top", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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

	public void onEditComments(int nPos){
		Intent myIntent = new Intent(getActivity(), EditCommentActivity.class);

		myIntent.putExtra(Constants.eKeyImageObjId, DataStore.getInstance().wallImagesForMyOwn.get(nPos).strImageObjId);

		getActivity().startActivity(myIntent);
	}

	public void onAddRecipe(int nPos){
		Intent myIntent = new Intent(getActivity(), EditRecipeActivity.class);

		myIntent.putExtra(Constants.eKeyImageObjId, DataStore.getInstance().wallImagesForMyOwn.get(nPos).strImageObjId);

		getActivity().startActivity(myIntent);
	}

	public void onDelete(int nPos){
        WallImage wallImage = DataStore.getInstance().wallImagesForMyOwn.get(nPos);
        final String strImageObjId = wallImage.strImageObjId;

        Global.myInfo.arrWallImages.remove(strImageObjId);

        ParseUser currentUser = ParseUser.getCurrentUser();

        currentUser.put(Constants.pKeyWallImages, Global.myInfo.arrWallImages);

        currentUser.saveInBackground();

        DataStore.getInstance().wallImagesForMyOwn.remove(wallImage);
        DataStore.getInstance().wallImagesForNewsFeed.remove(wallImage);
        DataStore.getInstance().wallImagesForRecipe.remove(wallImage);
        DataStore.getInstance().wallImagesForFavorites.remove(wallImage);
        DataStore.getInstance().wallImagesForCategory.remove(wallImage);
        DataStore.getInstance().wallImagesForTag.remove(wallImage);

        ParseObject pObjWallImage = DataStore.getInstance().wallImagePFObjectMap.get(strImageObjId);

        pObjWallImage.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    DataStore.getInstance().wallImagePFObjectMap.remove(strImageObjId);

                    ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery(Constants.pClassWallImageComments);

                    commentQuery.orderByAscending(Constants.pKeyCreatedAt);
                    commentQuery.whereEqualTo(Constants.pKeyImageObjId, strImageObjId);

                    commentQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if(e == null){
                                try {
                                    ParseObject.deleteAll(parseObjects);
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                                DataStore.getInstance().wallImagePFObjectMap.remove(strImageObjId);

                                ((MainActivity)getActivity()).setupContainer(3);
                            }
                        }
                    });

                }
            }
        });
	}
}
