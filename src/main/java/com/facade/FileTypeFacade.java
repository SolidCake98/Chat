/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.FileType;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class FileTypeFacade extends AbstractFacade<FileType>{

    public FileTypeFacade() {
        super(FileType.class);
    }
    
}
