package cl.intelidata.security.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import cl.intelidata.ccm2.security.entity.Usuario;
import cl.intelidata.ccm2.security.repository.IUsuarioDAO;

public class CustomTokenEnhancer implements TokenEnhancer {

	@Autowired
	private IUsuarioDAO dao;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		String username;
		Object principal = authentication.getPrincipal();

		// Detecta si el principal es User o String
		if (principal instanceof User) {
			username = ((User) principal).getUsername();
		} else if (principal instanceof String) {
			username = (String) principal;
		} else {
			throw new IllegalArgumentException("Tipo de principal no soportado: " + principal.getClass());
		}

		Usuario userEntity = dao.findUsuario(username, "1");
		final Map<String, Object> additionalInfo = new HashMap<>();
		additionalInfo.put("departamento", userEntity.getDepartamento().getIdDepartamento());
		additionalInfo.put("area", userEntity.getDepartamento().getArea().getIdArea());
		additionalInfo.put("empresa", userEntity.getDepartamento().getArea().getEmpresa().getIdEmpresa());
		additionalInfo.put("idUsuario", userEntity.getIdUsuario());


		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

		return accessToken;
	}
}
