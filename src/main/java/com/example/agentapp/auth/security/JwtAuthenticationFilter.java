package com.example.agentapp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// Her API isteğinde JWT token'ı kontrol etmek
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Forward/include olsa bile filtre SADECE 1 KEZ çalışır!
    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal( // ana method controllerdan önce burası çalışır
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // jwt tokenı headerdan al
            String jwt = getJwtFromRequest(request);

            // token var mı ve geçerli mi?
            if (StringUtils.hasText(jwt)) {

                // Token'dan username'i çıkar
                String username = jwtService.extractUsername(jwt);

                // önceden kalma bir authentication var mı?
                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    // db den userı yükle
                    UserDetails userDetails = customUserDetailsService
                            .loadUserByUsername(username);

                    // tokenı doğrula

                    if (jwtService.validateToken(jwt, userDetails.getUsername())) {

                        // authentication oluşturma
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,              // Principal (UserPrincipal)
                                        null,                     // Credentials (şifre yok)
                                        userDetails.getAuthorities() // Authorities (roller + permissions)
                                );

                        // ip adress ve session id ekleme opsiyonel loglama için
                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // SecurityContexte koy
                        SecurityContextHolder.getContext()
                                .setAuthentication(authenticationToken);
                    }
                }
            }

        } catch (Exception ex) {
            // ExpiredJwtException, SignatureException, MalformedJwtException, UsernameNotFoundException hataları olabilir
            logger.error("JWT Token doğrulanamadı", ex);
            // Filter chain'e devam et (401 dönecek)
            // 500 dönmemesi için authenticationa bırakılır
        }

        // sonraki filtre veya controllera geç, filter chain devam eder
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // Authorization header'ı al
        String bearerToken = request.getHeader("Authorization");

        // "Bearer " ile başlıyorsa prefix'i çıkar
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 7 karakter
        }

        // Token yok veya format yanlış
        return null;
    }
}