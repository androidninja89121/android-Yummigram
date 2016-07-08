package com.zhs1.Yummigram.other;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.utils.CustomActivity;

/**
 * Created by I54460 on 7/6/2015.
 */
public class EditCommentActivity extends CustomActivity implements View.OnClickListener {
    WallImage wallImage;
    Button btnBack, btnSave;
    ImageView imageView;
    EditText etxtComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_comment);

        Intent myIntent = getIntent(); // gets the previously created intent

        String strImageObjId = myIntent.getStringExtra(Constants.eKeyImageObjId);
        wallImage = DataStore.getInstance().wallImageMap.get(strImageObjId);

        btnBack = (Button)findViewById(R.id.btnBackEditComment);
        btnSave = (Button)findViewById(R.id.btnSaveEditComment);
        etxtComment = (EditText)findViewById(R.id.etxtCommentEditComment);
        imageView = (ImageView)findViewById(R.id.imgvEditComment);

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

        etxtComment.setText(wallImage.strSelfComments);

        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }

        if(v == btnSave){
            String strSelfComment = etxtComment.getText().toString();

            if (strSelfComment.length() > 0){
                wallImage.strSelfComments = strSelfComment;
                wallImage.arrTag = Commons.getTagsFromComment(strSelfComment);

                ParseObject pObjWallImage = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

                pObjWallImage.put(Constants.pKeySelfComment, strSelfComment);
                pObjWallImage.put(Constants.pKeyTag, wallImage.arrTag);

                Commons.showProgressHUD(EditCommentActivity.this, "Updating...");

                pObjWallImage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Commons.dismissProgressHUD();

                        if(e == null){
                            finish();
                        }else{
                            Toast.makeText(getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
