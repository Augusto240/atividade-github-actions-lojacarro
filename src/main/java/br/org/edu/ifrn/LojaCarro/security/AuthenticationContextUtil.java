package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContextUtil {

    public User getLoggedUser(HttpServletRequest request) {
        Object userAttribute = request.getAttribute("user");
        if (userAttribute instanceof User) {
            return (User) userAttribute;
        }
        return null;
    }
}

