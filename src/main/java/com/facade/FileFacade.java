/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.facade;

import com.models.File;
import javax.ejb.Stateless;

/**
 *
 * @author root
 */
@Stateless
public class FileFacade extends AbstractFacade<File>{

    public FileFacade() {
        super(File.class);
    }
    
    
}
