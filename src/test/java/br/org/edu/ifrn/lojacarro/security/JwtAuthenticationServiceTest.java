package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.User;
import br.org.edu.ifrn.lojacarro.model.UserRole;
import br.org.edu.ifrn.lojacarro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationService authenticationService;

    @Test
    void getUserFromTokenDeveRetornarVazioQuandoTokenInvalido() {
        when(tokenProvider.validateToken("token-invalido")).thenReturn(false);

        Optional<User> resultado = authenticationService.getUserFromToken("token-invalido");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void getUserFromTokenDeveRetornarUsuarioQuandoTokenValidoEUsuarioExiste() {
        User user = new User("gerente@teste.com", "hash", UserRole.GERENTE);

        when(tokenProvider.validateToken("token-valido")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("token-valido")).thenReturn("gerente@teste.com");
        when(userRepository.findByEmail("gerente@teste.com")).thenReturn(Optional.of(user));

        Optional<User> resultado = authenticationService.getUserFromToken("token-valido");

        assertTrue(resultado.isPresent());
    }

    @Test
    void getUserFromTokenDeveRetornarVazioQuandoTokenValidoMasUsuarioNaoExiste() {
        when(tokenProvider.validateToken("token-valido")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("token-valido")).thenReturn("fantasma@teste.com");
        when(userRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        Optional<User> resultado = authenticationService.getUserFromToken("token-valido");

        assertTrue(resultado.isEmpty());
    }
}
