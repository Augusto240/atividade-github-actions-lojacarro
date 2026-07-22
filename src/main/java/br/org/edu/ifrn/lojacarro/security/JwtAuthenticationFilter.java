package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService authenticationService;
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtAuthenticationService authenticationService, JwtTokenProvider tokenProvider) {
        this.authenticationService = authenticationService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        if (isPublicEndpoint(requestPath, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || bearerToken.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - Missing authorization header\"}");
            return;
        }

        String token = tokenProvider.extractToken(bearerToken);

        Optional<User> user = authenticationService.getUserFromToken(token);

        if (user.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid token\"}");
            return;
        }

        request.setAttribute("user", user.get());
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath, String method) {
        return requestPath.equals("/auth/login")
                || requestPath.equals("/auth/register")
                || requestPath.equals("/boas-vindas")
                || HttpMethod.OPTIONS.matches(method);
    }
}
