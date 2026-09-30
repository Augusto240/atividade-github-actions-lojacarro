package br.org.edu.ifrn.lojacarro.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class LogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requisicaoSemRequestIdGanhaUmNovo() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/boas-vindas"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(resultado.getResponse().getHeader("X-Request-Id")).hasSize(8);
    }

    @Test
    void requestIdComCaracterEstranhoEhTrocadoPorUmNovo() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/boas-vindas").header("X-Request-Id", "id com espaco; drop"))
                .andReturn();

        assertThat(resultado.getResponse().getHeader("X-Request-Id")).hasSize(8).doesNotContain(" ");
    }

    @Test
    void cadaRequisicaoGeraLogDeEntradaESaidaComStatus(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/usuarios/987654").header("X-Request-Id", "log-404"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Request-Id", "log-404"));

        assertThat(output)
                .contains("--> GET /usuarios/987654")
                .contains("Usuario 987654 nao encontrado")
                .contains("<-- GET /usuarios/987654 respondeu 404");
    }

    @Test
    void erroDoFrontendChegaNoLogDoServidor(CapturedOutput output) throws Exception {
        enviarLogDoFrontend("{\"nivel\":\"error\",\"mensagem\":\"TypeError: x is undefined\",\"pagina\":\"/usuarios\"}");
        enviarLogDoFrontend("{\"nivel\":\"WARN\",\"mensagem\":\"API lenta\",\"pagina\":\"/carros\"}");
        enviarLogDoFrontend("{\"mensagem\":\"" + "x".repeat(600) + "\"}");

        assertThat(output)
                .contains("FRONTEND")
                .contains("[/usuarios] TypeError: x is undefined")
                .contains("[/carros] API lenta")
                .contains("[-] " + "x".repeat(500) + "...");
    }

    @Test
    void quebraDeLinhaNaoForjaLinhaNovaNoLog(CapturedOutput output) throws Exception {
        enviarLogDoFrontend("{\"nivel\":\"INFO\",\"mensagem\":\"linha1\\nERROR falso\",\"pagina\":\"/x\"}");

        assertThat(output).contains("linha1 ERROR falso").doesNotContain("linha1\nERROR falso");
    }

    @Test
    void rotaInexistenteDa404EMetodoErradoDa405() throws Exception {
        mockMvc.perform(get("/auditoria/nada/aqui")).andExpect(status().isNotFound());
        mockMvc.perform(patch("/usuarios")).andExpect(status().isMethodNotAllowed());
    }

    private void enviarLogDoFrontend(String json) throws Exception {
        mockMvc.perform(post("/logs").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNoContent());
    }
}
