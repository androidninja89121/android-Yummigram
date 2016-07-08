package com.zhs1.Yummigram.global;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.entity.mime.HttpMultipartMode;
import com.parse.entity.mime.MultipartEntity;
import com.parse.entity.mime.content.ByteArrayBody;
import com.parse.entity.mime.content.StringBody;
import com.zhs1.Yummigram.MainActivity;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.DetailMsg;
import com.zhs1.Yummigram.model.NotifyPost;
import com.zhs1.Yummigram.model.TotalMsg;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.model.WallImage;
import com.zhs1.Yummigram.model.WallImageComment;
import com.zhs1.Yummigram.other.CommentActivity;
import com.zhs1.Yummigram.other.ProfileActivity;
import com.zhs1.Yummigram.other.RecipeActivity;
import com.zhs1.Yummigram.signup.LoginFragment;
import com.zhs1.Yummigram.utils.ProgressHUD;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by I54460 on 6/29/2015.
 */
public class Commons {

    public static enum  NotifyType {
        notifyFollowing,
        notifyLiked,
        notifyComment,
        notifyAddFavorite,
        notifyRequestRecipe
    }

    public static enum ViewMode {
        viewPhoto,
        viewFollower,
        viewFollowing,
        viewEveryone
    }

    public static enum Category{
        cateRecipe,
        cateFavorite,
        cateProfile
    }

    public static void showProgressHUD(Context context, String message){
        if(Global.progressHUD == null){
            Global.progressHUD = ProgressHUD.show(context, message, true,false,null);
        }
    }

    public static void dismissProgressHUD(){
        if(Global.progressHUD != null){
            Global.progressHUD.dismiss();
            Global.progressHUD = null;
        }
    }

    public static String getKeyString(String strObjId1, String strObjId2){
        String strKey = "";

        if(strObjId1.compareTo(strObjId2) < 0)
            strKey = strObjId1 + "-" + strObjId2;
        else
            strKey = strObjId2 + "-" + strObjId1;

        return strKey;
    }

    public static ArrayList<String> getTagsFromComment(String strComment){
        ArrayList<String> arrAns = new ArrayList<String>();

        String[] arrTmp = strComment.split("#");

        for(int idx  = 0; idx < arrTmp.length; idx ++){
            if(idx == 0) continue;

            String strSnippet = arrTmp[idx];

            String[] arrTmpOther = strSnippet.split(" ");
            String strElement = arrTmpOther[0];

            if(strElement.length() == 1) continue;

            strElement = "#" + strElement.toLowerCase();

            arrAns.add(strElement);
        }

        return arrAns;
    }


    public static class CreatedDateComparator implements Comparator<WallImage>
    {
        public int compare(WallImage left, WallImage right) {
            return right.createdDate.compareTo(left.createdDate);
        }
    }

    public static String validString(String strVal){
        if(strVal == null) return "";
        return strVal;
    }

