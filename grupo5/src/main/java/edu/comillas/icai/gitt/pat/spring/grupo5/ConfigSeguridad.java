package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigSeguridad {
    @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                // Para API: puedes desactivar CSRF completamente o restringirlo a tu ruta de API
                .csrf(csrf -> csrf.disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/pistaPadel/**"))

                .authorizeHttpRequests(auth -> auth
                        // === ENDPOINTS PÚBLICOS (POST - registro, GET - healthcheck)===
                        .requestMatchers("/pistaPadel/auth/register").permitAll()
                        .requestMatchers("/pistaPadel/health").permitAll()

                        // === LO DEMÁS PROTEGIDO ===
                        .anyRequest().authenticated()
                )

                // === MANEJO DE ERRORES 401 Y 403 ===
                // ¿Qué sucede cuando...? (Aplicado a todos los endpoint protegidos)
                .exceptionHandling(ex -> ex
                        // Usuario NO autenticado → 401
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 - No autenticado") )
                        // Usuario autenticado pero sin permisos → 403
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        "403 - Acceso denegado")
                        )
                )

                // httpBasic y/o formLogin para probar rápidamente
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean public UserDetailsService usuarios() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("usuario")
                .password("clave")
                .roles("USER")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("clave")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user,admin);
    }

}
