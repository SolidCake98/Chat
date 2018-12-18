/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author root
 */
@Entity
@Table(name = "user_chat")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserChat.findAll", query = "SELECT u FROM UserChat u")
    , @NamedQuery(name = "UserChat.findById", query = "SELECT u FROM UserChat u WHERE u.id = :id")})
public class UserChat implements Serializable {

    @JoinColumn(name = "status", referencedColumnName = "id")
    @ManyToOne
    private UserChatStatus status;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "chat", referencedColumnName = "id")
    @ManyToOne
    private Chat chat;
    @JoinColumn(name = "user_t", referencedColumnName = "id")
    @ManyToOne
    private Users userT;

    public UserChat() {
    }

    public UserChat(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Users getUserT() {
        return userT;
    }

    public void setUserT(Users userT) {
        this.userT = userT;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserChat)) {
            return false;
        }
        UserChat other = (UserChat) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.models.UserChat[ id=" + id + " ]";
    }

    public UserChatStatus getStatus() {
        return status;
    }

    public void setStatus(UserChatStatus status) {
        this.status = status;
    }
    
}
