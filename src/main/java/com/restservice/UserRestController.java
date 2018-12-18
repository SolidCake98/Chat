/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restservice;

import com.facade.UserFacade;
import com.models.File;
import com.models.Users;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import my.project.chatmaven.ApplicationController;
import org.json.JSONObject;

/**
 *
 * @author danil
 */
@Stateless
@Path("user")
public class UserRestController {

    @PersistenceContext(unitName = "ChatPU")
    private EntityManager entityManager;

    @Inject
    private UserFacade userFacade;

    @Inject
    private UserRestController userRestController;

    @Inject
    private ApplicationController applicationController;

    /**
     *
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает всех пользователей из БД
     * @throws Exception
     */
    @GET
    @Path("getAllUsers")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Users> getAllUsers(@HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userRestController.verifyJWT(token)) {
            return null;
        }
        return userFacade.getAllUsers();
    }

    /**
     *Регистрация пользователя, сохранение в БД
     * @param user Пользователь, который будет зарегистрирован
     * @return Возваращает ответ о успешной или неуспешной регистрации 
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public String registerUser(Users user) throws NoSuchAlgorithmException {
        String shapass = hash(user.getPassword());
        if (userFacade.getUserByName(user.getName()).size() > 0) {
            return "{\"" + user.getName() + "\":\"User with this name already exist\"}";
        }
        user.setPassword(shapass);
        userFacade.create(user);
        return "{\"" + user.getName() + "\":\"You have succesfully registred\"}";
    }

    /**
     *
     * @param password Пароль
     * @return Хэш пароля
     * @throws NoSuchAlgorithmException
     */
    public String hash(String password) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        try {
            sha.update(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }
        StringBuilder out = new StringBuilder();
        if (sha != null) {
            byte[] digest = sha.digest();

            for (int i : digest) {
                String s = Integer.toHexString(0XFF & i);
                if (s.length() < 2) {
                    s = "0" + s;
                }
                out.append(s);
            }
        }
        return out.toString();
    }
    
    /**
     *
     * @param token Токен пользователя, отправивший запрос
     * @return Возвращает текущего пользователя
     * @throws Exception
     */
    @GET
    @Path("curUser")
    @Produces({MediaType.APPLICATION_JSON})
    public Users currentUser(@HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userRestController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        return ur;
    }

    /**
     *Сохранение аватарки для пользователя
     * @param file Аватарка
     * @param token Токен пользователя, отправивший запрос
     * @throws Exception
     */
    @Path("setAvatar")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void setAvatar(File file, @HeaderParam("token") String token) throws Exception {
        if (token.isEmpty() || !userRestController.verifyJWT(token)) {
            return;
        }
        Users ur = userFacade.findUserByToken(token);
        ur.setAvatar(file);
        userFacade.edit(ur);
        entityManager.flush();
    }
    
    /**
     *
     * @param name Имя пользоавтеля
     * @param password Пароль пользователя
     * @return Возвращает токен пользователю
     */
    @GET
    @Path("getToken/{name}/{password}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getToken(@PathParam("name") String name,
            @PathParam("password") String password) {
        Map<String, Object> jsMap = new HashMap<String, Object>();
        try {
            password = hash(password);
            Users currUser = userFacade.getUserByNameAndPass(name, password);
            if (currUser == null) {
                jsMap.put("isLogged", false);
                jsMap.put("msg", "Incorrect login or password");
                return Response.ok(jsMap).build();
            }
            jsMap.put("userId", currUser.getId());
            jsMap.put("isLogged", true);
            jsMap.put("role", currUser.getRole());
            jsMap.put("token", userRestController.getJWT(currUser));
            return Response.ok(jsMap).build();

        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
    
    /**
     *
     * @param name Имя пользователя
     * @return Возвращает пользователя по имени
     */
    @GET
    @Path("getUser/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUserByName(@PathParam("name") String name){
        Map<Object, String> response = new HashMap<Object, String>();
        List<Users> user = userFacade.getUserByName(name);
        if (userFacade.getUserByName(name).size() > 0) {
            response.put(name, "Was found");
        } else {
            response.put(name, "Not found");
        }
        return Response.ok(response).build();
    }

    /**
     *
     * @param user Пользователь
     * @return Возвращает Java Web Token для пользователя
     * @throws Exception
     */
    public String getJWT(Users user) throws Exception {
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("userId", user.getId().toString());
        JSONObject jsObjPayLoad = new JSONObject(payloadMap);
        return getJWT(user, jsObjPayLoad);
    }

    /**
     *Генерация токена
     * @param user
     * @param jsObjPayLoad Id пользователя, для которого будет сгенерирован Токен
     * @return Возвращает Java Web Token для пользователя
     * @throws Exception
     */
    public String getJWT(Users user, JSONObject jsObjPayLoad) throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("alg", "RS256");
        headerMap.put("typ", "JWT");
        JSONObject jsObjHeader = new JSONObject(headerMap);
        String header = jsObjHeader.toString();
        byte[] byteArrHeader = header.getBytes();
        Base64.Encoder enc = Base64.getUrlEncoder();
        byteArrHeader = enc.encode(byteArrHeader);
        String encodedHeader = new String(byteArrHeader, Charset.forName("UTF-8"));
        String payload = jsObjPayLoad.toString();
        byte[] byteArrPayload = payload.getBytes();
        byteArrPayload = enc.encode(byteArrPayload);
        String encodedPayload = new String(byteArrPayload, Charset.forName("UTF-8"));
        String encodedMessage = encodedHeader + "." + encodedPayload;
        KeyPair keyPair = null;
        if (applicationController.getJwtKeysMap().containsKey(user.getId())) {
            keyPair = applicationController.getJwtKeysMap().get(user.getId());
        } else {
            keyPair = generateKeyPair();
            applicationController.addJwtKeyToMap(user.getId(), keyPair);
        }
        String signature = signJWT(encodedMessage, keyPair.getPrivate());
        String JWT = encodedMessage + "." + signature;
        return JWT;
    }
/**
 * 
 * @return Открытый и закрытый ключ
 * @throws Exception 
 */
    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    /**
     *Подписывает токен закрытым ключем
     * @param plainText Токен
     * @param privateKey Закрытый ключ
     * @return Возвращает подписанный токен
     * @throws Exception
     */
    public String signJWT(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes("UTF-8"));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }
    
    /**
     *
     * @param JWT Токен
     * @return Возвращает payload - id пользователя
     */
    public JSONObject getJWTPayload(String JWT){
        String[] jwtSplit = JWT.split("\\.");
        Base64.Decoder dec = Base64.getUrlDecoder();
        byte[] decodedByteArr = dec.decode(jwtSplit[1]);
        String decodedPayload = new String(decodedByteArr);
        JSONObject jsonPayload = new JSONObject(decodedPayload);
        return jsonPayload;
    }
    
    /**
     *
     * @param JWT Токен
     * @return Возвращает валидность токена
     * @throws Exception
     */
    public boolean verifyJWT(String JWT) throws Exception {
        try {
            String[] jwtSplit = JWT.split("\\.");
            if (jwtSplit.length < 2) {
                return false;
            }
            Base64.Decoder dec = Base64.getUrlDecoder();
            byte[] decodedByteArr = dec.decode(jwtSplit[1]);
            String decodedPayload = new String(decodedByteArr);
            JSONObject jsonPayload = new JSONObject(decodedPayload);
            Integer userId = Integer.parseInt(jsonPayload.getString("userId"));
            String plainText = jwtSplit[0] + "." + jwtSplit[1];
            String signature = jwtSplit[2];
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            if (!applicationController.getJwtKeysMap().containsKey(userId)) {
                return false;
            }
            publicSignature.initVerify(applicationController.getJwtKeysMap().get(userId).getPublic());
            publicSignature.update(plainText.getBytes("UTF-8"));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            return publicSignature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

}