    public static ParseObject getFollowPFObjectFrom(String strFollowObjID){
        ParseObject pObj = DataStore.getInstance().followPFObjectMap.get(strFollowObjID);

        if(pObj == null){
            ParseQuery<ParseObject> queryFollow = ParseQuery.getQuery(Constants.pClassFollow);

            try {
                pObj = queryFollow.get(strFollowObjID);
                DataStore.getInstance().followPFObjectMap.put(strFollowObjID, pObj);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return pObj;
    }

    public static WallImage getImageDataFrom(ParseObject wallImageObj){
        WallImage wallImageTmp = DataStore.getInstance().wallImageMap.get(wallImageObj.getObjectId());

        if(wallImageTmp == null){
            wallImageTmp = new WallImage(wallImageObj);

            DataStore.getInstance().wallImagePFObjectMap.put(wallImageTmp.strImageObjId, wallImageObj);
            DataStore.getInstance().wallImageMap.put(wallImageTmp.strImageObjId, wallImageTmp);
        }

        return wallImageTmp;
    }

    public static WallImage getImageDataWith(String strImageObjID){
        WallImage wallImageTmp = DataStore.getInstance().wallImageMap.get(strImageObjID);

        if(wallImageTmp == null){
            ParseQuery<ParseObject> queryWallImage = ParseQuery.getQuery(Constants.pClassWallImageOther);

            queryWallImage.whereEqualTo(Constants.pKeyObjId, strImageObjID);

            try {
                ParseObject pWallImage = queryWallImage.getFirst();

                wallImageTmp = Commons.getImageDataFrom(pWallImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return wallImageTmp;
    }

    public static UserInfo getUserInfoFrom(String strUserObjId){
        UserInfo userInfo = DataStore.getInstance().userInfoMap.get(strUserObjId);

        if(userInfo == null){
            ParseQuery<ParseUser> queryUser = ParseUser.getQuery();

            queryUser.whereEqualTo(Constants.pKeyObjId, strUserObjId);

            try {
                ParseUser pUser = queryUser.getFirst();

                userInfo = new UserInfo(pUser);

                DataStore.getInstance().userInfoMap.put(userInfo.strUserObjID, userInfo);
                DataStore.getInstance().userInfoPFObjectMap.put(userInfo.strUserObjID, pUser);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return userInfo;
    }

    public static String getTime(Date time){
        if (time != null) {
            long diff = Calendar.getInstance().getTime().getTime() - time.getTime();
            long distanceBetweenDates = diff / 1000;

            if (distanceBetweenDates < 60) {
                long secondsInAnHour = 1;
                long second = distanceBetweenDates / secondsInAnHour;
                return second + "s ago";
            } else {
                if (distanceBetweenDates < 3600) {
                    long secondsInAnHour = 60;
                    long minutes = distanceBetweenDates / secondsInAnHour;
                    return minutes + "m ago";
                } else {
                    if (distanceBetweenDates < 3600*24) {
                        long secondsInAnHour = 3600;
                        long hours = distanceBetweenDates / secondsInAnHour;
                        return hours + "h ago";
                    } else {
                        long secondsInAnHour = 3600*24;
                        long hoursBetweenDates = distanceBetweenDates / secondsInAnHour;
                        if (hoursBetweenDates > 365) {
                            long year = hoursBetweenDates/365;
                            return year + "y ago";
                        } else{
                            return hoursBetweenDates + "d ago";
                        }
                    }
                }
            }
        }

        return "";
    }

    public static void postNotifyWithImage(final WallImage wallImage, final NotifyType notifyType){
        if(wallImage.strUserObjId.equals(Global.myInfo.strUserObjID)) return;

        UserInfo otherUserInfo = Commons.getUserInfoFrom(wallImage.strUserObjId);

        boolean isNotifyComment  = otherUserInfo.canNotifyComment;
        boolean isNotifyLike     = otherUserInfo.canNotifyLike;
        boolean isNotifyFavorite = otherUserInfo.canNotifyFavorite;

        if(!isNotifyComment && notifyType == NotifyType.notifyComment) return;
        if(!isNotifyLike && notifyType == NotifyType.notifyLiked) return;
        if(!isNotifyFavorite && notifyType == NotifyType.notifyAddFavorite) return;

        if(notifyType == NotifyType.notifyLiked || notifyType == NotifyType.notifyAddFavorite){
            ParseQuery<ParseObject> queryNotify = ParseQuery.getQuery(Constants.pClassNotifyPost);

            queryNotify.whereEqualTo(Constants.pKeyImageObjId, wallImage.strImageObjId);
            queryNotify.whereEqualTo(Constants.pKeyOtherUserObjId, Global.myInfo.strUserObjID);
            queryNotify.whereEqualTo(Constants.pKeyNotifyType, notifyType.ordinal());

            try {
                int nCount = queryNotify.count();

                if(nCount > 0) return;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        final ParseObject notifyObject = ParseObject.create(Constants.pClassNotifyPost);

        notifyObject.put(Constants.pKeyUserObjId, wallImage.strUserObjId);
        notifyObject.put(Constants.pKeyNotifyType, notifyType.ordinal());
        notifyObject.put(Constants.pKeyOtherUserObjId, Global.myInfo.strUserObjID);
        notifyObject.put(Constants.pKeyImageObjId, wallImage.strImageObjId);

        notifyObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    ParseQuery query = ParseInstallation.getQuery();

                    query.whereEqualTo(Constants.pKeyUserObjId, wallImage.strUserObjId);

                    String strFullName = Global.myInfo.strUserFirstName + " " + Global.myInfo.strUserLastName;
                    String strAlert = "";

                    if(notifyType == NotifyType.notifyLiked){
                        strAlert = strFullName + " liked your post";
                    }else if(notifyType == NotifyType.notifyAddFavorite){
                        strAlert = strFullName + " added your post as a favorite";
                    }else if(notifyType == NotifyType.notifyComment){
                        strAlert = strFullName + " commented your post";
                    }else if(notifyType == NotifyType.notifyRequestRecipe){
                        strAlert = strFullName + " requested the recipe of your post";
                    }

                    JSONObject data = new JSONObject();

                    try {
                        data.put(Constants.pnMode, Constants.PN_NOTIFY);
                        data.put(Constants.pnBadge, Constants.PN_INCREMENT);
                        data.put(Constants.pnAlert, strAlert);
                        data.put(Constants.pnNotifyType, notifyType.ordinal());
                        data.put(Constants.pnImageId, wallImage.strImageObjId);
                        data.put(Constants.pnUserObjId, Global.myInfo.strUserObjID);
                        data.put(Constants.pnSound, "Notify_1.wav");

                        ParsePush push = new ParsePush();

                        push.setQuery(query);
                        push.setData(data);

                        push.sendInBackground();

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }else{
                    Log.d("notifyObject", e.getLocalizedMessage());
                }
            }
        });
    }

    public static void onOtherProfile(Context context,String strUserObjId){
        Intent myIntent = new Intent(context, ProfileActivity.class);

        myIntent.putExtra(Constants.eKeyUserObjId, strUserObjId);

        context.startActivity(myIntent);
    }

    public static void onRecipeView(Context context, WallImage wallImage){
        Intent myIntent = new Intent(context, RecipeActivity.class);

        myIntent.putExtra(Constants.eKeyImageObjId, wallImage.strImageObjId);

        context.startActivity(myIntent);
    }

    public static void onCommentView(Context context, WallImage wallImage){
        Intent myIntent = new Intent(context, CommentActivity.class);

        myIntent.putExtra(Constants.eKeyImageObjId, wallImage.strImageObjId);

        context.startActivity(myIntent);
    }

    public static void onTagSelected(Context context, String strTag){
        Global.strTag = strTag;
        ((MainActivity) context).setupContainer(5);
    }

    public static void postImageToFB(byte[] imgData, String strComments)
    {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httppost = new HttpPost("https://graph.facebook.com/me/photos?access_token=" + Global.myInfo.strFacebookToken);
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("source", new ByteArrayBody(imgData, "post.jpg"));
            entity.addPart("message", new StringBody(strComments));
            httppost.setEntity(entity);

            HttpResponse response = httpclient.execute(httppost, localContext);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public static void onShareFacebook(final Context context, final WallImage wallImage){
        String strComments = "";

        if(wallImage.strRecipe.length() > 0){
            strComments = wallImage.strRecipe + " RECIPE -> www.yummigram.com/recipeid\n" + wallImage.strSelfComments;
        }else{
            strComments = wallImage.strSelfComments + "\nShared via Yummigram -> www.yummigram.com";
        }

        if(wallImage.bmpWall == null){
            final String finalStrComments = strComments;
            ImageLoader.getInstance().loadImage(wallImage.strImgUrl, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    wallImage.bmpWall = bitmap;

                    Uri imageUri = Commons.getImageUri(context, wallImage.bmpWall);

                    Intent shareIntent = new Intent();

                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, finalStrComments);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, finalStrComments);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    context.startActivity(Intent.createChooser(shareIntent, "send"));
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            Uri imageUri = Commons.getImageUri(context, wallImage.bmpWall);

            Intent shareIntent = new Intent();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, strComments);
            shareIntent.putExtra(Intent.EXTRA_TEXT, strComments);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share"));
        }




    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static void getWallImagesForNewsFeed(final Object delegate, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForNewsFeed);
        imageQuery.setSkip(DataStore.getInstance().wallImagesForNewsFeed.size());
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage: list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForNewsFeed.add(wallImage);
                    }
                }else{
                    Log.d("ImagesForNewsFeed", e.getLocalizedMessage());
                }

                Method method = null;
                Class objectedClass = delegate.getClass();

                try {

                    method = objectedClass.getMethod("didGetWallImageForNewsFeed", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getWallImagesForRecipe(final Object delegate,  int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForRecipe);
        imageQuery.whereEqualTo(Constants.pKeyIsRecipe, 1);
        imageQuery.setSkip(DataStore.getInstance().wallImagesForRecipe.size());
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage: list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForRecipe.add(wallImage);
                    }
                }else{
                    Log.d("getWallImagesForRecipe", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForRecipe", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getWallImagesForFavorite(final Object delegate, int nLimit){
        ArrayList<String> arrTmp = new ArrayList<String>();

        for(String strObjId : Global.myInfo.arrFavorites){
            WallImage wallImage = DataStore.getInstance().wallImageMap.get(strObjId);

            if(wallImage != null) continue;

            arrTmp.add(strObjId);
        }

        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereContainedIn(Constants.pKeyObjId, arrTmp);
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForFavorites.add(wallImage);
                    }
                }else{
                    Log.d("ImagesForFavorite", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForFavorites", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static  void getWallImagesForMyOwn(final Object delegate, int nLimit){
        ArrayList<String> arrTmp = new ArrayList<String>();

        for(String strObjId : Global.otherInfo.arrWallImages){
            WallImage wallImage = DataStore.getInstance().wallImageMap.get(strObjId);

            if(wallImage != null) continue;

            arrTmp.add(strObjId);
        }

        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereContainedIn(Constants.pKeyObjId, arrTmp);
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        Commons.getImageDataFrom(pObjWallImage);
                    }
                }else{
                    Log.d("getWallImagesForMyOwn", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForMyOwn", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getCommments(final Object delegate, int nLimit){
        ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery(Constants.pClassWallImageComments);

        commentQuery.orderByDescending(Constants.pKeyCreatedAt);
        commentQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastCommentUpdate);
        commentQuery.whereEqualTo(Constants.pKeyImageObjId, Global.wallImage.strImageObjId);

        commentQuery.setSkip(DataStore.getInstance().comments.size());
        commentQuery.setLimit(nLimit);

        commentQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjComment : list){
                        WallImageComment wallImageComment = new WallImageComment(pObjComment);
                        DataStore.getInstance().comments.add(wallImageComment);
                    }
                }else{
                    Log.d("getCommments", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetComments", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getTotalMsg(final Object delegate, int nLimit){
        ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.pClassTotalMsg);
        firstQuery.whereEqualTo(Constants.pKeyFirstUser, Global.myInfo.strUserObjID);

        ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.pClassTotalMsg);
        secondQuery.whereEqualTo(Constants.pKeySecondUser, Global.myInfo.strUserObjID);

        ParseQuery<ParseObject> compoundQuery = ParseQuery.or(Arrays.asList(firstQuery, secondQuery));

        compoundQuery.orderByDescending(Constants.pKeyUpdatedAt);
        compoundQuery.whereLessThanOrEqualTo(Constants.pKeyUpdatedAt, Global.lastTotalMsgUpdate);

        compoundQuery.setSkip(DataStore.getInstance().totalMsg.size());
        compoundQuery.setLimit(nLimit);

        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjTotalMsg : list){
                        TotalMsg totalMsg = new TotalMsg(pObjTotalMsg);

                        DataStore.getInstance().totalMsg.add(totalMsg);
                        DataStore.getInstance().totalMsgPFObjectMap.put(totalMsg.strObjId, pObjTotalMsg);
                    }
                }else{
                    Log.d("getTotalMsg", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetTotalMsg", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getDetailMsg(final Object delegate, String strCompoundKey, int nLimit){
        ParseQuery<ParseObject> queryDetailMsg = ParseQuery.getQuery(Constants.pClassDetailMsg);

        queryDetailMsg.orderByDescending(Constants.pKeyCreatedAt);
        queryDetailMsg.whereEqualTo(Constants.pKeyCompoundUser, strCompoundKey);
        queryDetailMsg.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastDetailMsgUpdate);
        queryDetailMsg.setSkip(DataStore.getInstance().detailMsg.size());
        queryDetailMsg.setLimit(nLimit);

        queryDetailMsg.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjDetailMsg : list){
                        DetailMsg detailMsg = new DetailMsg(pObjDetailMsg);
                        DataStore.getInstance().detailMsg.add(0, detailMsg);
                    }
                }else{
                    Log.d("getDetailMsg", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetDetailMsg", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getNotifyPosts(final Object delegate, int nLimit){
        ParseQuery<ParseObject> queryNotifyPost = ParseQuery.getQuery(Constants.pClassNotifyPost);

        queryNotifyPost.orderByDescending(Constants.pKeyCreatedAt);
        queryNotifyPost.whereEqualTo(Constants.pKeyUserObjId, Global.myInfo.strUserObjID);

        queryNotifyPost.setSkip(DataStore.getInstance().notifyPosts.size());
        queryNotifyPost.setLimit(nLimit);

        queryNotifyPost.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (ParseObject pObjNotify : list) {
                        NotifyPost notifyPost = new NotifyPost(pObjNotify);

                        DataStore.getInstance().notifyPosts.add(notifyPost);
                        DataStore.getInstance().userNotifyPostPFObjectMap.put(notifyPost.strObjId, pObjNotify);
                    }
                } else {
                    Log.d("getNotifyPosts", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetNotifyPosts", null);

                    if (method != null) {
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void  getWallImageForCategory(Object delegate, String strCategory, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForCategory);
        imageQuery.whereEqualTo(Constants.pKeyCategory, strCategory);
        imageQuery.whereEqualTo(Constants.pKeyIsRecipe, 1);
        imageQuery.setSkip(DataStore.getInstance().wallImagesForCategory.size());
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForCategory.add(wallImage);
                    }
                }else{
                    Log.d("getWallImageForCategory", e.getLocalizedMessage());
                }
            }
        });

        Method method = null;
        try {
            Class objectedClass = delegate.getClass();
            
            method = objectedClass.getMethod("didGetWallImageForCategory", null);

            if(method != null){
                method.invoke(delegate, null);
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    public static void getWallImageForTag(final Object delegate, String strTag, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByDescending(Constants.pKeyCreatedAt);
        imageQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForTag);
        imageQuery.whereContainsAll(Constants.pKeyTag, Arrays.asList(strTag));
        imageQuery.setSkip(DataStore.getInstance().wallImagesForTag.size());
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForTag.add(wallImage);
                    }
                }else{
                    Log.d("getWallImageForTag", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForTag", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void  getWallImageForSearchOfNewsFeed(final Object delegate, String strSearch, int nLimit){
        ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        firstQuery.whereContains(Constants.pKeySelfComment, strSearch);

        ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        secondQuery.whereContains(Constants.pKeyRecipe, strSearch);

        ParseQuery<ParseObject> compoundQuery = ParseQuery.or(Arrays.asList(firstQuery, secondQuery));

        compoundQuery.orderByDescending(Constants.pKeyCreatedAt);
        compoundQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForSearch);

        compoundQuery.setSkip(DataStore.getInstance().wallImagesForSearch.size());
        compoundQuery.setLimit(nLimit);

        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForSearch.add(wallImage);
                    }
                }else{
                    Log.d("SearchOfNewsFeed", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForSearch", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getWallImageForSearchOfRecipe(final Object delegate, String strSearch, int nLimit){
        ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        firstQuery.whereContains(Constants.pKeySelfComment, strSearch);

        ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        secondQuery.whereContains(Constants.pKeyRecipe, strSearch);

        ParseQuery<ParseObject> compoundQuery = ParseQuery.or(Arrays.asList(firstQuery, secondQuery));

        compoundQuery.orderByDescending(Constants.pKeyCreatedAt);
        compoundQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForSearch);
        compoundQuery.whereEqualTo(Constants.pKeyIsRecipe, 1);

        compoundQuery.setSkip(DataStore.getInstance().wallImagesForSearch.size());
        compoundQuery.setLimit(nLimit);

        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForSearch.add(wallImage);
                    }
                }else{
                    Log.d("SearchOfRecipe", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForSearch", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void getWallImageForSearchOfFavorite(final Object delegate,String strSearch, int nLimit){

        ArrayList<String> arrTmp = (ArrayList<String>)Global.myInfo.arrFavorites.clone();

        for(WallImage wallImage : DataStore.getInstance().wallImagesForSearch){
            if(arrTmp.contains(wallImage.strImageObjId)){
                arrTmp.remove(wallImage.strImageObjId);
            }
        }

        ParseQuery<ParseObject> firstQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        firstQuery.whereContains(Constants.pKeySelfComment, strSearch);

        ParseQuery<ParseObject> secondQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);
        secondQuery.whereContains(Constants.pKeyRecipe, strSearch);

        ParseQuery<ParseObject> compoundQuery = ParseQuery.or(Arrays.asList(firstQuery, secondQuery));

        compoundQuery.orderByDescending(Constants.pKeyCreatedAt);
        compoundQuery.whereLessThanOrEqualTo(Constants.pKeyCreatedAt, Global.lastImageUpdateForSearch);
        compoundQuery.whereContainedIn(Constants.pKeyObjId, arrTmp);

        compoundQuery.setSkip(DataStore.getInstance().wallImagesForSearch.size());
        compoundQuery.setLimit(nLimit);

        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForSearch.add(wallImage);
                    }
                }else{
                    Log.d("SearchOfFavorite", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didGetWallImageForSearch", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    public static void loadMoreForNewsFeed(final Object delegate, int nLimit){

        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByAscending(Constants.pKeyCreatedAt);
        imageQuery.whereGreaterThan(Constants.pKeyCreatedAt, Global.lastImageUpdateForNewsFeed);
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForNewsFeed.add(0, wallImage);

                        if(pObjWallImage.getCreatedAt().compareTo(Global.lastImageUpdateForNewsFeed) < 0){
                            Global.lastImageUpdateForNewsFeed = pObjWallImage.getCreatedAt();
                        }
                    }
                }else{
                    Log.d("loadMoreForNewsFeed", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didLoadMoreForNewsFeed", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void loadMoreForRecipe(final Object delegate, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByAscending(Constants.pKeyCreatedAt);
        imageQuery.whereGreaterThan(Constants.pKeyCreatedAt, Global.lastImageUpdateForRecipe);
        imageQuery.whereEqualTo(Constants.pKeyIsRecipe, 1);
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForRecipe.add(0, wallImage);

                        if(pObjWallImage.getCreatedAt().compareTo(Global.lastImageUpdateForRecipe) < 0){
                            Global.lastImageUpdateForRecipe = pObjWallImage.getCreatedAt();
                        }
                    }
                }else{
                    Log.d("loadMoreForRecipe", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didLoadMoreForRecipe", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void loadMoreForCategory(final Object delegate, String strCategory, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByAscending(Constants.pKeyCreatedAt);
        imageQuery.whereGreaterThan(Constants.pKeyCreatedAt, Global.lastImageUpdateForCategory);
        imageQuery.whereEqualTo(Constants.pKeyCategory, strCategory);
        imageQuery.whereEqualTo(Constants.pKeyIsRecipe, 1);
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForCategory.add(0, wallImage);

                        if(pObjWallImage.getCreatedAt().compareTo(Global.lastImageUpdateForCategory) < 0){
                            Global.lastImageUpdateForCategory = pObjWallImage.getCreatedAt();
                        }
                    }
                }else{
                    Log.d("loadMoreForCategory", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didLoadMoreForCategory", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void loadMoreForTag(final Object delegate, String strTag, int nLimit){
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery(Constants.pClassWallImageOther);

        imageQuery.orderByAscending(Constants.pKeyCreatedAt);
        imageQuery.whereGreaterThan(Constants.pKeyCreatedAt, Global.lastImageUpdateForTag);
        imageQuery.whereContainsAll(Constants.pKeyTag, Arrays.asList(strTag));
        imageQuery.setLimit(nLimit);

        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null){
                    for(ParseObject pObjWallImage : list){
                        WallImage wallImage = Commons.getImageDataFrom(pObjWallImage);
                        DataStore.getInstance().wallImagesForTag.add(0, wallImage);

                        if(pObjWallImage.getCreatedAt().compareTo(Global.lastImageUpdateForTag) < 0){
                            Global.lastImageUpdateForTag = pObjWallImage.getCreatedAt();
                        }
                    }
                }else{
                    Log.d("loadMoreForTag", e.getLocalizedMessage());
                }

                Method method = null;
                try {
                    Class objectedClass = delegate.getClass();
                    
                    method = objectedClass.getMethod("didLoadMoreForTag", null);

                    if(method != null){
                        method.invoke(delegate, null);
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
