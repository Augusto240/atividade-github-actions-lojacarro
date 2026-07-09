package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.repository.UserRepository;
import br.org.edu.ifrn.lojacarro.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtAuthenticationService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationService(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public Optional<User> getUserFromToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            return Optional.empty();
        }
        String email = tokenProvider.getEmailFromToken(token);
        return userRepository.findByEmail(email);
    }
}
