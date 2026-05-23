package kz.gamematch.security;

import kz.gamematch.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/logout", "/css/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/exports/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/requests/inactive").hasRole("ADMIN")

                        .requestMatchers("/api/player/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/profiles/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/games/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/requests/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/responses/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/teams/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/chat/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/matches/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/ratings/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/blacklist/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/exports/me/**").hasAnyRole("PLAYER", "ADMIN")

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }
}
