package com.zhs1.Yummigram.main;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zhs1.Yummigram.adapter.SearchViewGridAdapter;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;

import java.util.ArrayList;
import java.util.Arrays;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by I54460 on 6/30/2015.
 */
public class SearchViewFragment extends Fragment implements View.OnClickListener{
    String strSearch, strMode;
    ScrollView scrollView;
    LinearLayout lyContent;
    GridViewWithHeaderAndFooter gridView;
    boolean isGridGettingMore = false, isListGettingMore = false, isGridView = true;
    SearchViewGridAdapter adapterGrid;
    ImageView imgvGridViewForGrid, imgvListViewForGrid, imgvGridViewForList, imgvListViewForList, imgvSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_search_view, null);

        this.strSearch = getArguments().getString(Constants.eKeySearchKey);
        this.strMode = getArguments().getString(Constants.eKeyMode);

        loadDataForSearchView();

        scrollView = (ScrollView)v.findViewById(R.id.scrollViewSearchView);
        lyContent = (LinearLayout)v.findViewById(R.id.lyContentSearchView);
        gridView = (GridViewWithHeaderAndFooter)v.findViewById(R.id.gridViewSearchView);

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

        imgvGridViewForGrid = (ImageView)headerView.findViewById(R.id.imgvGridView);
        imgvListViewForGrid = (ImageView)headerView.findViewById(R.id.imgvListView);

        imgvListViewForGrid.setOnClickListener(this);

        gridView.addHeaderView(headerView);

        adapterGrid = new SearchViewGridAdapter(getActivity());

        gridView.setAdapter(adapterGrid);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                WallImage wallImage = DataStore.getInstance().wallImagesForSearch.get(position);

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

                if(view.getBottom() < gridView.getHeight()) return;
                // Calculate the scrolldiff
                int diff = (view.getBottom() - (gridView.getHeight() + scrollY));

                // if diff is zero, then the bottom has been reached
                if (diff < -10 && !isGridGettingMore) {
                    if(!isGridView) return;
                    // notify that we have reached the bottom
                    isGridGettingMore = true;
                    imgvSpinner.setVisibility(View.VISIBLE);

                    loadNextData();
                }
            }
        });

        imgvSpinner = (ImageView)v.findViewById(R.id.imgvSpinnerSearchView);

        AnimationDrawable spinner = (AnimationDrawable) imgvSpinner.getBackground();
        spinner.start();

        imgvSpinner.setVisibility(View.GONE);

        scrollView.setVisibility(View.INVISIBLE);

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
                if (diff < -10 && !isListGettingMore) {
                    if(isGridView) return;
                    // notify that we have reached the bottom
                    isListGettingMore = true;
                    imgvSpinner.setVisibility(View.VISIBLE);

                    loadNextData();
                }
            }
        });

        return v;
    }

    public void loadNextData(){
        int nLimit = (isGridView)? Constants.LIMIT_NUMBER_GRID : Constants.LIMIT_NUMBER_LIST;

        if(this.strMode.equals(Constants.fragNewsFeed)){
            Commons.getWallImageForSearchOfNewsFeed(SearchViewFragment.this, this.strSearch, nLimit);
        }else if(this.strMode.equals(Constants.fragRecipe)){
            Commons.getWallImageForSearchOfRecipe(SearchViewFragment.this, this.strSearch, nLimit);
        }else{
            Commons.getWallImageForSearchOfFavorite(SearchViewFragment.this, this.strSearch, nLimit);
        }
    }

    public void didGetWallImageForSearch(){
        if(isGridView){
            isGridGettingMore = false;
        }else{
            isListGettingMore = false;
        }

        imgvSpinner.setVisibility(View.GONE);

        adapterGrid.notifyDataSetChanged();
        gridView.invalidateViews();

        presentData();
    }

    public void presentData(){
        lyContent.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View headerView = layoutInflater.inflate(R.layout.cell_view_mode, null);

        imgvGridViewForList = (ImageView)headerView.findViewById(R.id.imgvGridView);
        imgvListViewForList = (ImageView)headerView.findViewById(R.id.imgvListView);

        imgvGridViewForList.setOnClickListener(this);

        imgvGridViewForList.setImageResource(R.drawable.photos_table_view);
        imgvListViewForList.setImageResource(R.drawable.photos_list_view_active);

        lyContent.addView(headerView);

        for(int i = 0; i < DataStore.getInstance().wallImagesForSearch.size(); i ++){
            if(i > 0){
                LinearLayout betweenLayout = new LinearLayout(getActivity());
                betweenLayout.setLayoutParams(new LinearLayout.LayoutParams(100, 5));

                lyContent.addView(betweenLayout);
            }

            final WallImage wallImage = DataStore.getInstance().wallImagesForSearch.get(i);
            final LinearLayout newCell = (LinearLayout)(View.inflate(getActivity(), R.layout.cell_wall_image, null));

            CircularImageView imgvUserPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhoto);
            Button btnFullName = (Button)newCell.findViewById(R.id.btnFullName);
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
                            Commons.onTagSelected(getActivity(), strTag);
                        }
                    };

                    ss.setSpan(clickableSpan, nPos, nPos + strTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                tvSelfComment.setText(ss);
            }
//            Display display = getActivity().getWindowManager().getDefaultDisplay();
//            int width = display.getWidth();
//            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,width);
//
//            imgvWall.setLayoutParams(parms);

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

            btnFullName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(getActivity(), wallImage.strUserObjId);
                }
            });

            btnRecipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onRecipeView(getActivity(), wallImage);
                }
            });

            rlyLike.setOnClickListener(new View.OnClickListener() {
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

            rlyComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onCommentView(getActivity(), wallImage);
                }
            });

            rlyFavorite.setOnClickListener(new View.OnClickListener() {
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

            btnRecipeRequest.setOnClickListener(new View.OnClickListener() {
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

            btnShareFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onShareFacebook(getActivity(), wallImage);
                }
            });

            imgvUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.onOtherProfile(getActivity(), wallImage.strUserObjId);
                }
            });

            imgvWall.setOnClickListener(new View.OnClickListener() {
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

    public void scrollToTop(){
        Toast.makeText(getActivity(), "should scroll to top", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v == imgvListViewForGrid){
            isGridView = false;

            scrollView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);

            presentData();
        }

        if(v == imgvGridViewForList){
            isGridView = true;

            scrollView.setVisibility(View.INVISIBLE);
            gridView.setVisibility(View.VISIBLE);

            adapterGrid.notifyDataSetChanged();
            gridView.invalidateViews();
        }
    }

    public void loadDataForSearchView(){
        DataStore.getInstance().wallImagesForSearch.clear();

        ArrayList<WallImage> arrDataTmp = new ArrayList<WallImage>();

        if(strMode.equals(Constants.fragNewsFeed)) {
            arrDataTmp = (ArrayList<WallImage>)DataStore.getInstance().wallImagesForNewsFeed.clone();
        }else if(strMode.equals(Constants.fragRecipe)){
            arrDataTmp = (ArrayList<WallImage>)DataStore.getInstance().wallImagesForRecipe.clone();
        }else if(strMode.equals(Constants.fragFavorite)){
            arrDataTmp = (ArrayList<WallImage>)DataStore.getInstance().wallImagesForFavorites.clone();
        }

        for(WallImage wallImage : arrDataTmp){
            if(wallImage.strRecipe.contains(strSearch) || wallImage.strSelfComments.contains(strSearch)){
                DataStore.getInstance().wallImagesForSearch.add(wallImage);
            }
        }
    }
}
