package com.zhs1.Yummigram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.UserInfo;

import org.json.JSONObject;

import java.util.Calendar;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();

        boolean isLogged = Global.pref.getBoolean(Constants.prefLogged, false);
        ParseUser currentUser = ParseUser.getCurrentUser();

        if(isLogged && currentUser != null){
            Global.myInfo = new UserInfo(currentUser);

            Global.lastImageUpdateForNewsFeed = Calendar.getInstance().getTime();
            Global.lastImageUpdateForRecipe = Calendar.getInstance().getTime();
            Global.lastCommentUpdate = Calendar.getInstance().getTime();
            Global.lastUserInfoUpdate = Calendar.getInstance().getTime();

            Global.otherInfo = Global.myInfo;
            Commons.getWallImagesForNewsFeed(SplashActivity.this, Constants.LIMIT_NUMBER_GRID);
//            didGetWallImageForMyOwn();
        }else{
            WaitThread waitThread = new WaitThread();
            waitThread.execute("");
        }
    }

    public void didGetWallImageForNewsFeed(){
        Commons.getWallImagesForRecipe(SplashActivity.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForRecipe(){
        Commons.getWallImagesForFavorite(SplashActivity.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForFavorites(){
        Commons.getWallImagesForMyOwn(SplashActivity.this, Constants.LIMIT_NUMBER_GRID);
    }

    public void didGetWallImageForMyOwn(){
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put(Constants.pKeyUserObjId, Global.myInfo.strUserObjID);

        currentInstallation.saveInBackground();

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);

        startActivity(intent);
        finish();
    }



    class WaitThread extends AsyncTask<String, Void, JSONObject> {
		
		@Override
    	protected JSONObject doInBackground(String... urls) {

    		try {
    			Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	    return null;
    	}
		
		@Override
    	protected void onPostExecute(JSONObject result) {
			Intent intent = new Intent(SplashActivity.this, SignupActivity.class);
			
    		startActivity(intent);
    		finish();
		}
	}
	
	@Override
	public void onBackPressed() {	
		finish();
	}

	public void init(){
		Global.pref = getSharedPreferences(Constants.prefName, Context.MODE_PRIVATE);

		ImageLoaderConfiguration defaultConfiguration
				= new ImageLoaderConfiguration.Builder(this)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();

		ImageLoader.getInstance().init(defaultConfiguration);
	}
}
