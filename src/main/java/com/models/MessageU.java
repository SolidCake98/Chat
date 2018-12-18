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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author root
 */
@Entity
@Table(name = "message_u")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageU.findAll", query = "SELECT m FROM MessageU m")
    , @NamedQuery(name = "MessageU.findById", query = "SELECT m FROM MessageU m WHERE m.id = :id")
    , @NamedQuery(name = "MessageU.findByText", query = "SELECT m FROM MessageU m WHERE m.text = :text")})
public class MessageU implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "text")
    private String text;
    @JoinColumn(name = "chat", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Chat chat;
    @JoinColumn(name = "fromu", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Users fromu;

    public MessageU() {
    }

    public MessageU(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Users getFromu() {
        return fromu;
    }

    public void setFromu(Users fromu) {
        this.fromu = fromu;
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
        if (!(object instanceof MessageU)) {
            return false;
        }
        MessageU other = (MessageU) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.models.MessageU[ id=" + id + " ]";
    }
    
}
