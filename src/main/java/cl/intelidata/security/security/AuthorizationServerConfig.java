package cl.intelidata.security.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import cl.intelidata.security.config.PropertiesConfig;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private DefaultTokenServices tokenServices;

	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;

	@Autowired
	private CustomTokenEnhancer tokenEnhancer;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@Override
	public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
		configurer.inMemory()
				.withClient(propertiesConfig.getJwtClientId())
				.secret(bcrypt.encode(propertiesConfig.getJwtClientSecret()))
				.authorizedGrantTypes("password", "refresh_token")
				.scopes(propertiesConfig.getJwtScopeRead(), propertiesConfig.getJwtScopeWrite())
				.resourceIds(propertiesConfig.getJwtResourceIds());
	}




	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
		enhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter, tokenEnhancer));
		endpoints
				.tokenStore(tokenStore)
				.accessTokenConverter(accessTokenConverter)
				.tokenEnhancer(enhancerChain)
				.authenticationManager(authenticationManager)
				.tokenServices(tokenServices);
	}

}
