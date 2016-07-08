package com.zhs1.Yummigram.signup;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.SignupActivity;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.utils.SoftKeyboardHandledLinearLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignupFragment extends Fragment implements OnClickListener {
	Button btnMale, btnFemale, btnCreateAccount, btnBack, btnPrev, btnNext, btnDone;
	EditText etxtFirstName, etxtLastName, etxtBirthday, etxtEmail, etxtPassword;
	boolean isMale;
	RelativeLayout lyToolBar;
	SoftKeyboardHandledLinearLayout lyMainView;
	Date birthdayDate;
	View focusedView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_signup, null);
		
		btnMale          = (Button)v.findViewById(R.id.btnMaleSignup);
		btnFemale        = (Button)v.findViewById(R.id.btnFemaleSignup);
		btnCreateAccount = (Button)v.findViewById(R.id.btnCreateAccountSignup);
		btnBack          = (Button)v.findViewById(R.id.btnBackSignup);
		btnPrev          = (Button)v.findViewById(R.id.btnPrevSignup);
		btnNext          = (Button)v.findViewById(R.id.btnNextSignup);
		btnDone          = (Button)v.findViewById(R.id.btnDoneSignup);
		
		etxtFirstName   = (EditText)v.findViewById(R.id.etxtFirstNameSignup);
		etxtLastName    = (EditText)v.findViewById(R.id.etxtLastNameSignup);
		etxtBirthday    = (EditText)v.findViewById(R.id.etxtBirthdaySignup);
		etxtEmail       = (EditText)v.findViewById(R.id.etxtEmailSignup);
		etxtPassword    = (EditText)v.findViewById(R.id.etxtPasswordSignup);
		
		btnMale.setOnClickListener(this);
		btnFemale.setOnClickListener(this);
		btnCreateAccount.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		btnDone.setOnClickListener(this);

		etxtFirstName.setOnFocusChangeListener(focusListener);
		etxtLastName.setOnFocusChangeListener(focusListener);
		etxtEmail.setOnFocusChangeListener(focusListener);
		etxtPassword.setOnFocusChangeListener(focusListener);

		isMale = true;

		etxtBirthday.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new DatePickerDialog(getActivity(), date, myCalendar
						.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
						myCalendar.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		etxtBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					new DatePickerDialog(getActivity(), date, myCalendar
							.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
							myCalendar.get(Calendar.DAY_OF_MONTH)).show();
				}
			}
		});

		lyToolBar  = (RelativeLayout)v.findViewById(R.id.my_toolbar_signup);
		lyMainView = (SoftKeyboardHandledLinearLayout)v.findViewById(R.id.lyMainViewSignup);

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

		lyToolBar.setVisibility(View.GONE);

		return v;
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

	Calendar myCalendar = Calendar.getInstance();

	DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
							  int dayOfMonth) {
			// TODO Auto-generated method stub
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateLabel();
		}

	};

	private void updateLabel() {

		String myFormat = "MM/dd/yy"; //In which you need put here
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		etxtBirthday.setText(sdf.format(myCalendar.getTime()));

		birthdayDate = myCalendar.getTime();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btnMale) 		  onMaleClick(v);
		if(v == btnFemale)        onFemaleClick(v);
		if(v == btnCreateAccount) onCreateAccountClick(v);
		if(v == btnBack)          onBack(v);
		if(v == btnPrev)          onPrevClick(v);
		if(v == btnNext)          onNextClick(v);
		if(v == btnDone)          onCreateAccountClick(v);
	}

	public void onPrevClick(View v){
		if(focusedView == etxtLastName)
			etxtFirstName.requestFocus();
		else if(focusedView == etxtEmail)
			etxtLastName.requestFocus();
		else if(focusedView == etxtPassword)
			etxtEmail.requestFocus();
	}

	public void onNextClick(View v){
		if(focusedView == etxtFirstName)
			etxtLastName.requestFocus();
		else if(focusedView == etxtLastName)
			etxtEmail.requestFocus();
		else if(focusedView == etxtEmail)
			etxtPassword.requestFocus();
	}
	
	public void onMaleClick(View v){
		isMale = true;
		btnMale.setBackgroundResource(R.drawable.icon_check);
		btnFemale.setBackgroundResource(R.drawable.icon_check_unchecked);
	}
	
	public void onFemaleClick(View v){
		isMale = false;
		btnMale.setBackgroundResource(R.drawable.icon_check_unchecked);
		btnFemale.setBackgroundResource(R.drawable.icon_check);
	}
	
	public void onCreateAccountClick(View v){
		lyToolBar.setVisibility(View.GONE);

		String strFirstName = etxtFirstName.getText().toString();
		String strLastName = etxtLastName.getText().toString();
		String strBirthday = etxtBirthday.getText().toString();
		String strEmail = etxtEmail.getText().toString();
		String strPassword = etxtPassword.getText().toString();

		if(strFirstName.length() < 1){
			Toast.makeText(getActivity(), "Please enter your first name", Toast.LENGTH_LONG).show();
			return;
		}

		if(strLastName.length() < 1){
			Toast.makeText(getActivity(), "Please enter your last name", Toast.LENGTH_LONG).show();
			return;
		}

		if(strBirthday.length() < 1){
			Toast.makeText(getActivity(), "Please enter your birthday", Toast.LENGTH_LONG).show();
			return;
		}

		if(strEmail.length() < 1){
			Toast.makeText(getActivity(), "Please enter your email address", Toast.LENGTH_LONG).show();
			return;
		}

		if(strPassword.length() < 6){
			Toast.makeText(getActivity(), "password length should be at least 6 characters long", Toast.LENGTH_LONG).show();
			return;
		}

		final ParseUser pUser = new ParseUser();

		pUser.put(Constants.pKeyFirstName, strFirstName);
		pUser.put(Constants.pKeyLastName, strLastName);

		pUser.setUsername(strEmail);
		pUser.setEmail(strEmail);
		pUser.setPassword(strPassword);

		pUser.put(Constants.pKeyBirthday, birthdayDate);
		pUser.put(Constants.pKeyGender, Boolean.valueOf(isMale));

		pUser.put(Constants.pKeyNotifyComments, Boolean.valueOf(true));
		pUser.put(Constants.pKeyNotifyMessage, Boolean.valueOf(true));
		pUser.put(Constants.pKeyNotifyLike, Boolean.valueOf(false));
		pUser.put(Constants.pKeyNotifyFollow, Boolean.valueOf(true));
		pUser.put(Constants.pKeyNotifyFavorite, Boolean.valueOf(true));

		final ParseObject pObjFollow = new ParseObject(Constants.pClassFollow);

		pObjFollow.put(Constants.pKeyFollower, new ArrayList());
		pObjFollow.put(Constants.pKeyFollowing, new ArrayList());

		Commons.showProgressHUD(getActivity(), "Creating Account...");

		pObjFollow.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(e == null){
					pUser.put(Constants.pKeyFollowID, pObjFollow.getObjectId());

					pUser.signUpInBackground(new SignUpCallback() {
						@Override
						public void done(ParseException e) {
							Commons.dismissProgressHUD();

							if(e == null){
								
								SharedPreferences.Editor editor = Global.pref.edit();
								editor.putBoolean(Constants.prefLogged, true);
								editor.commit();

								Global.myInfo = new UserInfo(pUser);

                                Global.lastImageUpdateForNewsFeed = Calendar.getInstance().getTime();
                                Global.lastImageUpdateForRecipe = Calendar.getInstance().getTime();
                                Global.lastCommentUpdate = Calendar.getInstance().getTime();
                                Global.lastUserInfoUpdate = Calendar.getInstance().getTime();

                                Global.otherInfo = Global.myInfo;
                                Commons.getWallImagesForNewsFeed(SignupFragment.this, Constants.LIMIT_NUMBER_GRID);

                            }else{
                                Commons.dismissProgressHUD();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
						}
					});
				}else{
					Commons.dismissProgressHUD();
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

    public void didGetWallImageForNewsFeed(){
        Commons.getWallImagesForRecipe(SignupFragment.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForRecipe(){
        Commons.getWallImagesForFavorite(SignupFragment.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForFavorites(){
        Commons.getWallImagesForMyOwn(SignupFragment.this, Constants.LIMIT_NUMBER_GRID);
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

	public void onBack(View v){
		((SignupActivity)getActivity()).setupContainer(0);
	}
}
