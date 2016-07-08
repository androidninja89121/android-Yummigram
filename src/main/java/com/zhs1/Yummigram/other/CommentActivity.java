package com.zhs1.Yummigram.other;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.SignupActivity;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.model.WallImageComment;
import com.zhs1.Yummigram.utils.CustomActivity;
import com.zhs1.Yummigram.utils.ProgressHUD;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by I54460 on 6/30/2015.
 */
public class CommentActivity extends CustomActivity implements OnClickListener{
    WallImage wallImage;
    Button btnBack, btnPost;
    EditText etxtComment;
    LinearLayout lyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_comment);

        lyContent = (LinearLayout)findViewById(R.id.lyContentComment);
        etxtComment = (EditText)findViewById(R.id.etxtComment);

        btnBack = (Button)findViewById(R.id.btnBackComment);
        btnPost = (Button)findViewById(R.id.btnPostComment);

        btnBack.setOnClickListener(this);
        btnPost.setOnClickListener(this);

        Intent myIntent = getIntent(); // gets the previously created intent

        String strImageObjId = myIntent.getStringExtra(Constants.eKeyImageObjId);
        wallImage = DataStore.getInstance().wallImageMap.get(strImageObjId);

        initWallImage();

        Global.lastCommentUpdate = Calendar.getInstance().getTime();
        Global.wallImage = wallImage;

        DataStore.getInstance().comments.clear();

        Commons.getCommments(CommentActivity.this, 2);
    }

    public void initWallImage(){
        CircularImageView imgvUserPhoto = (CircularImageView) findViewById(R.id.imgbUserPhoto);
        Button btnFullName = (Button) findViewById(R.id.btnFullName);
        TextView tvTime = (TextView) findViewById(R.id.tvTime);
        Button            btnRecipe = (Button) findViewById(R.id.btnRecipe);
        TextView          tvSelfComment = (TextView) findViewById(R.id.tvSelfComment);
        final ImageView imgvWall = (ImageView) findViewById(R.id.imgvWall);
        final Button            btnRecipeRequest = (Button) findViewById(R.id.btnRecipeRequest);
        Button            btnShareFacebook = (Button) findViewById(R.id.btnShareFacebook);
        RelativeLayout rlyLike = (RelativeLayout) findViewById(R.id.rlyLike);
        RelativeLayout    rlyComment = (RelativeLayout) findViewById(R.id.rlyComment);
        RelativeLayout    rlyFavorite = (RelativeLayout) findViewById(R.id.rlyFavorite);
        TextView          tvLike = (TextView) findViewById(R.id.tvLike);
        TextView          tvComment = (TextView) findViewById(R.id.tvComment);
        final ImageView   imgvLike = (ImageView) findViewById(R.id.imgvLike);
        ImageView         imgvComment = (ImageView) findViewById(R.id.imgvComment);
        final ImageView   imgvFavorite = (ImageView) findViewById(R.id.imgvFavorite);

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
                        Commons.onTagSelected(CommentActivity.this, strTag);
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
                Commons.onOtherProfile(CommentActivity.this, wallImage.strUserObjId);
            }
        });

        btnRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.onRecipeView(CommentActivity.this, wallImage);
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
                Commons.onShareFacebook(CommentActivity.this, wallImage);
            }
        });

        imgvUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.onOtherProfile(CommentActivity.this, wallImage.strUserObjId);
            }
        });

    }

    public void didGetComments(){
        presentData();
    }

    public void presentData(){
        lyContent.removeAllViews();

        for(int i = 0; i < DataStore.getInstance().comments.size(); i ++){
            WallImageComment comment = DataStore.getInstance().comments.get(i);
            UserInfo userInfo1 = Commons.getUserInfoFrom(comment.strUserObjId);

            LinearLayout newCellComment = (LinearLayout)(View.inflate(this, R.layout.cell_comment, null));

            CircularImageView imgvUserPhoto1 = (CircularImageView)newCellComment.findViewById(R.id.imgbUserPhoto);
            Button btnFullName1 = (Button)newCellComment.findViewById(R.id.btnFullName);
            TextView tvTime1 = (TextView)newCellComment.findViewById(R.id.tvTime);
            TextView tvComment1 = (TextView)newCellComment.findViewById(R.id.tvComment);

            if(userInfo1.bmpPhoto == null){
                imgvUserPhoto1.setImageResource(R.drawable.btn_profile);
            }else{
                imgvUserPhoto1.setImageBitmap(userInfo1.bmpPhoto);
            }

            btnFullName1.setText(userInfo1.strUserFirstName + " " + userInfo1.strUserLastName);
            tvTime1.setText(Commons.getTime(comment.createdDate));
            tvComment1.setText(comment.strComment);

            lyContent.addView(newCellComment);
        }

        if(DataStore.getInstance().comments.size() < wallImage.arrComments.size()){
            LinearLayout newCellShowMore = (LinearLayout)(View.inflate(this, R.layout.cell_show_more, null));

            newCellShowMore.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Commons.getCommments(CommentActivity.this, 4);
                }
            });

            lyContent.addView(newCellShowMore);
        }
    }

    public void postComment(String strComment){
        final ParseObject commentObj = ParseObject.create(Constants.pClassWallImageComments);

        commentObj.put(Constants.pKeyComments, strComment);
        commentObj.put(Constants.pKeyUserObjId, Global.myInfo.strUserObjID);
        commentObj.put(Constants.pKeyUserFBId, Global.myInfo.strFacebookID);
        commentObj.put(Constants.pKeyImageObjId, wallImage.strImageObjId);

        Commons.showProgressHUD(getBaseContext(), "Wait...");

        commentObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Commons.dismissProgressHUD();

                if(e == null){
                    WallImageComment comment = new WallImageComment(commentObj);

                    DataStore.getInstance().comments.add(0, comment);

                    wallImage.commented = true;

                    Global.lastCommentUpdate = commentObj.getCreatedAt();

                    wallImage.arrComments.add(commentObj.getObjectId());

                    ParseObject pObjWallImage = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

                    pObjWallImage.addUnique(Constants.pKeyComments, commentObj.getObjectId());

                    pObjWallImage.saveInBackground();

                    Commons.postNotifyWithImage(wallImage, Commons.NotifyType.notifyComment);

                    presentData();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }

        if(v == btnPost){
            etxtComment.clearFocus();

            String strComment = etxtComment.getText().toString();

            if(strComment.length() > 0) postComment(strComment);

           etxtComment.setText("");
        }
    }
}
