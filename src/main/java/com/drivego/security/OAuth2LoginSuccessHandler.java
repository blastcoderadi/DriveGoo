package com.drivego.security;

import com.drivego.model.User;
import com.drivego.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Handles successful Google OAuth2 login.
 * Creates or loads the user, stores them in session, then redirects by role.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        // Find or create user (Customer or Admin based on email config)
        User user = userService.findOrCreateGoogleUser(email, name);

        // Store user in HTTP session for use in controllers
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        session.setAttribute("currentUserId", user.getId());

        // Redirect based on role
        String redirectUrl = "ADMIN".equals(user.getRole()) ? "/admin/dashboard" : "/";
        response.sendRedirect(redirectUrl);
    }
}
