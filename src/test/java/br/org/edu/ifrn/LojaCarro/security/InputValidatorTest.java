package br.org.edu.ifrn.LojaCarro.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputValidatorTest {

    private final InputValidator validator = new InputValidator();

    @Test
    void sanitizeDeveRemoverTagsHtmlEScript() {
        String resultado = validator.sanitize("<script>alert(1)</script>Toyota");
        assertFalse(resultado.toLowerCase().contains("<script"));
        assertTrue(resultado.contains("Toyota"));
    }

    @Test
    void sanitizeDeveRemoverAtributosPerigosos() {
        assertFalse(validator.sanitize("javascript:alert(1)").contains("javascript:"));
        assertFalse(validator.sanitize("onerror=alert(1)").contains("onerror="));
        assertFalse(validator.sanitize("onload=alert(1)").contains("onload="));
    }

    @Test
    void sanitizeDeveRetornarNullParaEntradaNull() {
        assertNull(validator.sanitize(null));
    }

    @Test
    void sanitizeDeveAparartrimarEspacos() {
        assertEquals("Gol", validator.sanitize("  Gol  "));
    }

    @Test
    void validateFieldLengthDeveLancarQuandoExcede255Caracteres() {
        String valorGrande = "a".repeat(256);
        InputValidator.ValidationException ex = assertThrows(InputValidator.ValidationException.class,
                () -> validator.validateFieldLength(valorGrande, "marca"));
        assertEquals(422, ex.getStatusCode());
    }

    @Test
    void validateFieldLengthDevePassarParaValorDentroDoLimite() {
        assertDoesNotThrow(() -> validator.validateFieldLength("Toyota", "marca"));
    }

    @Test
    void validateNotEmptyDeveLancarParaNuloOuVazio() {
        InputValidator.ValidationException ex1 = assertThrows(InputValidator.ValidationException.class,
                () -> validator.validateNotEmpty(null, "senha"));
        assertEquals(400, ex1.getStatusCode());

        InputValidator.ValidationException ex2 = assertThrows(InputValidator.ValidationException.class,
                () -> validator.validateNotEmpty("   ", "senha"));
        assertEquals(400, ex2.getStatusCode());
    }

    @Test
    void validateNotEmptyDevePassarParaValorPreenchido() {
        assertDoesNotThrow(() -> validator.validateNotEmpty("senha123", "senha"));
    }

    @Test
    void validateEmailDeveLancarParaFormatoInvalido() {
        assertThrows(InputValidator.ValidationException.class, () -> validator.validateEmail("nao-e-email"));
        assertThrows(InputValidator.ValidationException.class, () -> validator.validateEmail(null));
    }

    @Test
    void validateEmailDevePassarParaFormatoValido() {
        assertDoesNotThrow(() -> validator.validateEmail("usuario@teste.com"));
    }
}
