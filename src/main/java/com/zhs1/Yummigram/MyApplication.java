package com.zhs1.Yummigram;

import android.support.multidex.MultiDexApplication;

import com.aviary.android.feather.sdk.IAviaryClientCredentials;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class MyApplication extends MultiDexApplication implements IAviaryClientCredentials {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here

        Parse.initialize(this, "m4qAyLcZpdsTgzFb5GsXn0uGq2wqh0pF1j0VbDnw", "yNRWV4629dmXvVNjj0Ov0zyu7PuifK7tM9IdGo3q");
        ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);
    }

    @Override
    public String getBillingKey() {
        return "";
    }

    @Override
    public String getClientID() {
        return "9adff13b78a94ade805787113d419a41";
    }

    @Override
    public String getClientSecret() {
        return "3b5447a0-3eca-4206-adda-842c05a33270";
    }
}
