package com.zhs1.Yummigram.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.utils.CustomActivity;

/**
 * Created by I54460 on 6/30/2015.
 */
public class TagActivity extends CustomActivity {
    String strTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tag);

        Intent myIntent = getIntent(); // gets the previously created intent
        strTag = myIntent.getStringExtra(Constants.eKeyTag);
    }
}
