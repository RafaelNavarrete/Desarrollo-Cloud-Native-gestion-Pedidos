package com.duoc.gestion_pedidos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de la aplicación.
 * Valida tokens JWT emitidos por Azure AD B2C y protege
 * los endpoints según el custom claim "extension_guiaRole":
 *   - rol_descarga  → solo puede usar GET /guias/descargar
 *   - rol_admin     → puede usar todos los demás endpoints
 *
 * @author Rafael Navarrete
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Se setea desde application.properties con el valor del issuer de Azure AD B2C
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // Se setea desde application.properties con el jwks_uri de Azure AD B2C
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Sin estado: no se crean sesiones Http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Deshabilitar CSRF (API REST sin estado)
            .csrf(csrf -> csrf.disable())
            //Reglas de autorización para los endpoints
            .authorizeHttpRequests(auth -> auth
                // Solo rol_descarga (y rol_admin) pueden descargar guías
                .requestMatchers("/guias/descargar").hasAnyAuthority("ROLE_rol_descarga", "ROLE_rol_admin")
                // Solo rol_admin puede usar el resto de endpoints
                .requestMatchers("/guias/**").hasAuthority("ROLE_rol_admin")
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            // Configurar como Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();    
    }


    /**
     * Decoder JWT que valida firma (via jwks_uri) e issuer del token.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        // Validar que el issuer del token coincida con el de Azure AD B2C
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(issuerValidator);
        decoder.setJwtValidator(combinedValidator);

        return decoder;
    }
    
    /**
     * Convierte el custom claim "extension_guiaRole" del JWT en authorities de Spring Security.
     * El prefijo ROLE_ permite usar hasAuthority("ROLE_rol_admin") en las reglas.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Leer el custom claim de Azure AD B2C como fuente de roles
        authoritiesConverter.setAuthoritiesClaimName("extension_guiaRole");
        // Prefijo ROLE_ para que funcione con hasAuthority
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

}