/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.project.chatmaven;

import com.facade.UserFacade;
import com.models.Users;
import com.restservice.UserRestController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.DispatcherType;

import javax.servlet.FilterChain;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author danil
 */
@WebFilter(filterName = "URLFilter", urlPatterns = {"/*"}, dispatcherTypes = {DispatcherType.REQUEST})
public class UrlFilter implements Filter {

    @Inject
    private UserRestController userRestController;

    @Inject
    private UserFacade userFacade;

    private String token;

    @Override
    public void init(FilterConfig filterConfig) {

    }
    
    /**
     * Фильтрует запросы к серверу.
     * @param req Запрос от клиента
     * @param res Ответ сервера
     * @param chain 
     * @throws IOException
     * @throws ServletException 
     */

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uriWithoutContext = request.getRequestURI();
        Users user = null;
        Cookie[] cookieArr = request.getCookies();
        if (cookieArr == null) {
            if (uriWithoutContext.contains("/res/default")) {
                chain.doFilter(request, response);
                return;
            }
            if (uriWithoutContext.contains("/res/")) {
                response.sendError(403);
                return;
            }
        } else {
            for (Cookie c : cookieArr) {
                if (c.getName().equals("token")) {
                    try {
                        user = (c.getValue() == null || !userRestController.verifyJWT(c.getValue())) ? null : userFacade.findUserByToken(c.getValue());
                    } catch (Exception ex) {
                        user = null;
                    }
                }
            }
            if (user == null) {
                if (uriWithoutContext.contains("/res/default")) {
                    chain.doFilter(request, response);
                    return;
                }
                if (uriWithoutContext.contains("/res/")) {
                    response.sendError(403);
                    return;
                }

            }
        }
        chain.doFilter(request, response);
    }
}

//To change body of generated methods, choose Tools | Templates.

