package com.geosinergia.gestiontareas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/img/**",
                                "/images/**",
                                "/error",
                                "/favicon.ico")
                        .permitAll()

                        .requestMatchers("/colaboradores/**")
                        .hasRole("COORDINADOR")

                        .requestMatchers("/tareas/nueva")
                        .hasRole("COORDINADOR")

                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acceso-denegado")
                )

                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .usernameParameter("correo")

                        .passwordParameter("password")

                        .defaultSuccessUrl("/", true)

                        .failureHandler((request, response, exception) -> {

                            String mensajeError;

                            if (exception instanceof DisabledException) {

                                mensajeError =
                                        "Tu cuenta se encuentra inactiva. "
                                        + "Comunícate con el coordinador "
                                        + "para solicitar acceso.";

                            } else {

                                mensajeError =
                                        "Correo o contraseña incorrectos.";
                            }

                            request.getSession(true)
                                    .setAttribute(
                                            "loginError",
                                            mensajeError);

                            response.sendRedirect(
                                    request.getContextPath()
                                            + "/login");
                        })

                        .permitAll()
                )

                .logout(logout -> logout

                        .logoutSuccessHandler(
                                (request,
                                 response,
                                 authentication) -> {

                                    request.getSession(true)
                                            .setAttribute(
                                                "logoutSuccess",
                                                true);

                                    response.sendRedirect(
                                            request.getContextPath()
                                                + "/login");
                                })

                        .permitAll()
                );

        return http.build();
    }
}