package com.zhs1.Yummigram.model;

import com.parse.ParseObject;
import com.zhs1.Yummigram.global.Commons;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.Date;

/**
 * Created by I54460 on 6/29/2015.
 */
public class NotifyPost {
    public Commons.NotifyType nType;
    public String            strObjId;
    public String            strOtherUserObjId;
    public String            strImageObjId;
    public Date              createdDate;
    public boolean           viewed;

    public NotifyPost(ParseObject pObj){
        super();

        this.strObjId = pObj.getObjectId();

        Commons.NotifyType[] values = Commons.NotifyType.values();
        int nOrdinal = pObj.getNumber(Constants.pKeyNotifyType).intValue();

        this.nType = values[nOrdinal];
        this.strOtherUserObjId = pObj.getString(Constants.pKeyOtherUserObjId);
        this.strImageObjId = pObj.getString(Constants.pKeyImageObjId);
        this.createdDate = pObj.getCreatedAt();
        this.viewed = pObj.getBoolean(Constants.pKeyViewed);
    }

    public NotifyPost(){
        super();
    }
}
