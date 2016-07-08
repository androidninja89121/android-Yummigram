package com.zhs1.Yummigram.model;

import com.parse.ParseObject;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.Date;

/**
 * Created by I54460 on 6/29/2015.
 */
public class DetailMsg {
    public boolean isFromMe;
    public String strMessage;
    public Date createdDate;

    public DetailMsg(ParseObject pObj){
        super();

        String strMainUserObjId  = pObj.getString(Constants.pKeyMainUser);

        this.strMessage = pObj.getString(Constants.pKeyMsg);
        this.isFromMe = strMainUserObjId.equals(Global.myInfo.strUserObjID);
        this.createdDate = pObj.getUpdatedAt();
    }
}
