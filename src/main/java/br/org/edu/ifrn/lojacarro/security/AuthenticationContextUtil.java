package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContextUtil {

    public User getLoggedUser(HttpServletRequest request) {
        Object userAttribute = request.getAttribute("user");
        if (userAttribute instanceof User user) {
            return user;
        }
        return null;
    }
}

