package cl.intelidata.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import cl.intelidata.ccm2.security.config.DataSourceSecurityConfiguration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Import(DataSourceSecurityConfiguration.class)
@Getter
@Setter
@PropertySource(value = { "file:${properties.external.path}" })
public class PropertiesConfig {

	@Value("${security.signing-key}")
	private String signingKey;

	@Value("${security.encoding-strength}")
	private String encodingStrength;

	@Value("${security.security-realm}")
	private String securityRealm;

	@Value("${security.jwt.client-id}")
	private String jwtClientId;

	@Value("${security.jwt.client-secret}")
	private String jwtClientSecret;

	@Value("${security.jwt.resource-ids}")
	private String jwtResourceIds;

	@Value("${security.jwt.grant-type}")
	private String jwtGrantType;

	@Value("${security.jwt.scope-read}")
	private String jwtScopeRead;

	@Value("${security.jwt.scope-write}")
	private String jwtScopeWrite;

	@Value("${security.jwt.token-time}")
	private int jwtTokenTime;

	@Value("${security.jwt.token-expiration}")
	private int jwtTokenExpiration;

	@Value("${security.jwt.refresh-token-expiration}")
	private int jwtRefreshTokenExpiration;

}
