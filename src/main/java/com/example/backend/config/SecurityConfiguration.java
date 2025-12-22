package com.example.backend.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.example.backend.config.security.AccessTokenValidatorGrantedAuthoritiesConverter;
import com.example.backend.util.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

        @Value("${backend.jwt.base64-secret}")
        private String jwtKey;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(
                        HttpSecurity http,
                        CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

                String[] whiteList = {
                                "/",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/register",
                                "/api/v1/files/**",
                                "/storage/**",
                };

                http
                                .csrf(c -> c.disable())
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(
                                                authz -> authz
                                                                .requestMatchers(whiteList)
                                                                .permitAll()
                                                                // .anyRequest().authenticated())
                                                                .anyRequest().permitAll())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .decoder(jwtDecoder())
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter()))
                                                .authenticationEntryPoint(customAuthenticationEntryPoint))
                                .formLogin(f -> f.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                // chỉ cần pass converter instance, Spring sẽ gọi convert()
                converter.setJwtGrantedAuthoritiesConverter(
                                new AccessTokenValidatorGrantedAuthoritiesConverter()::convert);
                return converter;
        }

        @Bean
        public JwtDecoder jwtDecoder() {

                NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                                .build();

                // validate exp, nbf, iat
                decoder.setJwtValidator(JwtValidators.createDefault());
                return decoder;
        }

        @Bean
        public JwtEncoder jwtEncoder() {
                return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
        }

        private SecretKey getSecretKey() {
                byte[] keyBytes = Base64.from(jwtKey).decode();
                return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                                SecurityUtil.JWT_ALGORITHM.getName());
        }

}