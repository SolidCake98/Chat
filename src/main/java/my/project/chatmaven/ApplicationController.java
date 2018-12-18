/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.project.chatmaven;

import java.security.KeyPair;
import java.util.HashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author danil
 */
@ApplicationScoped
@Named("applicationController")
public class ApplicationController {
    
    private String FILE_PREFIX = "/home/danil/projects/";
    private static String DEFAULT_ICON_PROFILE = "/res/default/death.jpg";

    /**
     *
     * @return Возвращает путь к стандартной аватарке пользователя
     */
    public static String getDEFAULT_ICON_PROFILE() {
        return DEFAULT_ICON_PROFILE;
    }

    /**
     *
     * @return Возвращает путь к папке с файлами
     */
    public String getFILE_PREFIX() {
        return FILE_PREFIX;
    }
    
    private HashMap<Integer, KeyPair> jwtKeysMap = new HashMap<>();
    
    /**
     *
     * @return Возвращает открытый и закрытый ключ для пользователя
     */
    public HashMap<Integer, KeyPair> getJwtKeysMap() {
        return jwtKeysMap;
    }

    /**
     *Сохраняет открытый и закрытый ключ для подписи токена на сервере во время работы сервера
     * @param userId Id пользователя
     * @param keyPair Открытый и закрытый ключ для подписи токена
     */
    public void addJwtKeyToMap(Integer userId, KeyPair keyPair) {
        jwtKeysMap.put(userId, keyPair);
    }
    
}
