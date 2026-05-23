package com.aibert.dosw.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");

        if (userId != null && !userId.isBlank()) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authenticated request with X-User-Id header for path={}", request.getRequestURI());
        } else {

            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring(7);
                    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                            .parseClaimsJws(token).getBody();
                    String email = claims.getSubject();
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Authenticated request with Bearer token for path={}", request.getRequestURI());
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    log.warn("JWT authentication failed for path={}: {}", request.getRequestURI(), e.getMessage());
                }
            } else {
                log.debug("No authentication headers found for path={}", request.getRequestURI());
            }
        }
        chain.doFilter(request, response);
    }
}
