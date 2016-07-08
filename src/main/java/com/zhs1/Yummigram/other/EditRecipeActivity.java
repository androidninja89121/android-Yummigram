package com.zhs1.Yummigram.other;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.zhs1.Yummigram.utils.SoftKeyboardHandledLinearLayout;

/**
 * Created by I54460 on 7/6/2015.
 */
public class EditRecipeActivity extends Activity implements View.OnClickListener {
    WallImage wallImage;
    Button btnBack, btnSave, btnPrev, btnNext, btnDone;
    ImageView imgView;
    EditText etxtRecipeName, etxtIngrediants, etxtDirections;
    View focusedView;
    RelativeLayout lyToolBar;
    SoftKeyboardHandledLinearLayout lyMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_recipe);

        Intent myIntent = getIntent(); // gets the previously created intent

        String strImageObjId = myIntent.getStringExtra(Constants.eKeyImageObjId);
        wallImage = DataStore.getInstance().wallImageMap.get(strImageObjId);

        btnBack = (Button)findViewById(R.id.btnBackEditRecipe);
        btnSave = (Button)findViewById(R.id.btnSaveEditRecipe);
        btnPrev = (Button)findViewById(R.id.btnPrevEditRecipe);
        btnNext = (Button)findViewById(R.id.btnNextEditRecipe);
        btnDone = (Button)findViewById(R.id.btnDoneEditRecipe);

        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        etxtRecipeName = (EditText)findViewById(R.id.etxtAddRecipeNameEditRecipe);
        etxtDirections = (EditText)findViewById(R.id.etxtAddDirectionEditRecipe);
        etxtIngrediants = (EditText)findViewById(R.id.etxtAddIngrediantEditRecipe);

        imgView = (ImageView)findViewById(R.id.imgvEditRecipe);

        lyToolBar = (RelativeLayout)findViewById(R.id.my_toolbar_edit_recipe);

        lyMainView = (SoftKeyboardHandledLinearLayout)findViewById(R.id.lyMainViewEditRecipe);

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

        etxtRecipeName.setOnFocusChangeListener(focusListener);
        etxtIngrediants.setOnFocusChangeListener(focusListener);
        etxtDirections.setOnFocusChangeListener(focusListener);

        etxtRecipeName.setText(wallImage.strRecipe);
        etxtIngrediants.setText(wallImage.strIngredients);
        etxtDirections.setText(wallImage.strDirections);

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
                    imgView.setImageBitmap(bitmap);
                    wallImage.bmpWall = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imgView.setImageBitmap(wallImage.bmpWall);
        }

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

    public void updateRecipe(){
        String strRecipeName = etxtRecipeName.getText().toString();
        String strDirections = etxtDirections.getText().toString();
        String strIngredients = etxtIngrediants.getText().toString();

        if(strRecipeName.length() == 0){
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

        ParseObject pObjWallImage = DataStore.getInstance().wallImagePFObjectMap.get(wallImage.strImageObjId);

        pObjWallImage.put(Constants.pKeyRecipe, strRecipeName);
        pObjWallImage.put(Constants.pKeyIngredients, strIngredients);
        pObjWallImage.put(Constants.pKeyDirections, strDirections);

        if(strRecipeName.length() > 0){
            pObjWallImage.put(Constants.pKeyIsRecipe, 1);
        }

        wallImage.strDirections = strDirections;
        wallImage.strIngredients = strIngredients;
        wallImage.strRecipe = strRecipeName;

        Commons.showProgressHUD(EditRecipeActivity.this, "Updating...");

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

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }

        if(v == btnSave){
            updateRecipe();
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

            updateRecipe();
        }
    }
}
