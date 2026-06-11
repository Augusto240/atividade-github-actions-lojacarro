package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.repository.UserRepository;
import br.org.edu.ifrn.LojaCarro.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtAuthenticationService {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getUserFromToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            return Optional.empty();
        }
        String email = tokenProvider.getEmailFromToken(token);
        return userRepository.findByEmail(email);
    }
}
