package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
@Profile("!test") // Para que no sea del profile test y haya discodancia
public class ConfigSeguridad {

   @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/pistaPadel/auth/register",
                                "/pistaPadel/auth/login",
                                "/pistaPadel/auth/me",
                                "/pistaPadel/auth/logout",
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
