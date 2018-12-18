/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.Chat;
import com.models.UserChat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class ChatFacade extends AbstractFacade<Chat>{
    
    public ChatFacade() {
        super(Chat.class);
    }
    
    public List<Chat> FindChatByUsers(Integer userId1, Integer userId2) {
         List<UserChat> resultList = getEntityManager()
                 .createQuery("SELECT uc FROM UserChat uc WHERE uc.userT.id = :userId1 and uc.chat.id in "
                        + "(SELECT uc1.chat.id FROM UserChat uc1 WHERE uc1.userT.id = :userId2)")
                 .setParameter("userId1", userId1)
                 .setParameter("userId2", userId2)
                 .getResultList();
         List<Chat> chatList = new ArrayList<>();
         for(UserChat uc : resultList){
             chatList.add(uc.getChat());
         }
         return chatList;                
    }
}
