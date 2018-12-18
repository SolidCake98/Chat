/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.messages;

import com.models.Message;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;

/**
 *
 * @author root
 */

@ServerEndpoint(value= "/ChatEndpoint/{username}", decoders = MessageDecoder.class, 
  encoders = MessageEncoder.class )

public class ChatEndpoint {
    private Session session;
    private static Set<ChatEndpoint> chatEndpoints 
      = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();
 
    @OnOpen
    public void onOpen(
      
      @PathParam("username") String username, Session session) throws IOException, EncodeException {
  
        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), username);
 
        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }
 
    @OnMessage
    public void onMessage(Session session, Message message) 
      throws IOException, EncodeException {
  
        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }
 
    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
  
        chatEndpoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }
 
    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }
 
    private static void broadcast(Message message) 
      throws IOException, EncodeException {
  
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().
                      sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
