package org.example.iqtestweb.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && "ADMIN".equals(session.getAttribute("userRole"))) {
            return true; // User is an admin, proceed to the controller method
        }
        // User is not an admin or not logged in, redirect to a safe page
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return false; // Stop the request from proceeding
    }
}