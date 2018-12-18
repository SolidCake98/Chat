/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.messages;

import com.facade.ChatFacade;
import com.facade.MessageUFacade;
import com.facade.UserChatFacade;
import com.facade.UserChatStatusFacade;
import com.facade.UserFacade;
import com.models.Chat;
import com.models.MessageU;
import com.models.UserChat;
import com.models.UserChatStatus;
import com.models.Users;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author danil
 */
@ServerEndpoint(value = "/SingleChat")

public class SingleChatEndpoint {

    @PersistenceContext(unitName = "ChatPU")
    private EntityManager entityManager;

    @Inject
    private ChatFacade chatFacade;

    @Inject
    private UserFacade userFacade;

    @Inject
    private UserChatFacade userChatFacade;

    @Inject
    private MessageUFacade messageUFacade;

    @Inject
    private UserChatStatusFacade statusFacade;

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    private MessageU currentMessage;

    /**
     *
     * @param session Сессия пользователя
     */
    @OnOpen
    public void onOpen(Session session) {
        peers.add(session);
    }

    /**
     *
     * @param session Сессия
     * @param message Сообщение, отправленное пользователем
     * @throws IOException
     * @throws EncodeException
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException, EncodeException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject jsonObject = jsonReader.readObject();
            String text;
            try {

                if (jsonObject.getString("type").equals("createChat")) {
                    Users user = userFacade.getUserByName(jsonObject.getString("text")).get(0);
                    JsonObject newJsonObject = Json.createObjectBuilder()
                            .add("type", "createChat")
                            .add("tou", user.getId())
                            .add("chat", jsonObject.getInt("chat"))
                            .add("fromu", jsonObject.get("fromu")).build();
                    sendMessagePeer(newJsonObject);
                    return;
                }
                if (jsonObject.getString("type").equals("creatGroupChat")) {
                    JsonArray nameList = jsonObject.getJsonArray("text");
                    for (int i = 0; i < nameList.size(); i++) {
                        if (!userFacade.getUserByName(nameList.getString(i)).isEmpty()) {
                            Users user = userFacade.getUserByName(nameList.getString(i)).get(0);
                            JsonObject newJsonObject = Json.createObjectBuilder()
                                    .add("type", "createGroupChat")
                                    .add("tou", user.getId())
                                    .add("chat", jsonObject.getInt("chat"))
                                    .add("fromu", jsonObject.get("fromu")).build();
                            sendMessagePeer(newJsonObject);
                        }
                    }
                    return;
                }
                if (jsonObject.getString("type").equals("text")) {
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    if (isAllowed(getStatusForUserAndChat(jsonObject, chat))) {
                        List<UserChat> listUserChat = userChatFacade.findUserChatByChat(chat);
                        for (UserChat uc : listUserChat) {
                            userChatFacade.edit(uc);
                        }
                        JsonObjectBuilder job = Json.createObjectBuilder()
                                .add("type", "text")
                                .add("text", jsonObject.getString("text"))
                                .add("tou", 0)
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat"))
                                .add("avatar", jsonObject.getString("avatar"));
                        JsonObject newJsonObject = job.build();
                        sendMessagePeer(newJsonObject);
                        createMessage(jsonObject);
                    } else {
                        JsonObjectBuilder job = Json.createObjectBuilder()
                                .add("type", "text")
                                .add("text", "ты забанен")
                                .add("tou", -1)
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat"))
                                .add("avatar", jsonObject.getString("avatar"));
                        JsonObject newJsonObject = job.build();
                        sendMessagePeer(newJsonObject);
                    }
                }
                if (jsonObject.getString("type").equals("file")) {
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    if (isAllowed(getStatusForUserAndChat(jsonObject, chat))) {
                        List<UserChat> listUserChat = userChatFacade.findUserChatByChat(chat);
                        for (UserChat uc : listUserChat) {
                            userChatFacade.edit(uc);
                        }
                        JsonObjectBuilder job = Json.createObjectBuilder()
                                .add("type", "file")
                                .add("text", jsonObject.getString("text"))
                                .add("filePath", jsonObject.get("filePath"))
                                .add("tou", 0)
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat"))
                                .add("avatar", jsonObject.getString("avatar"));
                        JsonObject newJsonObject = job.build();
                        sendMessagePeer(newJsonObject);
                        createMessage(jsonObject);
                    } else {
                        JsonObjectBuilder job = Json.createObjectBuilder()
                                .add("type", "text")
                                .add("text", "ты забанен")
                                .add("tou", -1)
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat"))
                                .add("avatar", jsonObject.getString("avatar"));
                        JsonObject newJsonObject = job.build();
                        sendMessagePeer(newJsonObject);
                    }
                }
                if (jsonObject.getString("type").equals("add")) {
                    Users addUser = userFacade.getUserByName(jsonObject.getString("text")).get(0);
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    if (isAdmin(getStatusForUserAndChat(jsonObject, chat))) {
                        if (userChatFacade.findUserChatByChatAndUserList(chat, addUser).size() > 0) {
                            UserChat userChat = userChatFacade.findUserChatByChatAndUser(chat, addUser);
                            text = "User " + addUser.getName() + " already added to the chat";
                        } else {
                            UserChat userChat = new UserChat();
                            userChat.setChat(chat);
                            userChat.setUserT(addUser);
                            userChat.setStatus(statusFacade.find(2));
                            userChatFacade.create(userChat);
                            text = "User " + addUser.getName() + " was added to the chat";
                        }
                        JsonObject newJsonObject = Json.createObjectBuilder()
                                .add("type", "add")
                                .add("text", text)
                                .add("tou", addUser.getId())
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat")).build();
                        sendMessagePeer(newJsonObject);
                        createMessage(newJsonObject);
                        return;
                    } else {
                        JsonObject newJsonObject = Json.createObjectBuilder()
                                .add("type", "add")
                                .add("text", "у вас нет прав")
                                .add("tou", 0)
                                .add("fromu", jsonObject.get("fromu"))
                                .add("chat", jsonObject.getInt("chat")).build();
                        sendMessagePeer(newJsonObject);
                        return;
                    }
                }
                if (jsonObject.getString("type").equals("ban")) {
                    Users banUser = userFacade.find(jsonObject.getInt("text"));
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    text = "User " + banUser.getName() + " был забанен навсегда";
                    JsonObject newJsonObject = Json.createObjectBuilder()
                            .add("type", "text")
                            .add("text", text)
                            .add("tou", banUser.getId())
                            .add("fromu", jsonObject.get("fromu"))
                            .add("chat", jsonObject.getInt("chat")).build();
                    UserChat userChat = userChatFacade.findUserChatByChatAndUser(chat, banUser);
                    UserChatStatus status = statusFacade.find(3);
                    userChat.setStatus(status);
                    userChatFacade.edit(userChat);

                    sendMessagePeer(newJsonObject);
                    createMessage(newJsonObject);
                    return;
                }
                if (jsonObject.getString("type").equals("moder")) {
                    Users banUser = userFacade.find(jsonObject.getInt("text"));
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    text = "User " + banUser.getName() + " теперь имеет власть";
                    JsonObject newJsonObject = Json.createObjectBuilder()
                            .add("type", "text")
                            .add("text", text)
                            .add("tou", banUser.getId())
                            .add("fromu", jsonObject.get("fromu"))
                            .add("chat", jsonObject.getInt("chat")).build();
                    UserChat userChat = userChatFacade.findUserChatByChatAndUser(chat, banUser);
                    UserChatStatus status = statusFacade.find(5);
                    userChat.setStatus(status);
                    userChatFacade.edit(userChat);

                    sendMessagePeer(newJsonObject);
                    createMessage(newJsonObject);
                    return;
                }
                if (jsonObject.getString("type").equals("timeout")) {
                    Users banUser = userFacade.find(jsonObject.getInt("text"));
                    Chat chat = chatFacade.find(jsonObject.getInt("chat"));
                    text = "User " + banUser.getName() + " timeout";
                    JsonObject newJsonObject = Json.createObjectBuilder()
                            .add("type", "timeout")
                            .add("text", text)
                            .add("tou", banUser.getId())
                            .add("fromu", jsonObject.get("fromu"))
                            .add("chat", jsonObject.getInt("chat")).build();
                    UserChat userChat = userChatFacade.findUserChatByChatAndUser(chat, banUser);
                    UserChatStatus status = statusFacade.find(3);
                    userChat.setStatus(status);
                    userChatFacade.edit(userChat);

                    sendMessagePeer(newJsonObject);
                    createMessage(newJsonObject);
                    return;
                }
            } catch (Exception e) {

            }

        }
    }

    /**
     *Отправка сообщения сессиям 
     * @param jsonObject json, который пришел от пользователя
     * @throws IOException
     * @throws EncodeException
     */
    public void sendMessagePeer(JsonObject jsonObject) throws IOException, EncodeException {
        JsonObjectBuilder job = Json.createObjectBuilder()
                .add("type", jsonObject.getString("type"))
                .add("peer", "peer")
                .add("fromu", jsonObject.get("fromu"))
                .add("chat", jsonObject.getInt("chat"));
        if (jsonObject.containsKey("text")) {
            job.add("text", jsonObject.getString("text"));
            job.add("chatName", chatFacade.find(jsonObject.getInt("chat")).getName());
        }
        if (jsonObject.containsKey("tou")) {
            job.add("tou", jsonObject.getInt("tou"));
        }
        if (jsonObject.containsKey("filePath")) {
            job.add("filePath", jsonObject.get("filePath"));
        }
        JsonObject newJsonObject = job.build();
        for (Session peer : peers) {
            if (peer.isOpen()) {
                try {
                    peer.getBasicRemote().sendObject(newJsonObject);
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     *
     * @param jsonObject Json, который пришел от пользователя
     * @param chat Чат
     * @return Возвращает статус пользователя в чате
     */
    public UserChatStatus getStatusForUserAndChat(JsonObject jsonObject, Chat chat) {
        JsonObject js = (JsonObject) jsonObject.get("fromu");
        Users user = userFacade.find(js.getInt("id"));
        UserChat userChat = userChatFacade.findUserChatByChatAndUser(chat, user);
        return userChat.getStatus();
    }

    /**
     *
     * @param status Статус пользователя в чате
     * @return Может ли пользователь отправлять ссобщения в чат
     */
    public boolean isAllowed(UserChatStatus status) {
        return !(status.getId() == 3 || status.getId() == 4);
    }

    /**
     *
     * @param status Статус пользователя в чате
     * @return Пользоавтель - admin
     */
    public boolean isAdmin(UserChatStatus status) {
        return (status.getId() == 1 || status.getId() == 5);
    }

    /**
     *Сохранения сообщения в БД
     * @param jsonObject Json, который пришел от пользователя
     */
    public void createMessage(JsonObject jsonObject) {
        if (currentMessage == null) {
            currentMessage = new MessageU();
        }
        if (!jsonObject.containsKey("text")) {
            return;
        }
        JsonObject fromu = jsonObject.getJsonObject("fromu");
        Users currentUser = new Users(fromu.getInt("id"));
        Chat currentChat = chatFacade.find(jsonObject.getInt("chat"));// new Chat(jsonObject.getInt("chat"));
        currentMessage.setFromu(currentUser);
        if (jsonObject.containsKey("filePath")) {
            JsonObject file = (JsonObject) jsonObject.get("filePath");
            String text = "<a download='' href='" + file.getString("path") + "'>" + file.getString("title") + "</a>";
            currentMessage.setText(text);
        } else {
            currentMessage.setText(jsonObject.getString("text"));
        }
        currentMessage.setChat(currentChat);
        messageUFacade.create(currentMessage);
        currentMessage = new MessageU();
    }
}
