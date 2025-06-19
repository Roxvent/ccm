package cl.intelidata.security.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import cl.intelidata.security.config.PropertiesConfig;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Arrays;

import java.util.Date;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public CustomTokenEnhancer tokenEnhancer() {
		return new CustomTokenEnhancer();
	}

	@Autowired
	private DataSource dataSource;

	// Para Usuarios de BD
	@Autowired
	private UserDetailsService userDetailsService;

	// Para encriptar
	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bcrypt);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.httpBasic().realmName(propertiesConfig.getSecurityRealm()).and().csrf().disable() 	// Desabilitar csrf para
				// trabajar con token JWT
				.headers()
				.frameOptions()
				.deny()
				.xssProtection()
				.block(true);
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey(propertiesConfig.getSigningKey());
		return converter;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
		// return new JdbcTokenStore(this.dataSource);
	}

	@Bean
	public TokenEnhancerChain tokenEnhancerChain() {
		TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
		enhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter(), tokenEnhancer()));
		return enhancerChain;
	}

	/*@Bean
	public TokenEnhancer jwtTokenEnhancer() {
		return (accessToken, authentication) -> {

			((DefaultOAuth2AccessToken) accessToken).setExpiration(new Date(System.currentTimeMillis() + 3600));

			return accessToken;
		};
	}*/

	@Bean
	@Primary // Crea el bean primero
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		defaultTokenServices.setReuseRefreshToken(false);
		defaultTokenServices.setTokenEnhancer(tokenEnhancerChain());
		defaultTokenServices.setAccessTokenValiditySeconds( propertiesConfig.getJwtTokenExpiration());
		defaultTokenServices.setRefreshTokenValiditySeconds(propertiesConfig.getJwtRefreshTokenExpiration());
		return defaultTokenServices;
	}
}
