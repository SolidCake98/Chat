/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restservice;

import com.facade.FileFacade;
import com.facade.FileTypeFacade;
import com.facade.UserFacade;
import com.models.File;
import com.models.FileRQ;
import com.models.Users;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import my.project.chatmaven.ApplicationController;

/**
 *
 * @author danil
 */
@Stateless
@Path("file")
public class FileRestController {

    @Inject
    ApplicationController applicationController;

    @Inject
    FileFacade fileFacade;
    @Inject
    FileTypeFacade fileTypeFacade;
    @Inject
    UserFacade userFacade;
    @Inject
    UserRestController userRestController;
    private final String urlPrefix = "/res/";

    /**
     * Сохараняет файл на сервер
     *
     * @param filerq Модель файла для сохранения
     * @param token Токен
     * @return Возвращает модель файла
     * @throws ParseException
     * @throws IOException
     * @throws Exception
     */
    @POST
    @Path("saveFile")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public File saveFileFromJS(FileRQ filerq, @HeaderParam("token") String token) throws ParseException, IOException, Exception {
        if (token.isEmpty() || !userRestController.verifyJWT(token)) {
            return null;
        }
        Users ur = userFacade.findUserByToken(token);
        File file = null;
        if (filerq.getAvatar() != null && filerq.getAvatar()) {
            return saveCropImage(filerq, ur);
        } else {
            return saveFile(filerq, filerq.getFileType(), ur);
        }
    }

    private String getFilePathToSave(String fName, Users user) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd/");
        String date = df.format(new Date());
        String d = user == null ? "unknown" : user.getId().toString();
        String dirPath = urlPrefix + date + d;
        java.io.File theDir = new java.io.File(applicationController.getFILE_PREFIX() + dirPath);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            try {
                theDir.mkdirs();
            } catch (SecurityException se) {
                //handle it
            }
        }   //        int i = 0;
        boolean bool = true;
        java.io.File outFile = new java.io.File(applicationController.getFILE_PREFIX() + dirPath + "/" + fName);
        while (outFile.exists()) {
            fName = "_" + fName;
            outFile = new java.io.File(applicationController.getFILE_PREFIX() + dirPath + "/" + fName);
        }
        return dirPath + "/" + fName;
    }

    /**
     * Создание сущности для сохранения файла
     *
     * @param fname Имя файла
     * @param ftype Тип файла
     * @param u Пользователь,сохранивший файл
     * @return Возвращает модель файла
     */
    public File getEntityForFile(String fname, int ftype, Users u) {
        File f = new File();
        f.setTitle(fname);
        f.setPath(getFilePathToSave(fname, u));
        f.setType(fileTypeFacade.find(ftype));
        return f;
    }

    /**Сохранение модели Файл в бд и файла на сервере
     *
     * @param file Модель файл
     * @param fileType Тип файла
     * @param user Пользователь, который сохраняет чат
     * @return Возвращает модель файл
     * @throws ParseException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public com.models.File saveFile(FileRQ file, Integer fileType, Users user) throws ParseException, FileNotFoundException, IOException {
        com.models.File fileEntity
                = getEntityForFile(file.getFileName(), fileType, user);
        String fileName = applicationController.getFILE_PREFIX() + fileEntity.getPath();
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(Base64.getDecoder().decode(file.getFileBase64()));
            fileFacade.create(fileEntity);
            return fileEntity;
        } catch (Exception e) {
            return null;
        }
    }

    /**Сохранение модели Файл в бд и картинки на сервере
     *
     *
     * @param file Модель файл
     * @param user Пользователь, который сохраняет чат
     * @return Возвращает модель файл
     * @throws ParseException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public com.models.File saveCropImage(FileRQ file, Users user) throws ParseException, FileNotFoundException, IOException {
        com.models.File fileEntity
                = getEntityForFile(file.getFileName(), file.getFileType(), user);
        String fileName = applicationController.getFILE_PREFIX() + fileEntity.getPath();

        BufferedImage image = null;
        byte[] imageByte;
        imageByte = Base64.getDecoder().decode(file.getFileBase64());
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();
        double scale = file.getScale();
        double x1 = file.getX1() * scale;
        double width = (file.getX2() - file.getX1()) * scale;
        if (width > image.getWidth()) {
            width = image.getWidth();
        }
        double y1 = file.getY1() * scale;
        double height = (file.getY2() - file.getY1()) * scale;
        if (height > image.getHeight()) {
            height = image.getHeight();
        }
        image = image.getSubimage((int) Math.floor(x1), (int) Math.floor(y1), (int) Math.floor(width), (int) Math.floor(height));
        image = resize(image, 500, 500);

        java.io.File outputFile = new java.io.File(fileName);
        ImageIO.write(image, fileName.split("\\.")[fileName.split("\\.").length - 1], outputFile);
        fileFacade.create(fileEntity);
        return fileEntity;
    }

    /**
     * Обрезает картинку
     *
     *
     * @param img Изображения
     * @param newW Новая ширина изображения
     * @param newH Новая высота изображения
     * @return
     */
    public BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
