/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.Users;
import com.restservice.UserRestController;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.json.JSONObject;

/**
 *
 * @author root
 */
@Stateless
public class UserFacade extends AbstractFacade<Users> {

    @Inject
    UserRestController userRestController;
    
    public UserFacade() {
        super(Users.class);
    }

    public List<Users> getAllUsers() {
        return getEntityManager().createQuery("SELECT c FROM Users c ORDER BY c.id", Users.class)
                .getResultList();
    }

    public List<Users> getUserByName(String name) {
        return getEntityManager()
                .createQuery("SELECT u FROM Users u WHERE u.name = :name", Users.class)
                .setParameter("name", name)
                .getResultList();
    }

    public Users getUserByNameAndPass(String name, String pass) {
        return getEntityManager()
                .createQuery("SELECT u FROM Users u WHERE u.name = :name and u.password = :pass", Users.class)
                .setParameter("name", name)
                .setParameter("pass", pass)
                .getSingleResult();

    }
    
    public Users findUserByToken(String token){
        JSONObject payload = userRestController.getJWTPayload(token);
        return this.find(Integer.parseInt(payload.getString("userId")));
    }

}
