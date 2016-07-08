package com.zhs1.Yummigram.main;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.parse.ParseUser;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

/**
 * Created by I54460 on 7/6/2015.
 */
public class SettingFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{
    SwitchButton sbComment, sbMessage, sbLike, sbFollow, sbFavorite, sbPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_setting, null);

        sbComment = (SwitchButton)v.findViewById(R.id.switchBtnComment);
        sbMessage = (SwitchButton)v.findViewById(R.id.switchBtnMessage);
        sbLike = (SwitchButton)v.findViewById(R.id.switchBtnLike);
        sbFollow = (SwitchButton)v.findViewById(R.id.switchBtnFollow);
        sbFavorite = (SwitchButton)v.findViewById(R.id.switchBtnFavorite);
        sbPhoto = (SwitchButton)v.findViewById(R.id.switchBtnPhoto);

        ParseUser currentUser = ParseUser.getCurrentUser();

        sbComment.setChecked(currentUser.getBoolean(Constants.pKeyNotifyComments));
        sbMessage.setChecked(currentUser.getBoolean(Constants.pKeyNotifyMessage));
        sbLike.setChecked(currentUser.getBoolean(Constants.pKeyNotifyLike));
        sbFollow.setChecked(currentUser.getBoolean(Constants.pKeyNotifyFollow));
        sbFavorite.setChecked(currentUser.getBoolean(Constants.pKeyNotifyFavorite));
        sbPhoto.setChecked(Global.pref.getBoolean(Constants.prefPhotoEffects, true));

        sbComment.setOnCheckedChangeListener(this);
        sbMessage.setOnCheckedChangeListener(this);
        sbLike.setOnCheckedChangeListener(this);
        sbFollow.setOnCheckedChangeListener(this);
        sbFavorite.setOnCheckedChangeListener(this);

        return v;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if(buttonView == sbComment){
            currentUser.put(Constants.pKeyNotifyComments, isChecked);
        }

        if(buttonView == sbMessage){
            currentUser.put(Constants.pKeyNotifyMessage, isChecked);
        }

        if(buttonView == sbLike){
            currentUser.put(Constants.pKeyNotifyLike, isChecked);
        }

        if(buttonView == sbFollow){
            currentUser.put(Constants.pKeyNotifyFollow, isChecked);
        }

        if(buttonView == sbFavorite){
            currentUser.put(Constants.pKeyNotifyFavorite, isChecked);
        }

        if(buttonView == sbPhoto){
            SharedPreferences.Editor editor = Global.pref.edit();

            editor.putBoolean(Constants.prefPhotoEffects, isChecked);

            editor.commit();
        }

        currentUser.saveInBackground();
    }
}
