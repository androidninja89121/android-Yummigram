package com.zhs1.Yummigram;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;
import com.zhs1.Yummigram.model.DataStore;
import com.zhs1.Yummigram.model.NotifyPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;


public class MyCustomParsePushReceiver extends ParsePushBroadcastReceiver {
	private static final String TAG = "ParseReceiver";
   	Context context;
    String strAlert;

	
	@Override
	protected void onPushReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
//		super.onPushReceive(arg0, arg1);
		
		context = arg0;

        try {
            if(arg1 == null){
                Log.d(TAG, "Receiver intent null");
            }else{
                JSONObject json = new JSONObject(arg1.getExtras().getString("com.parse.Data"));

                String strMode = json.getString(Constants.pnMode);
                strAlert = json.getString(Constants.pnAlert);

                if(strMode.equals(Constants.PN_MESSAGE)){
                    if(Global.totalChatActivity != null){
                        Global.totalChatActivity.refreshData();
                    }else if(Global.detailChatActivity != null){
                        Global.detailChatActivity.refreshData();
                    }else if(Global.mainActivity != null){
                        Global.mainActivity.setNewMessage();
                    }
                }else{
                    Commons.NotifyType[] values = Commons.NotifyType.values();
                    int nOrdinal = json.getInt(Constants.pnNotifyType);

                    Commons.NotifyType nType = values[nOrdinal];
                    String strImageObjId = json.getString(Constants.pnImageId);
                    String strOtherUserObjId = json.getString(Constants.pnUserObjId);

                    NotifyPost notifyPost = new NotifyPost();

                    notifyPost.nType = nType;
                    notifyPost.strOtherUserObjId = strOtherUserObjId;
                    notifyPost.strImageObjId = strImageObjId;
                    notifyPost.createdDate = Calendar.getInstance().getTime();

                    DataStore.getInstance().notifyPosts.add(0, notifyPost);

                    if(Global.notifyActivity != null){
                        Global.notifyActivity.refreshData();
                    }else if(Global.mainActivity != null){
                        Global.mainActivity.setNewNotify();
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


	}
	
	
	@SuppressLint("NewApi")
	private void getNotification(Intent intent)
    {
          // determine label based on the class
        
          String message = strAlert;
          String title = "Yummigram";
          
          PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);

          Notification notification;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
          {
        	  NotificationCompat.Builder builder = new NotificationCompat.Builder(
                      context);
              notification = builder.setContentIntent(pi)
                      .setSmallIcon(R.drawable.ic_launcher).setTicker(message).setWhen(0)
                      .setAutoCancel(true).setContentTitle("Yummigram")
                      .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                      .setContentText(message).build();
              
              notification.defaults = Notification.DEFAULT_ALL;
          }
          else
          {
              notification =
                      new Notification(R.drawable.ic_launcher, message,
                              System.currentTimeMillis());
              notification.setLatestEventInfo(context, title, message, pi);
          }

     
          NotificationManager nNotiMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
          nNotiMgr.notify(0, notification);
          
    }
	

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);

	}

	@Override
	protected void onPushOpen(Context context, Intent intent) {
	}

}
