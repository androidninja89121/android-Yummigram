package com.zhs1.Yummigram.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pkmmte.view.CircularImageView;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.DetailMsg;
import com.zhs1.Yummigram.model.UserInfo;
import com.zhs1.Yummigram.utils.CustomActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by I54460 on 6/30/2015.
 */
public class DetailChatActivity extends CustomActivity implements View.OnClickListener{
    UserInfo otherUerInfo;

    Button btnBack, btnPost;
    EditText etxtChat;
    TextView tvTitle;
    LinearLayout lyContent;
    ScrollView scrollView;

    String strOtherUserObjId, strCompoundKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detail_chat);

        Intent myIntent = getIntent(); // gets the previously created intent

        strOtherUserObjId = myIntent.getStringExtra(Constants.eKeyUserObjId);

        otherUerInfo = Commons.getUserInfoFrom(strOtherUserObjId);

        strCompoundKey = Commons.getKeyString(Global.myInfo.strUserObjID, strOtherUserObjId);

        btnBack = (Button)findViewById(R.id.btnBackDetailChat);
        btnPost = (Button)findViewById(R.id.btnPostDetailChat);

        btnBack.setOnClickListener(this);
        btnPost.setOnClickListener(this);

        tvTitle = (TextView)findViewById(R.id.tvTitleDetailChat);

        tvTitle.setText(otherUerInfo.strUserFirstName + " " + otherUerInfo.strUserLastName);

        etxtChat = (EditText)findViewById(R.id.etxtDetailChat);

        scrollView = (ScrollView)findViewById(R.id.scrollViewDetailChat);
        lyContent = (LinearLayout)findViewById(R.id.lyContentDetailChat);

        loadData();
    }

    public void loadData(){
        DataStore.getInstance().detailMsg.clear();
        Global.lastDetailMsgUpdate = Calendar.getInstance().getTime();

        Commons.getDetailMsg(DetailChatActivity.this, strCompoundKey, 10);
    }

    public void didGetDetailMsg(){
        presentData();
    }

    public void presentData(){
        lyContent.removeAllViews();

        for(int i = 0; i < DataStore.getInstance().detailMsg.size(); i ++){
            if(i > 0){
                LinearLayout betweenLayout = new LinearLayout(this);
                betweenLayout.setLayoutParams(new LinearLayout.LayoutParams(100, 5));

                lyContent.addView(betweenLayout);
            }

            DetailMsg detailMsg = DataStore.getInstance().detailMsg.get(i);

            boolean isLastCell = false;

            if(i + 1 == DataStore.getInstance().detailMsg.size()){
                isLastCell = true;
            }else{
                DetailMsg nextMsg = DataStore.getInstance().detailMsg.get(i + 1);

                if(nextMsg.isFromMe == detailMsg.isFromMe){
                    isLastCell = false;
                }else{
                    isLastCell = true;
                }
            }

            RelativeLayout newCell = null;
            CircularImageView imgvPhoto;
            TextView tvChat, tvTime;


            if(detailMsg.isFromMe){
                if(isLastCell){
                    newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_chatme_last, null));

                    imgvPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhotoForChatMeCell);
                    tvChat = (TextView)newCell.findViewById(R.id.tvChatMeLast);
                    tvTime = (TextView)newCell.findViewById(R.id.tvTimeChatMe);

                    if(Global.myInfo.bmpPhoto == null){
                        imgvPhoto.setImageResource(R.drawable.btn_profile);
                    }else{
                        imgvPhoto.setImageBitmap(Global.myInfo.bmpPhoto);
                    }

                    tvTime.setText(android.text.format.DateFormat.format("hh:mm a", detailMsg.createdDate));
                }else{
                    newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_chatme_general, null));

                    tvChat = (TextView)newCell.findViewById(R.id.tvChatMeGeneral);
                }
            }else{
                if(isLastCell){
                    newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_chatother_last, null));

                    imgvPhoto = (CircularImageView)newCell.findViewById(R.id.imgbUserPhotoForChatOtherCell);
                    tvChat = (TextView)newCell.findViewById(R.id.tvChatOtherLast);
                    tvTime = (TextView)newCell.findViewById(R.id.tvTimeChatOther);

                    if(otherUerInfo.bmpPhoto == null){
                        imgvPhoto.setImageResource(R.drawable.btn_profile);
                    }else{
                        imgvPhoto.setImageBitmap(otherUerInfo.bmpPhoto);
                    }

                    tvTime.setText(android.text.format.DateFormat.format("hh:mm a", detailMsg.createdDate));
                }else{
                    newCell = (RelativeLayout)(View.inflate(this, R.layout.cell_chatother_general, null));

                    tvChat = (TextView)newCell.findViewById(R.id.tvChatOtherGeneral);
                }
            }

            tvChat.setText(detailMsg.strMessage);

            lyContent.addView(newCell);
        }
    }

    public void sendChat(final String strMsg){
        ParseUser pUserOther = DataStore.getInstance().userInfoPFObjectMap.get(strOtherUserObjId);

        boolean isNotifyMessage = pUserOther.getBoolean(Constants.pKeyNotifyMessage);

        if(!isNotifyMessage) return;

        final ParseObject pObjDetailMsg = ParseObject.create(Constants.pClassDetailMsg);

        pObjDetailMsg.put(Constants.pKeyCompoundUser, strCompoundKey);
        pObjDetailMsg.put(Constants.pKeyMainUser, Global.myInfo.strUserObjID);
        pObjDetailMsg.put(Constants.pKeyMsg, strMsg);

        pObjDetailMsg.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    String strFirstUser = Global.myInfo.strUserObjID;
                    String strSecondUser = strOtherUserObjId;

                    if(strFirstUser.compareTo(strSecondUser) > 0){
                        strFirstUser = strOtherUserObjId;
                        strSecondUser = Global.myInfo.strUserObjID;
                    }

                    ParseQuery<ParseObject> queryTotalMsg = ParseQuery.getQuery(Constants.pClassTotalMsg);

                    queryTotalMsg.whereEqualTo(Constants.pKeyFirstUser, strFirstUser);
                    queryTotalMsg.whereEqualTo(Constants.pKeySecondUser, strSecondUser);

                    try {
                        int nCount = queryTotalMsg.count();

                        ParseObject pObjTotalMsg = (nCount > 0)? queryTotalMsg.getFirst(): ParseObject.create(Constants.pClassTotalMsg);

                        pObjTotalMsg.put(Constants.pKeyFirstUser, strFirstUser);
                        pObjTotalMsg.put(Constants.pKeySecondUser, strSecondUser);
                        pObjTotalMsg.put(Constants.pKeyLastUser, Global.myInfo.strUserObjID);
                        pObjTotalMsg.put(Constants.pKeyLastMsg, strMsg);
                        pObjTotalMsg.put(Constants.pKeyViewed, false);

                        pObjTotalMsg.saveInBackground();

                        DetailMsg detailMsg = new DetailMsg(pObjDetailMsg);

                        DataStore.getInstance().detailMsg.add(detailMsg);

                        Global.lastDetailMsgUpdate = detailMsg.createdDate;

                        presentData();

                        ParseQuery queryInstallation = ParseInstallation.getQuery();

                        queryInstallation.whereEqualTo(Constants.pKeyUserObjId, strOtherUserObjId);

                        String strFullName = Global.myInfo.strUserFirstName + " " + Global.myInfo.strUserLastName;
                        String strAlert = "message from " + strFullName + ":\n" + strMsg;

                        JSONObject data = new JSONObject();

                        data.put(Constants.pnMode, Constants.PN_MESSAGE);
                        data.put(Constants.pnBadge, Constants.PN_INCREMENT);
                        data.put(Constants.pnAlert, strAlert);
                        data.put(Constants.pnUserObjId, Global.myInfo.strUserObjID);
                        data.put(Constants.pnSound, "Notify_1.wav");

                        ParsePush push = new ParsePush();

                        push.setQuery(queryInstallation);
                        push.setData(data);

                        push.sendInBackground();

                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
            }
        });

    }

    public void refreshData(){
        DataStore.getInstance().detailMsg.clear();
        Global.lastDetailMsgUpdate = Calendar.getInstance().getTime();
        Commons.getTotalMsg(DetailChatActivity.this, 10);
    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            finish();
        }

        if(v == btnPost){
            String strMsg = etxtChat.getText().toString();

            if(strMsg.length() > 0) sendChat(strMsg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Global.detailChatActivity = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.detailChatActivity = DetailChatActivity.this;
    }
}
