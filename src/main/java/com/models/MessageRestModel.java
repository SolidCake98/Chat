/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models;

/**
 *
 * @author root
 */
public class MessageRestModel {
    
    private String text;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageRestModel(String text, Integer fromUserId, String fromUserName, String fromUserPhoto, int id) {
        this.text = text;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.fromUserPhotoPath = fromUserPhoto;
        this.id = id;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    private Integer fromUserId;
    private String fromUserName;
    private String fromUserPhotoPath;

    public String getFromUserPhotoPath() {
        return fromUserPhotoPath;
    }

    public void setFromUserPhotoPath(String fromUserPhotoPath) {
        this.fromUserPhotoPath = fromUserPhotoPath;
    }
    
}
