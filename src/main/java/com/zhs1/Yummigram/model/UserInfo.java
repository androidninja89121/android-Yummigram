package com.zhs1.Yummigram.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.ContactsContract;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
  Created by I54460 on 6/27/2015.
 */
public class UserInfo {
    public String          strUserFirstName;
    public String          strUserLastName;
    public String          strUserEmail;
    public String          strFacebookID;
    public String          strFacebookToken;
    public String          strUserObjID;
    public boolean         isMale;
    public Date            birthDay;
    public Bitmap          bmpPhoto;
    public ArrayList<String>       arrFollowing;
    public ArrayList<String>       arrFollower;
    public ArrayList<String>       arrFavorites;
    public ArrayList<String>       arrWallImages;
    public String          strFollowObjID;
    public boolean         canNotifyComment;
    public boolean         canNotifyLike;
    public boolean         canNotifyFavorite;
    public boolean         canNotifyMessage;
    public boolean         canNotifyFollow;

    public UserInfo(ParseUser objUser){
        super();

        this.strUserFirstName = Commons.validString(objUser.getString(Constants.pKeyFirstName));
        this.strUserLastName = Commons.validString(objUser.getString(Constants.pKeyLastName));
        this.strUserEmail = Commons.validString(objUser.getEmail());
        this.strFacebookID = Commons.validString(objUser.getString(Constants.pKeyFBID));
        this.strFacebookToken = Commons.validString(objUser.getString(Constants.pKeyFBToken));
        this.strUserObjID = objUser.getObjectId();
        this.isMale = objUser.getBoolean(Constants.pKeyGender);
        this.birthDay = objUser.get(Constants.pKeyBirthday) == null ? Calendar.getInstance().getTime(): (Date)objUser.get(Constants.pKeyBirthday);

        this.bmpPhoto = null;
        if(objUser.get(Constants.pKeyPhoto) != null){

            try {
                byte[] data = ((ParseFile)objUser.get(Constants.pKeyPhoto)).getData();
                this.bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        this.arrFavorites = objUser.get(Constants.pKeyFavorites) == null ? new ArrayList<String>(): (ArrayList<String>)objUser.get(Constants.pKeyFavorites);
        this.arrWallImages = objUser.get(Constants.pKeyWallImages) == null ? new ArrayList<String>(): (ArrayList<String>)objUser.get(Constants.pKeyWallImages);

        this.strFollowObjID = objUser.getString(Constants.pKeyFollowID);

        ParseObject pObj = Commons.getFollowPFObjectFrom(this.strFollowObjID);

        this.arrFollowing = pObj.get(Constants.pKeyFollowing) == null ? new ArrayList<String>(): (ArrayList<String>)pObj.get(Constants.pKeyFollowing);
        this.arrFollower = pObj.get(Constants.pKeyFollower) == null ? new ArrayList<String>(): (ArrayList<String>)pObj.get(Constants.pKeyFollower);
    }
}
