package com.zhs1.Yummigram.other;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.zhs1.Yummigram.utils.CustomActivity;

import org.w3c.dom.Text;

import java.util.Arrays;

/**
 * Created by I54460 on 6/30/2015.
 */
public class RecipeActivity extends CustomActivity {
    WallImage wallImage;
    RelativeLayout rlyContent;
    Button btnBack;
    TextView tvRecipe, tvIngredients, tvDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recipe);

        btnBack = (Button)findViewById(R.id.btnBackRecipe);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        rlyContent = (RelativeLayout)findViewById(R.id.rlyContentRecipe);
        tvRecipe = (TextView)findViewById(R.id.tvRecipeRecipe);
        tvIngredients = (TextView)findViewById(R.id.tvIngredientsRecipe);
        tvDescription = (TextView)findViewById(R.id.tvDescriptionRecipe);

        Intent myIntent = getIntent(); // gets the previously created intent

        String strImageObjId = myIntent.getStringExtra(Constants.eKeyImageObjId);
        wallImage = DataStore.getInstance().wallImageMap.get(strImageObjId);

        tvRecipe.setText(wallImage.strRecipe);
        tvIngredients.setText(wallImage.strIngredients);
        tvDescription.setText(wallImage.strDirections);

        init();
    }

    public void init(){
//        final LinearLayout newCell = (LinearLayout)(View.inflate(this, R.layout.cell_wall_image_empty, null));

        CircularImageView imgvUserPhoto = (CircularImageView)findViewById(R.id.imgbUserPhoto);
        Button btnFullName = (Button)findViewById(R.id.btnFullName);
        TextView tvTime = (TextView)findViewById(R.id.tvTime);
        Button            btnRecipe = (Button)findViewById(R.id.btnRecipe);
        TextView          tvSelfComment = (TextView)findViewById(R.id.tvSelfComment);
        final ImageView imgvWall = (ImageView)findViewById(R.id.imgvWall);
        final Button            btnRecipeRequest = (Button)findViewById(R.id.btnRecipeRequest);
        Button            btnShareFacebook = (Button)findViewById(R.id.btnShareFacebook);
        RelativeLayout    rlyLike = (RelativeLayout)findViewById(R.id.rlyLike);
        RelativeLayout    rlyComment = (RelativeLayout)findViewById(R.id.rlyComment);
        RelativeLayout    rlyFavorite = (RelativeLayout)findViewById(R.id.rlyFavorite);
        TextView          tvLike = (TextView)findViewById(R.id.tvLike);
        TextView          tvComment = (TextView)findViewById(R.id.tvComment);
        final ImageView   imgvLike = (ImageView)findViewById(R.id.imgvLike);
        ImageView         imgvComment = (ImageView)findViewById(R.id.imgvComment);
        final ImageView   imgvFavorite = (ImageView)findViewById(R.id.imgvFavorite);

        UserInfo userInfo = Commons.getUserInfoFrom(wallImage.strUserObjId);

        if(userInfo.bmpPhoto == null){
            imgvUserPhoto.setImageResource(R.drawable.btn_profile);
        }else{
            imgvUserPhoto.setImageBitmap(userInfo.bmpPhoto);
        }

        btnFullName.setText(wallImage.strUserFullName);
        tvTime.setText(Commons.getTime(wallImage.createdDate) + " in " + wallImage.strCity + ", " + wallImage.strCountry);

        btnRecipe.setVisibility(View.GONE);
        btnRecipeRequest.setVisibility(View.GONE);

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
                        Commons.onTagSelected(RecipeActivity.this, strTag);
                    }
                };

                ss.setSpan(clickableSpan, nPos, nPos + strTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            tvSelfComment.setText(ss);
        }

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
                Commons.onOtherProfile(RecipeActivity.this, wallImage.strUserObjId);
            }
        });

        btnRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.onRecipeView(RecipeActivity.this, wallImage);
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
                Commons.onCommentView(RecipeActivity.this, wallImage);
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

        btnShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.onShareFacebook(RecipeActivity.this, wallImage);
            }
        });

        imgvUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.onOtherProfile(RecipeActivity.this, wallImage.strUserObjId);
            }
        });

//        rlyContent.addView(newCell);
    }
}
