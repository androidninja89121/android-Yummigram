package com.zhs1.Yummigram.signup;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.LoginActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.SignupActivity;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.UserInfo;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WelcomeFragment extends Fragment implements OnClickListener {
	Button btnLogin, btnFacebook, btnCreateAccount;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_welcome, null);
		
		btnLogin         = (Button)v.findViewById(R.id.btnLogin);
		btnFacebook      = (Button)v.findViewById(R.id.btnSigninWithFacebook);
		btnCreateAccount = (Button)v.findViewById(R.id.btnCreateAccount);
		
		btnLogin.setOnClickListener(this);
		btnFacebook.setOnClickListener(this);
		btnCreateAccount.setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btnLogin) 		  onLoginClick(v);
		if(v == btnFacebook)      onFacebookClick(v);
		if(v == btnCreateAccount) onCreateAccountClick(v);
	}
	
	public void onLoginClick(View v){
		((SignupActivity)getActivity()).setupContainer(1);
	}
	
	public void onFacebookClick(View v)
	{
		Commons.showProgressHUD(getActivity(), "Logging in...");
		List<String> permissions = Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL, "public_profile");

		ParseFacebookUtils.logIn(permissions, getActivity(), new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException err) {
				if (user == null) {
					/*************** if there is facebook app so user is null try to use embed facebook login activity **************/
					Commons.dismissProgressHUD();
				} else if (user.isNew()) {
					Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {

						@Override
						public void onCompleted(GraphUser graphUser, Response response) {
							if (graphUser != null) {
								final String fToken = Session.getActiveSession().getAccessToken();
								final GraphUser fuser = graphUser;
								final ParseUser user = ParseUser.getCurrentUser();

								try {
									String imgUrl = "https://graph.facebook.com/" + fuser.getId() + "/picture?type=large";
									URL url = new URL(imgUrl);
									Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
									ByteArrayOutputStream stream = new ByteArrayOutputStream();
									// Compress image to lower quality scale 1 - 100
									bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
									byte[] imageData = stream.toByteArray();

									final ParseFile pImageFile = new ParseFile("profile.png", imageData);

									pImageFile.saveInBackground(new SaveCallback() {
										@Override
										public void done(ParseException e) {
											if(e != null){
												user.put(Constants.pKeyFirstName, (fuser.getFirstName() == null)? "": fuser.getFirstName());
												user.put(Constants.pKeyLastName, (fuser.getLastName() == null)? "": fuser.getLastName());

												String strGender = (fuser.getProperty("gender") == null)? "male": (String) fuser.getProperty("gender");
												boolean isMale = strGender.equals("male");

												user.put(Constants.pKeyGender, Boolean.valueOf(isMale));
												user.put(Constants.pKeyFBID, fuser.getId());
												user.put(Constants.pKeyFBToken, fToken);
												user.put(Constants.pKeyPhoto, pImageFile);
												user.setEmail(fuser.getProperty("email").toString());
												user.put(Constants.pKeyNotifyComments, Boolean.valueOf(true));
												user.put(Constants.pKeyNotifyMessage, Boolean.valueOf(true));
												user.put(Constants.pKeyNotifyLike, Boolean.valueOf(false));
												user.put(Constants.pKeyNotifyFollow, Boolean.valueOf(true));
												user.put(Constants.pKeyNotifyFavorite, Boolean.valueOf(true));

												final ParseObject pObjFollow = new ParseObject(Constants.pClassFollow);

												pObjFollow.put(Constants.pKeyFollower, new ArrayList());
												pObjFollow.put(Constants.pKeyFollowing, new ArrayList());

												pObjFollow.saveInBackground(new SaveCallback() {
													@Override
													public void done(ParseException e) {
														if(e != null){
															user.put(Constants.pKeyFollowID, pObjFollow.getObjectId());

															user.saveInBackground(new SaveCallback() {
																@Override
																public void done(ParseException e) {

																	Commons.dismissProgressHUD();

																	if(e != null){
																		
																		SharedPreferences.Editor editor = Global.pref.edit();
																		editor.putBoolean(Constants.prefLogged, true);
																		editor.commit();

																		Global.myInfo = new UserInfo(user);

																		((SignupActivity)getActivity()).gotoMain();

																	}else{
																		Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
																	}
																}
															});
														}else{
															Commons.dismissProgressHUD();
															Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
														}
													}
												});
											}else{
												Commons.dismissProgressHUD();
												Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
											}
										}
									});

								} catch (Exception e) {
									Commons.dismissProgressHUD();
									Toast.makeText(getActivity(), "Error Occured", Toast.LENGTH_SHORT).show();
								}

							} else {
								Commons.dismissProgressHUD();
								Toast.makeText(getActivity(), "Error Occured", Toast.LENGTH_SHORT).show();
							}
						}
					});

					request.executeAsync();
				} else {

					Commons.dismissProgressHUD();

					Global.myInfo = new UserInfo(user);

					((SignupActivity) getActivity()).gotoMain();

				}
			}
		});
	}
	
	public void onCreateAccountClick(View v){
		((SignupActivity)getActivity()).setupContainer(2);
	}
	
}
