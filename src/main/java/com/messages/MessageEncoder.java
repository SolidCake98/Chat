/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.messages;

import com.google.gson.Gson;
import com.models.Message;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author root
 */
public class MessageEncoder implements Encoder.Text<Message>{
    
     private static Gson gson = new Gson();
 
    @Override
    public String encode(Message message) throws EncodeException {
        return gson.toJson(message);
    }
 
    /**
     *
     * @param endpointConfig
     */
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }
 
    @Override
    public void destroy() {
        // Close resources
    }
    
}
