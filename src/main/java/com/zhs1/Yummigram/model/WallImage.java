package com.zhs1.Yummigram.model;

import android.graphics.Bitmap;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.ArrayList;
import java.util.Date;

/*
  Created by I54460 on 6/29/2015.
 */
public class WallImage {
    public String strImgUrl;
    public String strImageObjId;
    public String strUserObjId;
    public String strUserFBId;
    public String strUserFullName;
    public String strRecipe;
    public String strIngredients;
    public String strDirections;
    public String strSelfComments;
    public Date createdDate;
    public String strCategory;
    public String strCity;
    public String strCountry;
    public boolean liked;
    public boolean favorited;
    public boolean commented;
    public int nNumberLikes;
    public int nNumberRecipeRequests;
    public Bitmap bmpWall = null;

    public ArrayList arrComments;
    public ArrayList<String> arrTag;

    public WallImage(ParseObject pObj){
        super();

        this.strImgUrl = pObj.getString(Constants.pKeyImage);
        this.strImageObjId = pObj.getObjectId();
        this.strUserObjId = pObj.getString(Constants.pKeyUserObjId);
        this.strUserFBId = pObj.getString(Constants.pKeyUserFBId);
        this.strUserFullName = pObj.getString(Constants.pKeyUserFullName);
        this.strRecipe = Commons.validString(pObj.getString(Constants.pKeyRecipe));
        this.strIngredients = Commons.validString(pObj.getString(Constants.pKeyIngredients));
        this.strDirections = Commons.validString(pObj.getString(Constants.pKeyDirections));
        this.strSelfComments = Commons.validString(pObj.getString(Constants.pKeySelfComment));
        this.createdDate = pObj.getCreatedAt();
        this.strCategory = Commons.validString(pObj.getString(Constants.pKeyCategory));
        this.strCity = Commons.validString(pObj.getString(Constants.pKeyCity));
        this.strCountry = Commons.validString(pObj.getString(Constants.pKeyCountry));
        this.arrComments = pObj.get(Constants.pKeyComments) == null ? new ArrayList(): (ArrayList)pObj.get(Constants.pKeyComments);
        this.arrTag = pObj.get(Constants.pKeyTag) == null ? new ArrayList<String>(): (ArrayList<String>)pObj.get(Constants.pKeyTag);

        this.nNumberRecipeRequests = pObj.getNumber(Constants.pKeyRequestRecipe) == null ? 0: pObj.getNumber(Constants.pKeyRequestRecipe).intValue();

        ArrayList arrLiked = pObj.get(Constants.pKeyLikes) == null ? new ArrayList(): (ArrayList)pObj.get(Constants.pKeyLikes);

        this.liked = arrLiked.contains(Global.myInfo.strUserObjID);
        this.favorited = Global.myInfo.arrFavorites.contains(this.strImageObjId);
        this.nNumberLikes = arrLiked.size();

        ParseQuery<ParseObject> queryComments = ParseQuery.getQuery(Constants.pClassWallImageComments);

        queryComments.whereEqualTo(Constants.pKeyImageObjId, this.strImageObjId);
        queryComments.whereEqualTo(Constants.pKeyUserObjId,  Global.myInfo.strUserObjID);

        int nCount = 0;

        try {
            nCount = queryComments.count();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.commented = (nCount > 0);
    }

}
