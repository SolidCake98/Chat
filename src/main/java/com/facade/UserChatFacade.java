/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.Chat;
import com.models.UserChat;
import com.models.Users;
import java.util.List;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class UserChatFacade extends AbstractFacade<UserChat> {

    public UserChatFacade() {
        super(UserChat.class);
    }

    public UserChat findUserChatByChatAndUser(Chat chat, Users user) {
        try {
            return (UserChat) getEntityManager()
                    .createQuery("SELECT u FROM UserChat u WHERE u.chat.id = :chat and u.userT.id = :user")
                    .setParameter("chat", chat.getId())
                    .setParameter("user", user.getId())
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserChat> findUserChatByChatAndUserList(Chat chat, Users user) {
        return getEntityManager()
                .createQuery("SELECT u FROM UserChat u WHERE u.chat.id = :chat and u.userT.id = :user")
                .setParameter("chat", chat.getId())
                .setParameter("user", user.getId())
                .getResultList();
    }

    public List<UserChat> findUserChatByUser(Users user) {
        return getEntityManager()
                .createQuery("SELECT u FROM UserChat u WHERE u.userT.id = :user")
                .setParameter("user", user.getId())
                .getResultList();
    }

    public List<UserChat> findUserChatByChat(Chat chat) {
        return getEntityManager()
                .createQuery("SELECT u FROM UserChat u WHERE u.chat.id = :chat")
                .setParameter("chat", chat.getId())
                .getResultList();
    }

    public List<Users> findFriendByChatAndUser(Chat chat, Users user) {
        return getEntityManager()
                .createQuery("SELECT u.userT FROM UserChat u WHERE u.userT.id != :user and u.chat.id=:chat")
                .setParameter("user", user.getId())
                .setParameter("chat", chat.getId())
                .getResultList();
        //return (User) userChat.getUser();
    }

}
