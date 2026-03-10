package org.isfce.pid.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de la sécurité OAuth2/Keycloak et CORS.
 * @author Ludovic
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
	private String keySetUri;

	@Value("${app.cors.allowed-origins}")
	private String corsAllowedOrigins;

	private final KeycloakJwtConverter converter;

	// constructeur qui injecte le convertisseur jwt vers security context
	public SecurityConfig(KeycloakJwtConverter converter) {
		super();
		this.converter = converter;
	}

	@Bean
	@Profile("dev")
	@Order(0)
	SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/h2/**")
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.csrf(AbstractHttpConfigurer::disable)
			.headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
		return http.build();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.csrf(c -> c.disable());
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.oauth2ResourceServer(
					c-> c.jwt(j ->j.jwkSetUri(keySetUri).jwtAuthenticationConverter(converter)));
		http.authorizeHttpRequests(
				c -> c.requestMatchers("/api/ue/sections","/api/ue/liste","/api/ue/detail/*","/api/ecole","/api/ecole/*/cours")
				                      .hasAnyRole("ETUDIANT","ADMIN")
				.anyRequest().authenticated());

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
		configuration.setExposedHeaders(Arrays.asList("Content-Disposition"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
