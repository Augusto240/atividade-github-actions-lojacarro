package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.IntegridadeAuditoriaResponse;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.repository.RegistroAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuditoriaService {

    static final String HASH_INICIAL = "0".repeat(64);

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final Logger trilha = LoggerFactory.getLogger("AUDITORIA");

    private final RegistroAuditoriaRepository repository;

    public AuditoriaService(RegistroAuditoriaRepository repository) {
        this.repository = repository;
    }

    public synchronized RegistroAuditoria registrar(AcaoAuditoria acao, String responsavel, String detalhes) {
        String hashAnterior = repository.findTopByOrderByIdDesc()
                .map(RegistroAuditoria::getHash)
                .orElse(HASH_INICIAL);
        LocalDateTime agora = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        String quem = responsavel == null || responsavel.isBlank() ? "anonimo" : responsavel;
        String texto = detalhes == null ? "" : detalhes;

        String hash = calcularHash(hashAnterior, agora, acao.name(), quem, texto);
        RegistroAuditoria salvo = repository.save(
                new RegistroAuditoria(agora, acao.name(), quem, texto, hashAnterior, hash));

        trilha.info("id={} acao={} responsavel=\"{}\" detalhes=\"{}\" hash={} anterior={}",
                salvo.getId(), acao, quem, texto, hash, hashAnterior);
        return salvo;
    }

    public List<RegistroAuditoria> listarRecentes() {
        List<RegistroAuditoria> registros = repository.findTop200ByOrderByIdDesc();
        log.info("Consulta na trilha de auditoria devolveu {} registro(s)", registros.size());
        return registros;
    }

    public IntegridadeAuditoriaResponse verificarIntegridade() {
        List<RegistroAuditoria> registros = repository.findAllByOrderByIdAsc();
        String esperado = HASH_INICIAL;

        for (RegistroAuditoria registro : registros) {
            String recalculado = calcularHash(registro.getHashAnterior(), registro.getDataHora(),
                    registro.getAcao(), registro.getResponsavel(), registro.getDetalhes());

            if (!registro.getHashAnterior().equals(esperado) || !recalculado.equals(registro.getHash())) {
                log.error("Trilha de auditoria ADULTERADA a partir do registro {} ({} registros verificados)",
                        registro.getId(), registros.size());
                return IntegridadeAuditoriaResponse.adulterada(registros.size(), registro.getId());
            }
            esperado = registro.getHash();
        }

        log.info("Trilha de auditoria integra, {} registro(s) conferidos", registros.size());
        return IntegridadeAuditoriaResponse.integra(registros.size());
    }

    static String calcularHash(String hashAnterior, LocalDateTime dataHora, String acao, String responsavel,
            String detalhes) {
        String conteudo = String.join("|", hashAnterior, dataHora.toString(), acao, responsavel, detalhes);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
        }
    }
}
