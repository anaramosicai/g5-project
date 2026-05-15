package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
@Profile("!test") // Para que no sea del profile test y haya discodancia
public class ConfigSeguridad {

    @Autowired
    @Lazy
    private UsuarioService usuarioService;

   @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new BearerTokenFilter(usuarioService), UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(
                                "/pistaPadel/auth/register",
                                "/pistaPadel/auth/login",
                                "/pistaPadel/auth/me",
                                "/pistaPadel/auth/logout",
                                "/pistaPadel/users",
                                "/pistaPadel/users/{userId}",
                                "/pistaPadel/reservations",
                                "/pistaPadel/reservations/{reservationId}",
                                "/pistaPadel/admin/reservations",
                                "/pistaPadel/availability",
                                "/pistaPadel/courts/{courtId}/availability"

                        ).permitAll()
                        .requestMatchers("/pistaPadel/health").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService usuarios() {
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

        return new InMemoryUserDetailsManager(user, admin);
    }
}
