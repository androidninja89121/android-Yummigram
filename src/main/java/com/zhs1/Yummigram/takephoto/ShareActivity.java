package com.zhs1.Yummigram.takephoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.utils.SoftKeyboardHandledLinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ShareActivity extends Activity implements View.OnClickListener {
    Button btnCheckBox, btnSharePhotoOnly, btnShareRecipe, btnBack, btnPrev, btnNext, btnDone;
    EditText etxtComment, etxtRecipeName, etxtIngrediants, etxtDirections;
    ImageView imgvShare;
    boolean isChecked = false;
    RelativeLayout lyToolBar;
    SoftKeyboardHandledLinearLayout lyMainView;
    View focusedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        btnBack           = (Button)findViewById(R.id.btnBackShare);
        btnCheckBox       = (Button)findViewById(R.id.btnCheckBoxShareOnFacebook);
        btnSharePhotoOnly = (Button)findViewById(R.id.btnSharePhotoOnly);
        btnShareRecipe    = (Button)findViewById(R.id.btnShareRecipe);
        btnPrev           = (Button)findViewById(R.id.btnPrev);
        btnNext           = (Button)findViewById(R.id.btnNext);
        btnDone           = (Button)findViewById(R.id.btnDone);

        lyToolBar  = (RelativeLayout)findViewById(R.id.my_toolbar);
        lyMainView = (SoftKeyboardHandledLinearLayout)findViewById(R.id.lyMainView);

        lyMainView.setOnSoftKeyboardVisibilityChangeListener(
                new SoftKeyboardHandledLinearLayout.SoftKeyboardVisibilityChangeListener() {
                    @Override
                    public void onSoftKeyboardShow() {
                        // TODO: do something here
                        lyToolBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSoftKeyboardHide() {
                        // TODO: do something here
                        lyToolBar.setVisibility(View.GONE);
                    }
                });

        imgvShare = (ImageView)findViewById(R.id.imgvShare);

        etxtComment     = (EditText)findViewById(R.id.etxtAddComment);
        etxtRecipeName  = (EditText)findViewById(R.id.etxtAddRecipeName);
        etxtIngrediants = (EditText)findViewById(R.id.etxtAddIngrediant);
        etxtDirections  = (EditText)findViewById(R.id.etxtAddDirection);

        btnBack.setOnClickListener(this);
        btnCheckBox.setOnClickListener(this);
        btnSharePhotoOnly.setOnClickListener(this);
        btnShareRecipe.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        etxtComment.setOnFocusChangeListener(focusListener);
        etxtRecipeName.setOnFocusChangeListener(focusListener);
        etxtIngrediants.setOnFocusChangeListener(focusListener);
        etxtDirections.setOnFocusChangeListener(focusListener);

        imgvShare.setImageURI(Global.uriSharePhoto);
        lyToolBar.setVisibility(View.GONE);
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                focusedView = v;
            }else{
                focusedView = null;
            }
        }
    };

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            Intent intent = new Intent(ShareActivity.this, TakePhotoActivity.class);
            startActivity(intent);

            finish();
        }

        if(v == btnCheckBox){
            isChecked = !isChecked;

            if(isChecked){
                btnCheckBox.setBackgroundResource(R.drawable.icon_check);
            }else{
                btnCheckBox.setBackgroundResource(R.drawable.icon_check_unchecked);
            }
        }

        if(v == btnShareRecipe){
            shareRecipe();
        }

        if (v == btnSharePhotoOnly) {
            sharePhotoOnly();
        }

        if(v == btnPrev){
            if(focusedView == etxtDirections){
                etxtIngrediants.requestFocus();
            }else if(focusedView == etxtIngrediants){
                etxtRecipeName.requestFocus();
            }
        }

        if(v == btnNext){
            if(focusedView == etxtRecipeName){
                etxtIngrediants.requestFocus();
            }else if(focusedView == etxtIngrediants){
                etxtDirections.requestFocus();
            }
        }

        if(v == btnDone){
            if(focusedView == null) return;

            if(focusedView == etxtComment){
                sharePhotoOnly();
            }else{
                shareRecipe();
            }
        }
    }


    public void sharePhotoOnly(){
        uploadImage(false);
    }

    public void shareRecipe(){
        uploadImage(true);
    }

    public void uploadImage(final boolean isShareWithRecipe){

        final String  strComments = etxtComment.getText().toString();
        final String  strRecipe   = etxtRecipeName.getText().toString();
        final String  strIngredients = etxtIngrediants.getText().toString();
        final String  strDirections = etxtDirections.getText().toString();

        if(isShareWithRecipe){
            if(strRecipe.length() == 0){
                Toast.makeText(getBaseContext(), "Please add the recipe name", Toast.LENGTH_SHORT).show();
                return;
            }

            if(strIngredients.length() == 0){
                Toast.makeText(getBaseContext(), "Please add the ingredient", Toast.LENGTH_SHORT).show();
                return;
            }

            if(strDirections.length() == 0){
                Toast.makeText(getBaseContext(), "Please add the directions", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bitmap bitmap = ((BitmapDrawable)imgvShare.getDrawable()).getBitmap();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] imgData=stream.toByteArray();

        if(isChecked){
            if(Global.myInfo.strFacebookToken.length() == 0){
                Toast.makeText(getBaseContext(), "You have no facebook account, please set up the facebook account", Toast.LENGTH_SHORT).show();
                return;
            }else{
                String strRealComments = "";

                if(isShareWithRecipe){
                    strRealComments = strRecipe + " RECIPE -> www.yummigram.com/recipeid\n " + strComments;
                }else{
                    strRealComments = strComments + "\nShared via Yummigram -> www.yummigram.com";
                }

                Commons.postImageToFB(imgData, strRealComments);
            }
        }

        final ParseFile imageFile = new ParseFile(imgData, "img");

        Commons.showProgressHUD(this, "Uploading");
        lyToolBar.setVisibility(View.GONE);

        imageFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    final ParseObject pObjWallImage = new ParseObject(Constants.pClassWallImageOther);

                    pObjWallImage.put(Constants.pKeyImage, imageFile.getUrl());
                    pObjWallImage.put(Constants.pKeyUserFBId, Global.myInfo.strFacebookID);
                    pObjWallImage.put(Constants.pKeyUserObjId, Global.myInfo.strUserObjID);
                    pObjWallImage.put(Constants.pKeyUserFullName, Global.myInfo.strUserFirstName + Global.myInfo.strUserLastName);
                    pObjWallImage.put(Constants.pKeySelfComment, strComments);
                    pObjWallImage.put(Constants.pKeyTag, Commons.getTagsFromComment(strComments));

                    if(Global.strCity.length() < 1){
                        Global.strCity = Global.pref.getString(Constants.pKeyCity, "");
                        Global.strCountry = Global.pref.getString(Constants.pKeyCountry, "");
                    }

                    pObjWallImage.put(Constants.pKeyCity, Global.strCity);
                    pObjWallImage.put(Constants.pKeyCountry, Global.strCountry);

                    if(isShareWithRecipe){
                        pObjWallImage.put(Constants.pKeyRecipe, strRecipe);
                        pObjWallImage.put(Constants.pKeyIngredients, strIngredients);
                        pObjWallImage.put(Constants.pKeyDirections, strDirections);
                        pObjWallImage.put(Constants.pKeyIsRecipe, 1);
                    }else{
                        pObjWallImage.put(Constants.pKeyIsRecipe, 0);
                    }

                    pObjWallImage.put(Constants.pKeyLikes, new ArrayList<String>());
                    pObjWallImage.put(Constants.pKeyFavorites, new ArrayList<String>());

                    pObjWallImage.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            Global.myInfo.arrWallImages.add(pObjWallImage.getObjectId());

                            ParseUser currentUser = ParseUser.getCurrentUser();

                            currentUser.addUnique(Constants.pKeyWallImages, pObjWallImage.getObjectId());

                            currentUser.saveInBackground(new SaveCallback() {
                                                             @Override
                                                             public void done(ParseException e) {
                                                                 Commons.dismissProgressHUD();
                                                                 finish();
                                                             }
                                                         });


                        }
                    });
                }else{
                    Commons.dismissProgressHUD();
                }
            }
        });

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
