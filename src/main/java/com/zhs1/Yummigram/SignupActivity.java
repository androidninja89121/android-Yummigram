package com.zhs1.Yummigram;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;

import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.signup.LoginFragment;
import com.zhs1.Yummigram.signup.SignupFragment;
import com.zhs1.Yummigram.signup.WelcomeFragment;
import com.zhs1.Yummigram.utils.CustomActivity;
import com.zhs1.Yummigram.utils.ProgressHUD;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignupActivity extends CustomActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_signup);
		
		setupContainer(0);
        showHashKey(this);
	}

    public static void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.zhs1.Yummigram", PackageManager.GET_SIGNATURES); //Your package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.v("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
	
	public void setupContainer(int nIdx)
	{
		switch (nIdx) {
		case 0:
			getFragmentManager().beginTransaction().replace(R.id.content_frame, new WelcomeFragment()).commit();
			break;
		case 1:
			getFragmentManager().beginTransaction().replace(R.id.content_frame, new LoginFragment()).commit();
			break;
		case 2:
			getFragmentManager().beginTransaction().replace(R.id.content_frame, new SignupFragment()).commit();
			break;
		}
	}
	
	public void gotoMain()
	{
		Intent intent = new Intent(SignupActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
