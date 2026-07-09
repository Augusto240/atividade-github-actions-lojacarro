package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.model.User;
import br.org.edu.ifrn.lojacarro.model.UserRole;
import br.org.edu.ifrn.lojacarro.repository.UserRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InputValidator validator;

    @InjectMocks
    private UserService userService;

    @Test
    void registerDeveSalvarUsuarioComSenhaCodificadaERoleCorreta() {
        when(userRepository.findByEmail("novo@teste.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha-encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User resultado = userService.register("novo@teste.com", "senha123", "GERENTE");

        assertEquals("novo@teste.com", resultado.getEmail());
        assertEquals("senha-encoded", resultado.getPassword());
        assertEquals(UserRole.GERENTE, resultado.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("senha-encoded", captor.getValue().getPassword());
    }

    @Test
    void registerDeveAceitarRoleEmMinusculas() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User resultado = userService.register("vendedor@teste.com", "senha123", "vendedor");

        assertEquals(UserRole.VENDEDOR, resultado.getRole());
    }

    @Test
    void registerDeveLancarConflitoQuandoEmailJaExiste() {
        when(userRepository.findByEmail("existente@teste.com"))
                .thenReturn(Optional.of(new User("existente@teste.com", "hash", UserRole.VENDEDOR)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.register("existente@teste.com", "senha123", "GERENTE"));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void registerDeveLancarBadRequestQuandoRoleInvalida() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.register("novo@teste.com", "senha123", "SUPERADMIN"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void findByEmailDeveDelegarParaRepository() {
        User user = new User("busca@teste.com", "hash", UserRole.GERENTE);
        when(userRepository.findByEmail("busca@teste.com")).thenReturn(Optional.of(user));

        Optional<User> resultado = userService.findByEmail("busca@teste.com");

        assertTrue(resultado.isPresent());
        verify(userRepository).findByEmail("busca@teste.com");
    }

    @Test
    void verifyPasswordDeveDelegarParaPasswordEncoder() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        assertTrue(userService.verifyPassword("raw", "encoded"));

        when(passwordEncoder.matches("errada", "encoded")).thenReturn(false);
        assertFalse(userService.verifyPassword("errada", "encoded"));
    }
}
