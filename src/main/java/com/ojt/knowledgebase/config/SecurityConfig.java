package com.ojt.knowledgebase.config;

import com.ojt.knowledgebase.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }



    // PASSWORD ENCODER


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }



    // SECURITY FILTER


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // REST API + JWT
                .csrf(
                        csrf ->
                                csrf.disable()
                )



                // STATELESS SESSION


                .sessionManagement(

                        session ->

                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )



                // AUTHORIZATION


                .authorizeHttpRequests(

                        auth -> auth

                                // PUBLIC APIs
                                .requestMatchers(

                                        "/api/auth/**",

                                        "/api/test",

                                        "/error",

                                        "/swagger-ui/**",

                                        "/swagger-ui.html",

                                        "/v3/api-docs/**"

                                )

                                .permitAll()


                                // EVERYTHING ELSE REQUIRES JWT
                                .anyRequest()

                                .authenticated()
                )



                // JWT FILTER


                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}