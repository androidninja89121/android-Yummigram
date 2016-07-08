package com.zhs1.Yummigram.signup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.SignupActivity;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.UserInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

public class LoginFragment extends Fragment implements OnClickListener {
	Button btnLogin, btnCreateAccount, btnBack;
	EditText etxtEmail, etxtPassword;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_login, null);
		
		btnLogin         = (Button)v.findViewById(R.id.btnLoginLogin);
		btnCreateAccount = (Button)v.findViewById(R.id.btnCreateAccountLogin);
		btnBack          = (Button)v.findViewById(R.id.btnBackLogin);
		
		etxtEmail    = (EditText)v.findViewById(R.id.etxtUsername);
		etxtPassword = (EditText)v.findViewById(R.id.etxtPassword);
		
		btnLogin.setOnClickListener(this);
		btnCreateAccount.setOnClickListener(this);
		btnBack.setOnClickListener(this);

		etxtEmail.setText(Global.pref.getString(Constants.prefEmail, ""));
		etxtPassword.setText(Global.pref.getString(Constants.prefPswd, ""));
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btnLogin) 		  onLoginClick(v);
		if(v == btnCreateAccount) onCreateAccountClick(v);
		if(v == btnBack)          onBack(v);         
	}
	
	public void onLoginClick(View v){
		String strEmail = etxtEmail.getText().toString();
		String strPassword = etxtPassword.getText().toString();

		if(strEmail.length() < 1){
			Toast.makeText(getActivity(), "Please, input your email", Toast.LENGTH_LONG).show();
			return;
		}

		if(strPassword.length() < 1){
			Toast.makeText(getActivity(), "Please, input your password", Toast.LENGTH_LONG).show();
			return;
		}

		Commons.showProgressHUD(getActivity(), "Logging In...");
		ParseUser.logInInBackground(strEmail, strPassword, new LogInCallback() {
			@Override
			public void done(ParseUser parseUser, ParseException e) {
				if(e == null){
					Global.myInfo = new UserInfo(parseUser);

					Global.lastImageUpdateForNewsFeed = Calendar.getInstance().getTime();
					Global.lastImageUpdateForRecipe = Calendar.getInstance().getTime();
					Global.lastCommentUpdate = Calendar.getInstance().getTime();
					Global.lastUserInfoUpdate = Calendar.getInstance().getTime();

					Global.otherInfo = Global.myInfo;
					Commons.getWallImagesForNewsFeed(LoginFragment.this, Constants.LIMIT_NUMBER_GRID);

				}else{
					Commons.dismissProgressHUD();
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void didGetWallImageForNewsFeed(){
		Commons.getWallImagesForRecipe(LoginFragment.this, Constants.LIMIT_NUMBER_GRID);
	}

	public void didGetWallImageForRecipe(){
		Commons.getWallImagesForFavorite(LoginFragment.this, Constants.LIMIT_NUMBER_GRID);
	}

	public void didGetWallImageForFavorites(){
		Commons.getWallImagesForMyOwn(LoginFragment.this, Constants.LIMIT_NUMBER_GRID);
	}

	public void didGetWallImageForMyOwn(){
		Commons.dismissProgressHUD();
		
		SharedPreferences.Editor editor = Global.pref.edit();
		editor.putBoolean(Constants.prefLogged, true);
		editor.putString(Constants.prefEmail, etxtEmail.getText().toString());
		editor.putString(Constants.prefPswd, etxtPassword.getText().toString());
		editor.commit();

		ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
		currentInstallation.put(Constants.pKeyUserObjId, Global.myInfo.strUserObjID);

		currentInstallation.saveInBackground();

		((SignupActivity)getActivity()).gotoMain();
	}


	public void onCreateAccountClick(View v){
		((SignupActivity)getActivity()).setupContainer(2);
	}
	
	public void onBack(View v){
		((SignupActivity)getActivity()).setupContainer(0);
	}
}
