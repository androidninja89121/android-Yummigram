package com.zhs1.Yummigram.model;

import com.parse.ParseObject;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.Date;

/**
 * Created by I54460 on 6/29/2015.
 */
public class TotalMsg {
    public String strObjId;
    public boolean isFromMe;
    public boolean viewed;
    public String strOtherUserObjId;
    public String strMessage;
    public Date updatedDate;

    public TotalMsg(ParseObject pObj){
        super();

        this.strObjId = pObj.getObjectId();

        String strFirstUserObjId  = pObj.getString(Constants.pKeyFirstUser);
        String strSecondUserObjId = pObj.getString(Constants.pKeySecondUser);
        String strLastUserObjId   = pObj.getString(Constants.pKeyLastUser);

        this.isFromMe = strLastUserObjId.equals(Global.myInfo.strUserObjID);
        this.viewed = pObj.getBoolean(Constants.pKeyViewed);
        this.strOtherUserObjId = strFirstUserObjId.equals(Global.myInfo.strUserObjID) ? strSecondUserObjId: strFirstUserObjId;
        this.updatedDate = pObj.getUpdatedAt();
        this.strMessage = pObj.getString(Constants.pKeyLastMsg);
    }
}
