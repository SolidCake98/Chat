/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restservice;

import com.models.Chat;
import java.util.List;

/**
 *
 * @author root
 */
public class ChatRestModel {

    public ChatRestModel(Chat chat, String lastMessage, List<String> userNames) {
        this.chat = chat;
        this.lastMessage = lastMessage;
        this.userNames = userNames;
    }
    
    private Chat chat;
    private String lastMessage;
    private List<String> userNames;

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUserNames() {
        return userNames;
    }

    public void setUserNames(List<String> userNames) {
        this.userNames = userNames;
    }
    
    
    
}
