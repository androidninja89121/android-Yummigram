package com.zhs1.Yummigram;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.main.FavoriteFragment;
import com.zhs1.Yummigram.main.NewsFeedFragment;
import com.zhs1.Yummigram.main.ProfileFragment;
import com.zhs1.Yummigram.main.RecipeFragment;
import com.zhs1.Yummigram.main.SearchViewFragment;
import com.zhs1.Yummigram.main.SettingFragment;
import com.zhs1.Yummigram.main.TagViewFragment;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.other.NotifyActivity;
import com.zhs1.Yummigram.other.TotalChatActivity;
import com.zhs1.Yummigram.takephoto.TakePhotoActivity;
import com.zhs1.Yummigram.utils.CustomActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends CustomActivity implements OnClickListener, ActionSheet.ActionSheetListener {
	LinearLayout lyTab;
	RelativeLayout rlyTabNewsFeed, rlyTabRecipe, rlyTabCamera, rlyTabFavorite, rlyTabProfile;
	RelativeLayout rlySearchBarGroup, rlyProfileGroup, rlySettingGroup, rlyOtherGroup;
	SearchView searchView;
	Button btnChat, btnNotify, btnBackOther, btnSetting, btnLogout, btnBackSetting;
	TextView tvTitleOther;
	String strCurFragTag = Constants.fragNewsFeed, strPrevFragTag, strSearch;
	public int nPosForProfile;
    boolean isClickedTakingPhoto;

	NewsFeedFragment fragmentNewsFeed = null;
	public RecipeFragment   fragmentRecipe = null;
	public FavoriteFragment fragmentFavorite = null;
	ProfileFragment  fragmentProfile = null;

    LocationManager locationManager = null;
    LocationListener locationListener = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		lyTab = (LinearLayout)findViewById(R.id.lyTab);
		searchView = (SearchView)findViewById(R.id.searchView);
		
		rlySearchBarGroup = (RelativeLayout)findViewById(R.id.rlySearchBarGroup);
		rlyProfileGroup = (RelativeLayout)findViewById(R.id.rlyProfileGroup);
		rlySettingGroup = (RelativeLayout)findViewById(R.id.rlySettingGroup);
		rlyOtherGroup = (RelativeLayout)findViewById(R.id.rlyOtherGroup);
		
		rlyTabNewsFeed = (RelativeLayout)findViewById(R.id.rlyTabNewsFeed);
		rlyTabRecipe = (RelativeLayout)findViewById(R.id.rlyTabRecipe);
		rlyTabCamera = (RelativeLayout)findViewById(R.id.rlyTabCamera);
		rlyTabFavorite = (RelativeLayout)findViewById(R.id.rlyTabFavorite);
		rlyTabProfile = (RelativeLayout)findViewById(R.id.rlyTabProfile);
		
		btnChat   = (Button)findViewById(R.id.btnChat);
		btnNotify = (Button)findViewById(R.id.btnNotify);
		btnSetting = (Button)findViewById(R.id.btnSetting);
		btnLogout = (Button)findViewById(R.id.btnLogout);
		btnBackSetting = (Button)findViewById(R.id.btnBackSetting);
        btnBackOther = (Button)rlyOtherGroup.findViewById(R.id.btnBackOther);

		btnChat.setOnClickListener(this);
		btnNotify.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
		btnBackSetting.setOnClickListener(this);
		btnBackOther.setOnClickListener(this);

		rlyTabNewsFeed.setOnClickListener(this);
		rlyTabRecipe.setOnClickListener(this);
		rlyTabCamera.setOnClickListener(this);
		rlyTabFavorite.setOnClickListener(this);
		rlyTabProfile.setOnClickListener(this);
		
		searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                strSearch = query;

                searchView.setQuery("", false);
                searchView.clearFocus();

                setupContainer(4);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

		int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
		View searchPlate = searchView.findViewById(searchPlateId);
		if (searchPlate!=null) {
			searchPlate.setBackgroundColor (Color.TRANSPARENT);
			int searchTextId = searchPlate.getContext ().getResources ().getIdentifier ("android:id/search_src_text", null, null);
		}

//		int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
//		ImageView magImage = (ImageView) searchView.findViewById(magId);
//		magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

		tvTitleOther = (TextView)rlyOtherGroup.findViewById(R.id.tvTitleOther);

		setupContainer(0);
        initGPS();
	}

    public void initGPS(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

	public void onActionSheet(boolean isNewRecipe){
        String strRecipeTitle = "Edit Recipe";

        if(isNewRecipe) strRecipeTitle = "Add Recipe";

		ActionSheet.createBuilder(this, getSupportFragmentManager())
				.setCancelButtonTitle("Cancel")
				.setOtherButtonTitles("Edit Comments", strRecipeTitle, "Delete")
				.setCancelableOnTouchOutside(true)
				.setListener(this).show();
	}

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {

    /*----------to get City-Name from coordinates ------------- */
            String cityName=null;
            String countryName = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());

                cityName=addresses.get(0).getLocality();
                countryName = addresses.get(0).getCountryName();

                Global.strCity = cityName;
                Global.strCountry = countryName;

                SharedPreferences.Editor editor = Global.pref.edit();

                editor.putString(Constants.pKeyCity, cityName);
                editor.putString(Constants.pKeyCountry, countryName);

                editor.commit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }


        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == rlyTabNewsFeed){
//			fragmentNewsFeed.scrollToTop();
			setupContainer(0);
		}
		
		if(v == rlyTabRecipe){
//			fragmentRecipe.scrollToTop();
			setupContainer(1);
		}
		
		if(v == rlyTabCamera){
            isClickedTakingPhoto = true;

			Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
			startActivity(intent);
		}
		
		if(v == rlyTabFavorite){
//			fragmentFavorite.scrollToTop();
			setupContainer(2);
		}
		
		if(v == rlyTabProfile){
//			fragmentProfile.scrollToTop();
			setupContainer(3);
		}
		
		if(v == btnChat){
            setCheckedMessage();
			startActivity(new Intent(MainActivity.this, TotalChatActivity.class));
		}
		
		if(v == btnNotify){
            setCheckedNotify();
			startActivity(new Intent(MainActivity.this, NotifyActivity.class));
		}

		if(v == btnSetting){
			setupContainer(6);
		}

		if(v == btnLogout){
			ParseUser.logOutInBackground(new LogOutCallback() {
				@Override
				public void done(ParseException e) {
                    SharedPreferences.Editor editor = Global.pref.edit();
                    editor.putBoolean(Constants.prefLogged, false);
                    editor.commit();

                    DataStore.getInstance().reset();

					startActivity(new Intent(MainActivity.this, SignupActivity.class));
					finish();
				}
			});
		}

		if(v == btnBackSetting){
			setupContainer(3);
		}

        if(v == btnBackOther){
            if(strCurFragTag.equals(Constants.fragNewsFeed)){
                setupContainer(0);
            }else if(strCurFragTag.equals(Constants.fragRecipe)){
                setupContainer(1);
            }else if(strCurFragTag.equals(Constants.fragFavorite)){
                setupContainer(2);
            }
        }
	}
	
	public void initTitleBar(){
		rlySearchBarGroup.setVisibility(View.INVISIBLE);
		rlyProfileGroup.setVisibility(View.INVISIBLE);
		rlySettingGroup.setVisibility(View.INVISIBLE);
		rlyOtherGroup.setVisibility(View.INVISIBLE);
	}
	
	public void setupContainer(int nIdx)
	{
		initTitleBar();

		strPrevFragTag = strCurFragTag;
		
		switch (nIdx) {
			case 0:
                strCurFragTag = Constants.fragNewsFeed;

				fragmentNewsFeed = null;
				fragmentNewsFeed = new NewsFeedFragment();
				getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentNewsFeed, Constants.fragNewsFeed).commit();

				lyTab.setBackgroundResource(R.drawable.tabbar_news);
				rlySearchBarGroup.setVisibility(View.VISIBLE);
				break;

			case 1:
                strCurFragTag = Constants.fragRecipe;

				fragmentRecipe = null;
				fragmentRecipe = new RecipeFragment();
				getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentRecipe, Constants.fragRecipe).commit();

				lyTab.setBackgroundResource(R.drawable.tabbar_recipes);
				rlySearchBarGroup.setVisibility(View.VISIBLE);
				break;

			case 2:
                strCurFragTag = Constants.fragFavorite;

				fragmentFavorite = null;
				fragmentFavorite = new FavoriteFragment();
				getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentFavorite, Constants.fragFavorite).commit();

				lyTab.setBackgroundResource(R.drawable.tabbar_favorites);
				rlySearchBarGroup.setVisibility(View.VISIBLE);
				break;

			case 3:
                strCurFragTag = Constants.fragProfile;

				fragmentProfile = null;
				fragmentProfile = new ProfileFragment();
				getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentProfile, Constants.fragProfile).commit();

				lyTab.setBackgroundResource(R.drawable.tabbar_account);
				rlyProfileGroup.setVisibility(View.VISIBLE);
				break;

			case 4:

                SearchViewFragment searchViewFragment = new SearchViewFragment();

                Bundle args = new Bundle();
                args.putString(Constants.eKeySearchKey, strSearch);
                args.putString(Constants.eKeyMode, strCurFragTag);

                searchViewFragment.setArguments(args);

				getFragmentManager().beginTransaction().replace(R.id.content_frame, searchViewFragment, Constants.fragSearchView).commit();
				rlyOtherGroup.setVisibility(View.VISIBLE);
				tvTitleOther.setText("Search:" + strSearch);

				break;

			case 5:
                TagViewFragment tagViewFragment = new TagViewFragment();

                Bundle args1 = new Bundle();
                args1.putString(Constants.eKeyTagKey, Global.strTag);

                tagViewFragment.setArguments(args1);

				getFragmentManager().beginTransaction().replace(R.id.content_frame, tagViewFragment, Constants.fragTagView).commit();
				rlyOtherGroup.setVisibility(View.VISIBLE);
				tvTitleOther.setText("Tag:" + Global.strTag);
				break;

			case 6:
                strCurFragTag = Constants.fragSetting;

				getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingFragment(), Constants.fragSetting).commit();
				rlySettingGroup.setVisibility(View.VISIBLE);
                break;
		}
	}



	@Override
	public void onDismiss(ActionSheet actionSheet, boolean b) {

	}

	@Override
	public void onOtherButtonClick(ActionSheet actionSheet, int i) {
		switch (i){
			case 0:
				fragmentProfile.onEditComments(nPosForProfile);
				break;
			case 1:
				fragmentProfile.onAddRecipe(nPosForProfile);
				break;
			case 2:
				fragmentProfile.onDelete(nPosForProfile);
				break;
			default:
		}
	}

    public void setNewNotify(){
        btnNotify.setBackgroundResource(R.drawable.ic_alarm);
    }

    public void setCheckedNotify(){
        btnNotify.setBackgroundResource(R.drawable.ic_alarm_default);
    }

    public void setNewMessage(){
        btnChat.setBackgroundResource(R.drawable.ic_chat);
    }

    public void setCheckedMessage(){
        btnChat.setBackgroundResource(R.drawable.ic_chat_default);
    }

	@Override
	protected void onResume() {
		super.onResume();
        if(isClickedTakingPhoto){
            setupContainer(0);
            fragmentNewsFeed.loadMore();
        }

        Global.mainActivity = MainActivity.this;
        isClickedTakingPhoto = false;
	}

    @Override
    protected void onPause() {
        super.onPause();
        Global.mainActivity = null;
    }

}
