/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.UserChatStatus;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class UserChatStatusFacade extends AbstractFacade<UserChatStatus>{

    public UserChatStatusFacade() {
        super(UserChatStatus.class);
    }
    
}
