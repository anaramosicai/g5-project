package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class BearerTokenFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;

    public BearerTokenFilter(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = UsuarioService.extractToken(authHeader);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Long userId = usuarioService.verifyAndGetUserId(token);
            if (userId != null) {
                Usuario usuario = usuarioService.getUsuarioById(userId);
                if (usuario != null) {
                    List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                    );
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/h2-console");
    }
}
