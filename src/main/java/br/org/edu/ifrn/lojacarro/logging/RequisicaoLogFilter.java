package br.org.edu.ifrn.lojacarro.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequisicaoLogFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequisicaoLogFilter.class);
    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");
    private static final Pattern NUMERO = Pattern.compile("\\d{1,18}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        String requestId = resolverRequestId(request.getHeader(HEADER_REQUEST_ID));
        String rota = montarRota(request);

        MDC.put("requestId", requestId);
        String usuarioId = request.getHeader("X-Usuario-Id");
        if (usuarioId != null && NUMERO.matcher(usuarioId).matches()) {
            MDC.put("usuario", "#" + usuarioId);
        }
        response.setHeader(HEADER_REQUEST_ID, requestId);
        log.info("--> {} de {}", rota, request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            registrarResposta(rota, response.getStatus(), System.currentTimeMillis() - inicio);
            MDC.clear();
        }
    }

    private void registrarResposta(String rota, int status, long duracao) {
        if (status >= 500) {
            log.error("<-- {} respondeu {} em {} ms", rota, status, duracao);
        } else if (status >= 400) {
            log.warn("<-- {} respondeu {} em {} ms", rota, status, duracao);
        } else {
            log.info("<-- {} respondeu {} em {} ms", rota, status, duracao);
        }
    }

    private String resolverRequestId(String recebido) {
        if (recebido != null && ID_VALIDO.matcher(recebido).matches()) {
            return recebido;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String montarRota(HttpServletRequest request) {
        String query = request.getQueryString();
        String caminho = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
        return request.getMethod() + " " + caminho;
    }
}
