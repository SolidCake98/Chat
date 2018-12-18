/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restservice;

import com.facade.ChatFacade;
import com.facade.MessageUFacade;
import com.facade.UserChatFacade;
import com.facade.UserFacade;
import com.models.Chat;
import com.models.GroupChatAddRQ;
import com.models.MessageRestModel;
import com.models.MessageU;
import com.models.UserChat;
import com.models.Users;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import my.project.chatmaven.ApplicationController;

/**
 *Рест контроллер для чата
 * @author danil
 */
@Stateless
@Path("chat")
public class ChatRestController {

    @PersistenceContext(unitName = "ChatPU")
    private EntityManager entityManager;

    @Inject
    private ChatFacade chatFacade;

    @Inject
    private UserFacade userFacade;

    @Inject
    private MessageUFacade messageUFacade;

    @Inject
    private UserChatFacade userChatFacade;

    @Inject
    private UserRestController userController;

    /**
     * Добавляет чат в бд
     *
     * @param name Имя чата
     * @param token Токен пользователя, отправивший запрос
     * @return Ответ по созданию чата
     * @throws java.lang.Exception
     */
    @GET
    @Path("addChat/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public String addChat(@PathParam("name") String name, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        try {
            createChat(name, ur);
        } catch (Exception e) {

        }
        return "{\"" + ur.getName() + "\":\"You have succesfully create chat\"}";
    }

    /**
     * Создание группового чата
     *
     * @param gcarq Модель группового чата
     * @param token  Токен пользователя, отправивший запрос
     * @throws java.lang.Exception
     */
    @POST
    @Path("addGroupChat")
    @Consumes("application/json")
    public void addGroupChat(GroupChatAddRQ gcarq, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return;
        }
        Users ur = userFacade.findUserByToken(token);
        if (ur != null) {
            if (gcarq.getChatName() == null || gcarq.getChatName().equals("")) {
                createGroupChatNew(gcarq.getNameList(), ur);
            } else {
                createGroupChatNew(gcarq.getNameList(), gcarq.getChatName(), ur);
            }
        }
    }

    @DELETE
    @Path("deleteMessage/{messageId}")
    @Produces({MediaType.APPLICATION_JSON})
    public void deleteMessage(@HeaderParam("token") String token, @PathParam("messageId") int id) throws Exception
    {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return;
        }
        messageUFacade.remove(messageUFacade.find(id));
    }
    
    /**
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает список чатов
     * @throws java.lang.Exception
     *
     */
    @GET
    @Path("chatList")
    @Produces({MediaType.APPLICATION_JSON})
    public List<ChatRestModel> getChatListWOParams(@HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        List<ChatRestModel> returnList = new ArrayList<ChatRestModel>();
        try {
            returnList = getListChatByUser(ur);
        } catch (Exception e) {
            Logger.getLogger(UserRestController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnList;
    }

    /**
     * @param chatId Id чата
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает список сообщений для чатов
     * @throws java.lang.Exception
     *
     */
    @GET
    @Path("messageList/{chatId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<MessageRestModel> getMessageList(@PathParam("chatId") Integer chatId, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        Chat currChat = chatFacade.find(chatId);
        if (ur != null) {

            UserChat userChat = userChatFacade.findUserChatByChatAndUser(currChat, ur);
            userChatFacade.edit(userChat);
        }
        List<MessageU> messageList = new ArrayList<MessageU>();
        List<MessageRestModel> returnList = new ArrayList<MessageRestModel>();
        try {
            messageList = getMessageList(currChat);
        } catch (Exception e) {
            Logger.getLogger(ChatRestController.class.getName()).log(Level.SEVERE, null, e);
        }
        for (MessageU msg : messageList) {
            String avaPath = msg.getFromu().getAvatarPath();
            MessageRestModel tmp = new MessageRestModel(msg.getText(),
                    msg.getFromu().getId(), msg.getFromu().getName(), avaPath, msg.getId());
            returnList.add(tmp);
        }
        return returnList;
    }

    /**
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает 20 сообщений для чата начиная с указанной позиции
     * @param chatId Id чата для которого мы получаем сообщения
     * @param startPos Начальная позиция с которой будет получено 20 сообщений
     * @throws java.lang.Exception
     */
    @GET
    @Path("messageList/{chatId}/{startPos}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<MessageRestModel> get20MessageList(@PathParam("chatId") Integer chatId, @PathParam("startPos") Integer startPos, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        Chat currChat = chatFacade.find(chatId);
        if (ur != null) {
            UserChat userChat = userChatFacade.findUserChatByChatAndUser(currChat, ur);
            userChatFacade.edit(userChat);
        }
        if (ur == null || userChatFacade.findUserChatByChatAndUser(currChat, ur) == null) {
            return null;
        }
        List<MessageU> messageList = new ArrayList<MessageU>();
        List<MessageRestModel> returnList = new ArrayList<MessageRestModel>();
        try {
            messageList = getMessageList20(currChat, startPos);
        } catch (Exception e) {
            Logger.getLogger(UserRestController.class.getName()).log(Level.SEVERE, null, e);
        }
        for (MessageU msg : messageList) {
            String avaPath = msg.getFromu().getAvatarPath();
            MessageRestModel tmp = new MessageRestModel(msg.getText(),
                    msg.getFromu().getId(), msg.getFromu().getName(), avaPath, msg.getId());
            returnList.add(tmp);
        }
        return returnList;
    }

    /**
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает пользователей для чата
     * @param chatId Id чата
     * @throws java.lang.Exception
     */
    @GET
    @Path("getUserList/{chatId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserChat> getUserList(@PathParam("chatId") Integer chatId, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        Chat currChat = chatFacade.find(chatId);
        List<UserChat> userChat = new ArrayList<UserChat>();
        if (ur != null) {
            userChat = userChatFacade.findUserChatByChat(currChat);
        }
        return userChat;
    }

    /**
     *
     * @param chat Чат
     * @param start Индекс, с которого будут возвращены 20 сообщений
     * @return Возвращает 20 сообщений
     */
    public List<MessageU> getMessageList20(Chat chat, Integer start) {
        if (chat != null && chat.getId() != null) {
            return messageUFacade.get20MessagesByChat(chat.getId(), start);
        }
        return null;
    }

    /**
     * Сохранение чата в бд
     *
     * @param userName Имя пользователя, которого добавили в чат
     * @param curUser Пользователь, который создает чат
     */
    public void createChat(String userName, Users curUser) {
        try {
            if (userFacade.getUserByName(userName).size() > 0) {
                Users addUser = userFacade.getUserByName(userName).get(0);
                List<Chat> listChat = chatFacade.FindChatByUsers(addUser.getId(), curUser.getId());
                if (listChat.isEmpty()) {
                    listChat = chatFacade.FindChatByUsers(curUser.getId(), addUser.getId());
                }
                if (addUser.getId() != curUser.getId()) {
                    if (listChat.size() > 0) {
                        UserChat userChat = userChatFacade.findUserChatByChatAndUser(listChat.get(0), curUser);
                        userChatFacade.edit(userChat);
                        createMessage("User " + curUser.getName() + " restored chat", userChat.getChat(), curUser);
                        userChat = userChatFacade.findUserChatByChatAndUser(listChat.get(0), addUser);
                        userChatFacade.edit(userChat);
                        createMessage("User " + addUser.getName() + " restored chat", userChat.getChat(), curUser);
                    } else {
                        Chat newChat = createChatByName(addUser.getName().concat(", " + curUser.getName()));
                        createMessage("User " + curUser.getName() + " created a chat " + newChat.getName(), newChat, curUser);
                        createUserChat(curUser, newChat);
                        createUserChat(addUser, newChat);
                    }
                }
            }
            entityManager.flush();
            entityManager.getEntityManagerFactory().getCache().evictAll();

        } catch (Exception e) {
            Logger.getLogger(ChatRestController.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    /**
     * Сохранение сообщения в бд
     *
     * @param text Текст сообщения
     * @param chat Чат, куда сохраняется сообщение
     * @param user Пользователь, который сохраняет сообщения
     */
    public void createMessage(String text, Chat chat, Users user) {
        MessageU message = new MessageU();
        message.setFromu(user);
        message.setText(text);
        message.setChat(chat);
        messageUFacade.create(message);
    }

    /**
     * Сохранение чата в бд по имени чата
     *
     * @param name Название чата
     * @return Возвращает созданный чат
     */
    public Chat createChatByName(String name) {
        Chat currentChat = new Chat();
        currentChat.setName(name);
        chatFacade.create(currentChat);
        return currentChat;
    }

    /**
     * Сохранение UserChat в бд
     *
     * @param chat Чат
     * @param user Пользователь
     *
     */
    public void createUserChat(Users user, Chat chat) {
        UserChat userChat = new UserChat();
        userChat.setChat(chat);
        userChat.setUserT(user);
        userChatFacade.create(userChat);
    }

    /**
     * @return Возвращает список сообщений
     * @param chat Чат
     *
     */
    public List<MessageU> getMessageList(Chat chat) {
        if (chat != null && chat.getId() != null) {
            return messageUFacade.getMessagesByChat(chat.getId());
        }
        return null;

    }

    /**
     * @return Возвращает список UserChat по чату
     * @param chat Чат
     *
     */
    public List<UserChat> getUserChatListByChat(Chat chat) {
        if (chat.getId() != null) {
            return userChatFacade.findUserChatByChat(chat);
        }
        return new ArrayList<UserChat>();
    }

    /**
     * @return Возвращает пользователя в твоем чате
     * @param chat Чат
     * @param user Пользователь
     */
    public Users getFriendByUserParam(Chat chat, Users user) {
        try {
            List<Users> userList = userChatFacade.findFriendByChatAndUser(chat, user);
            if (userList.size() > 0) {
                return userList.get(0);
            } else {
                return user;
            }
        } catch (Exception e) {
            Logger.getLogger(ChatRestController.class.getName()).log(Level.SEVERE, null, e);
            return user;
        }

    }

    /**
     * @return Возвращает список чатов для пользователя
     * @param ur Пользователь
     */
    public List<ChatRestModel> getListChatByUser(Users ur) {
        List<ChatRestModel> returnList = new ArrayList<ChatRestModel>();
        List<Chat> listChat = new ArrayList<>();
        List<Chat> listChatMessage = new ArrayList<>();
        List<UserChat> listUserChat = userChatFacade.findUserChatByUser(ur);
        for (UserChat uc : listUserChat) {
            listChat.add(uc.getChat());
        }
        for (Chat c : listChat) {
            c.setMessageUList(getMessageList(c));
            listChatMessage.add(c);
        }
        for (Chat curChat : listChatMessage) {
            List<String> userNames = new ArrayList<String>();
            List<MessageU> messageList = getMessageList(curChat);
            String lastMsgText = null;
            MessageU lastMessage = null;
            if (messageList.size() > 0) {
                lastMessage = messageList.get(messageList.size() - 1);
                lastMsgText = lastMessage.getText();
            }
            listChat.add(curChat);
            List<UserChat> userChatList = getUserChatListByChat(curChat);
            Users tempU = getFriendByUserParam(curChat, ur);
            if (tempU != null) {
                userNames.add(tempU.getName());
            }
            returnList.add(new ChatRestModel(curChat, lastMsgText, userNames));
        }
        if (returnList != null) {
            return returnList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * @param userEmailList Пользователи, которые будут добавлены в чат
     * @param chatName Имя чата
     * @param curUser Создатель чата
     */
    public void createGroupChatNew(List<String> userEmailList, String chatName, Users curUser) {
        createGroupChat(userEmailList, chatName, curUser);
    }

    /**
     *
     * @param userEmailList Список пользователей, добавленных в чат
     * @param curUser Пользователь, который создает чат
     */
    public void createGroupChatNew(List<String> userEmailList, Users curUser) {
        List<Users> userList = new ArrayList<Users>();
        String chatName = curUser.getName();
        for (String s : userEmailList) {
            Users tempU = userFacade.getUserByName(s).get(0);
            userList.add(tempU);
            if (chatName.split(" ").length < 3) {
                chatName += " " + tempU.getName();
            }
        }
        createGroupChat(userEmailList, chatName, curUser);
    }

    /**
     * @param userNameList Пользователи, которые будут добавлены в чат
     * @param chatName Имя чата
     * @param curUser Создатель чата
     */
    public void createGroupChat(List<String> userNameList, String chatName, Users curUser) {
        List<Users> userList = new ArrayList<Users>();
        for (String s : userNameList) {
            Users tempU = userFacade.getUserByName(s).get(0);
            userList.add(tempU);
        }
        Chat currentChat = createChatByName(chatName);
        createMessage("User " + curUser.getName() + " created a chat " + currentChat.getName(), currentChat, curUser);
        userList.add(0, curUser);
        for (Users addUser : userList) {

            //userChatFacade.FindUserChatByChatAndUser(chat, addUser);
            if (userChatFacade.findUserChatByChatAndUserList(currentChat, addUser) == null || userChatFacade.findUserChatByChatAndUserList(currentChat, addUser).isEmpty()) {
                createUserChat(addUser, currentChat);
            }
        }
    }
}
