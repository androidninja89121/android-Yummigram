package com.zhs1.Yummigram.global;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.other.DetailChatActivity;
import com.zhs1.Yummigram.other.NotifyActivity;
import com.zhs1.Yummigram.other.TotalChatActivity;
import com.zhs1.Yummigram.utils.ProgressHUD;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by I54460 on 6/25/2015.
 */
public class Global {


    public static Uri                           uriSharePhoto;
    public static UserInfo                      myInfo;
    public static UserInfo                      otherInfo;
    public static WallImage                     wallImage;
    public static Date                          lastImageUpdateForNewsFeed;
    public static Date                          lastImageUpdateForRecipe;
    public static Date                          lastImageUpdateForCategory;
    public static Date                          lastImageUpdateForTag;
    public static Date                          lastImageUpdateForSearch;
    public static Date                          lastCommentUpdate;
    public static Date                          lastUserInfoUpdate;
    public static Date                          lastTotalMsgUpdate;
    public static Date                          lastDetailMsgUpdate;
    public static String                        strCountry = "";
    public static String                        strCity = "";
    public static String                        strTag;

    public static String                        strCategory = "All Recipes";

    public static SharedPreferences             pref;
    public static ProgressHUD                   progressHUD;

    public static NotifyActivity                notifyActivity = null;
    public static TotalChatActivity             totalChatActivity = null;
    public static DetailChatActivity            detailChatActivity = null;
    public static MainActivity                  mainActivity = null;

}
