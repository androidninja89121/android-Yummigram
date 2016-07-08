package com.zhs1.Yummigram.model;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by I54460 on 6/29/2015.
 */
public class DataStore {
    static DataStore instance = null;

    public ArrayList<WallImage> wallImagesForNewsFeed;
    public ArrayList<WallImage> wallImagesForRecipe;
    public ArrayList<WallImage> wallImagesForFavorites;
    public ArrayList<WallImage> wallImagesForMyOwn;
    public ArrayList<NotifyPost> notifyPosts;
    public HashMap<String, ParseObject>   userNotifyPostPFObjectMap;
    public HashMap<String, WallImage>   wallImageMap;
    public HashMap<String, ParseObject> wallImagePFObjectMap;
    public HashMap<String, UserInfo>   userInfoMap;
    public HashMap<String, ParseUser>   userInfoPFObjectMap;
    public ArrayList<WallImageComment> comments;
    public ArrayList<TotalMsg> totalMsg;
    public HashMap<String, ParseObject>   totalMsgPFObjectMap;
    public ArrayList<DetailMsg> detailMsg;
    public HashMap<String, ParseObject>   followPFObjectMap;
    public ArrayList<WallImage> wallImagesForCategory;
    public ArrayList<WallImage> wallImagesForTag;
    public ArrayList<WallImage> wallImagesForSearch;

    public static DataStore getInstance() {
        if(instance == null){
            instance = new DataStore();
        }
        return instance;
    }

    public DataStore() {
        super();

        wallImagesForNewsFeed = new ArrayList<WallImage>();
        wallImagesForRecipe  = new ArrayList<WallImage>();
        wallImagesForFavorites = new ArrayList<WallImage>();
        wallImagesForMyOwn   = new ArrayList<WallImage>();
        notifyPosts          = new ArrayList<NotifyPost>();
        wallImageMap         = new HashMap<String, WallImage>();
        wallImagePFObjectMap = new HashMap<String, ParseObject>();
        userInfoMap          = new HashMap<String, UserInfo>();
        userInfoPFObjectMap  = new HashMap<String, ParseUser>();
        comments = new ArrayList<WallImageComment>();
        userNotifyPostPFObjectMap = new HashMap<String, ParseObject>();
        totalMsg = new ArrayList<TotalMsg>();
        totalMsgPFObjectMap = new HashMap<String, ParseObject>();
        detailMsg = new ArrayList<DetailMsg>();
        followPFObjectMap = new HashMap<String, ParseObject>();
        wallImagesForCategory = new ArrayList<WallImage>();
        wallImagesForTag = new ArrayList<WallImage>();
        wallImagesForSearch = new ArrayList<WallImage>();
    }

    public void reset(){
        wallImagesForNewsFeed.clear();
        wallImagesForRecipe.clear();
        wallImagesForFavorites.clear();
        wallImagesForMyOwn.clear();
        notifyPosts.clear();
        wallImageMap.clear();
        wallImagePFObjectMap.clear();
        userInfoMap.clear();
        userInfoPFObjectMap.clear();
        comments.clear();
        userNotifyPostPFObjectMap.clear();
        totalMsg.clear();
        totalMsgPFObjectMap.clear();
        detailMsg.clear();
        followPFObjectMap.clear();
        wallImagesForCategory.clear();
        wallImagesForTag.clear();
        wallImagesForSearch.clear();
    }
}
