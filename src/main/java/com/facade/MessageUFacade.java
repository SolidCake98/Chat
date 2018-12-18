/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.MessageU;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class MessageUFacade extends AbstractFacade<MessageU>{
    
    public MessageUFacade() {
        super(MessageU.class);
    }
    
    public List<MessageU> getMessagesByChat(int chatid) {
        return getEntityManager()
                .createQuery("SELECT m FROM MessageU m WHERE m.chat.id =:chatid ORDER BY m.id ASC")
                .setParameter("chatid", chatid)
                .getResultList();
    }
    
    public List<MessageU> get20MessagesByChat(int chatid, Integer start) {
        Long msgCount = getEntityManager()
                .createQuery("SELECT count(m) FROM MessageU m WHERE m.chat.id =:chatid", Long.class)
                .setParameter("chatid", chatid)
                .getSingleResult();
        Integer firstResult = Math.toIntExact(msgCount) - start - 19;
        Integer maxRes = 20;
        if (firstResult < 1 && start < msgCount) {
            firstResult = 1;
            maxRes = Math.toIntExact(msgCount) - start;
        } else if (firstResult < 1) {
            return new ArrayList<>();
        }
        return getEntityManager()
                .createQuery("SELECT m FROM MessageU m WHERE m.chat.id =:chatid order by m.id")
                .setParameter("chatid", chatid)
                .setFirstResult(firstResult - 1)
                .setMaxResults(maxRes)
                .getResultList();
    }
    
}
