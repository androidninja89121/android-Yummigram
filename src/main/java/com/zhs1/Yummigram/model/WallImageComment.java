package com.zhs1.Yummigram.model;

import com.parse.ParseObject;
import com.zhs1.Yummigram.global.Constants;
import com.zhs1.Yummigram.global.Global;

import java.util.Date;

/**
 * Created by I54460 on 6/29/2015.
 */
public class WallImageComment {
    public String strComment;
    public String strUserObjId;
    public String strUserFBId;
    public String strImageObjId;
    public Date createdDate;

    public WallImageComment(ParseObject pObj){
        super();

        this.strComment = pObj.getString(Constants.pKeyComments);
        this.strUserObjId = pObj.getString(Constants.pKeyUserObjId);
        this.strUserFBId = pObj.getString(Constants.pKeyFBID);
        this.strImageObjId = pObj.getString(Constants.pKeyImageObjId);
        this.createdDate = pObj.getCreatedAt();
    }
}
