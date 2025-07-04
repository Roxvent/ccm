package cl.intelidata.security.service.impl;






import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;


import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cl.intelidata.ccm2.security.entity.Rol;
import cl.intelidata.ccm2.security.entity.Servicio;
import cl.intelidata.ccm2.security.entity.Usuario;
import cl.intelidata.ccm2.security.entity.App;
import cl.intelidata.ccm2.security.entity.Departamento;
import cl.intelidata.ccm2.security.projections.UserListProjection;
import cl.intelidata.ccm2.security.projections.DTO.ExternalIdentificationDTO;
import cl.intelidata.ccm2.security.projections.DTO.UserDTO;
import cl.intelidata.ccm2.security.repository.IAppDAO;
import cl.intelidata.ccm2.security.repository.IDepartamentoDAO;
import cl.intelidata.ccm2.security.repository.IRolDAO;
import cl.intelidata.ccm2.security.repository.IServicioDAO;
import cl.intelidata.ccm2.security.repository.IUsuarioDAO;
import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.security.model.api.AuthExtendedWithAzureDTO;
import cl.intelidata.security.model.api.UserModel;
import cl.intelidata.security.model.api.UserModelResponse;
import cl.intelidata.security.model.api.UsuarioListRequest;
import cl.intelidata.security.model.api.UsuarioModel;
import cl.intelidata.security.service.IUsuarioService;
import cl.intelidata.security.util.JwtUtil;
import cl.intelidata.security.util.ResponseApiHandler;
import cl.intelidata.security.util.Regex;
import lombok.extern.slf4j.Slf4j;
import cl.intelidata.security.service.IEmpresaService;
import cl.intelidata.ccm2.security.entity.AuthKey;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@Service("userDetailsService")
@Slf4j
public class UserServiceImpl implements UserDetailsService, IUsuarioService {
        
        @Autowired
        private EntityManager entityManager;
    
    @Autowired
    private IUsuarioDAO dao;

    @Autowired
    private JwtUtil jwtUtil;

	@Autowired
	private IAppDAO daoApp;

	@Autowired
	private IRolDAO daoRol;

	@Autowired
	private IDepartamentoDAO daoDepartamento;

	@Autowired
	private IServicioDAO servicioDao;

	@Autowired
	private IEmpresaService empresaService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = dao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                new ArrayList<>());
    }

    private void setRoles(UsuarioModel user, Usuario o) {
        if (o.getRoles() != null) {
            o.getRoles().clear();
        } else {
            o.setRoles(new ArrayList<>());
        }
        List<Rol> allRoles = daoRol.listarRoles();
        allRoles.forEach(rol ->
                user.getRoles()
                        .stream()
                        .mapToLong(idRol -> idRol)
                        .filter(idRol -> rol.getIdRol() == idRol)
                        .forEach(idRol -> o.getRoles().add(rol)));
    }

    @Override
    public ResponseEntity<?> findServiciosByUsername(String username) {
		Map<String, Object> response = new HashMap<>();
		if(username.isEmpty()){
			response.put("status", HttpStatus.BAD_REQUEST.value());
			response.put("message", "usuario vacio");
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(response);
		}
		Optional<Servicio> optServicio = servicioDao.findServicioByUsername(username);
		if(optServicio.isPresent()){
			response.put("data", optServicio.get());
			response.put("status", HttpStatus.OK.value());
			response.put("message", "OK");
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(response);
		}
		response.put("status", HttpStatus.NOT_FOUND.value());
		response.put("message", "usuario no encontrado");
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(response);
	}

	@Override
	public ResponseEntity<?> findById(long id) {
		Usuario usuario = dao.findById(id).orElse(new Usuario());
		return ResponseApiHandler
				.generateResponse(HttpStatus.OK, usuario);
	}

	@Override
	public ResponseEntity<?> findUsuarioByDepartamento(UsuarioListRequest req) {
		Pageable pagination = PageRequest.of(req.getPage(), req.getSize());
		Page<Usuario> usuarioPage = dao.findUsuariosByDepartamento(req.getIdDepartamento(), pagination);
		return ResponseApiHandler
				.generateResponse(HttpStatus.OK, usuarioPage);
	}

	@Override
	public ResponseEntity<?> findUsuarioByFilters(UsuarioListRequest req) {
		UserModelResponse response = new UserModelResponse();
		Pageable pagination 	= PageRequest.of(req.getPage(), req.getSize());
		Page<UserListProjection> userListProjections 	= dao.listarUsuario(
				Long.valueOf(req.getIdEmpresa()).intValue(),
				Long.valueOf(req.getIdArea()).intValue(),
				Long.valueOf(req.getIdDepartamento()).intValue(),
				pagination);

		List<UserModel> userModelList = userListProjections
				.getContent()
				.stream()
				.map(userProjection -> {
					UserModel um = new UserModel();
					um.setIdUsuario(userProjection.getIdUsuario());
					um.setUsername(userProjection.getUsername());
					um.setEnabled(userProjection.getEnabled());
					return um;
				}).collect(Collectors.toList());

		userModelList.forEach(user -> {
			List<Rol> rolesByUsuarioId = dao.findRolesByUsuarioId(user.getIdUsuario());
			user.setRoles(rolesByUsuarioId);
		});

		response.setUsuarios(userModelList);
		response.setTotal(userListProjections.getTotalElements());
		return ResponseApiHandler
				.generateResponse(HttpStatus.OK, response);
	}

	@Override
	public ResponseEntity<?> findUsuarioByUsername(String username) {
		if(username.isEmpty()){
			return ResponseApiHandler
					.generateResponse(HttpStatus.BAD_REQUEST);
		}
		//Optional<Usuario> usuario = dao.findByUsername(username);
		Optional<UserDTO> byUsernameAsDTO = dao.findByUsernameAsDTO(username);
		return ResponseApiHandler
				.generateResponse(HttpStatus.OK, byUsernameAsDTO);
	}

	@Override
	public AuthDTO getIdentification(String username) {
		try {
			Usuario user = dao.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
			AuthDTO authDTO = new AuthDTO();
			authDTO.setIdEmpresa(user.getDepartamento().getArea().getEmpresa().getIdEmpresa());
			authDTO.setUsername(user.getUsername());
			authDTO.setRoles(user.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList()));
			return authDTO;
		} catch (Exception e) {
			log.error("Error getting identification for user {}: {}", username, e.getMessage());
			return null;
		}
	}

@Override
public ResponseEntity<?> findIdentification(String username) {
    ExternalIdentificationDTO ei = null;
    List<Rol> rolesByUsername = Collections.emptyList();
    
    try {
        // 1. Obtener datos básicos del usuario
        ei = dao.findIdentification(username).orElse(null);
        if (ei == null) {
            return ResponseApiHandler.generateResponse(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        // 2. Obtener roles
        rolesByUsername = dao.findRolesByUsername(username);
        
        // 3. Consulta nativa para obtener azure_ad
        Integer azureAd;
        try {
            azureAd = (Integer) entityManager.createNativeQuery(
                    "SELECT azure_ad FROM bddesseguridad.empresa WHERE id_empresa = :idEmpresa")
                .setParameter("idEmpresa", ei.getIdEmpresa())
                .getSingleResult();
        } catch (NoResultException e) {
            log.warn("No se encontró azure_ad para empresa {}", ei.getIdEmpresa());
            azureAd = 0;
        }

        // 4. Construir respuesta exitosa
        AuthExtendedWithAzureDTO response = new AuthExtendedWithAzureDTO(
            ei.getIdEmpresa(),
            username,
            rolesByUsername.stream().map(Rol::getNombre).collect(Collectors.toList()),
            ei.getIdArea(),
            ei.getIdDepartamento(),
            azureAd
        );

        return ResponseApiHandler.generateResponse(HttpStatus.OK, response);

    } catch (Exception e) {
        log.error("Error en findIdentification: ", e);
        
        // Respuesta de fallback con los datos que tengamos
        Integer fallbackAzureAd = 0;
        Long fallbackIdEmpresa = (ei != null) ? ei.getIdEmpresa() : 0L;
        Long fallbackIdArea = (ei != null) ? ei.getIdArea() : 0L;
        Long fallbackIdDepto = (ei != null) ? ei.getIdDepartamento() : 0L;
        
        AuthExtendedWithAzureDTO errorResponse = new AuthExtendedWithAzureDTO(
            fallbackIdEmpresa,
            username,
            rolesByUsername.stream().map(Rol::getNombre).collect(Collectors.toList()),
            fallbackIdArea,
            fallbackIdDepto,
            fallbackAzureAd
        );
        
        return ResponseApiHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
    }
}

	@Override
	public String authenticate(String username, String password) {
        String dbUsername = username;
        if (username.contains("@")) {
            dbUsername = username.split("@")[0];
        }

        Optional<Usuario> userOptional = dao.findByUsername(dbUsername);

        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + dbUsername);
        }

        Usuario user = userOptional.get();

        if (!user.isEnabled()) {
            throw new DisabledException("Usuario inhabilitado: " + dbUsername);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(password, user.getPassword())) {
            UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
           return jwtUtil.generateToken(userDetails);
        } else {
            throw new BadCredentialsException("Credenciales inválidas para el usuario: " + dbUsername);
        }
    }

	@Override
	public ResponseEntity<?> changePassword(UsuarioModel model, AuthDTO auth) {
		if(auth == null){
			return ResponseApiHandler
					.generateResponse(HttpStatus.BAD_REQUEST);
		}
		boolean isSadmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("SADMIN"));
		if(isSadmin){
			BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
			Optional<Usuario> optUser = dao.findById(model.getIdUsuario());
			if(optUser.isPresent()){
				Usuario user = optUser.get();
				boolean isValidPassword = Regex.VALID_PASSWORD.matcher(model.getPassword()).find();
				if(isValidPassword){
					user.setPassword(bcrypt.encode(model.getPassword()));
					dao.save(user);
					return ResponseApiHandler
							.generateResponse(HttpStatus.CREATED, "valor modificado");
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.BAD_REQUEST, "password invalida");
			}
			return ResponseApiHandler
					.generateResponse(HttpStatus.NOT_FOUND);
		}

		boolean isAdmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("ADMIN"));
		if(isAdmin){
			Long idEmpresa = dao.findEmpresaByUsername(model.getUsername()).orElse(null);
			if (idEmpresa != null && idEmpresa == auth.getIdEmpresa() && auth.getRoles().stream().noneMatch(r -> r.equals("SADMIN"))){
				BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
				Optional<Usuario> optUser = dao.findById(model.getIdUsuario());
				if(optUser.isPresent()){
					Usuario user = optUser.get();
					boolean isValidPassword = Regex.VALID_PASSWORD.matcher(model.getPassword()).find();
					if(isValidPassword){
						user.setPassword(bcrypt.encode(model.getPassword()));
						dao.save(user);
						return ResponseApiHandler
								.generateResponse(HttpStatus.CREATED, "valor modificado");
					}
					return ResponseApiHandler
							.generateResponse(HttpStatus.BAD_REQUEST, "password invalida");
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.NOT_FOUND);
			}
			return ResponseApiHandler
					.generateResponse(HttpStatus.FORBIDDEN,
							"no cumple con los permisos para realizar la operacion");
		}

		Long idEmpresa = dao.findEmpresaByUsername(model.getUsername()).orElse(null);
		if(idEmpresa != null && idEmpresa == auth.getIdEmpresa() && model.getUsername().equals(auth.getUsername())){
			BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
			Optional<Usuario> optUser = dao.findById(model.getIdUsuario());
			if(optUser.isPresent()){
				Usuario user = optUser.get();
				boolean isValidPassword = Regex.VALID_PASSWORD.matcher(model.getPassword()).find();
				if(isValidPassword){
					user.setPassword(bcrypt.encode(model.getPassword()));
					dao.save(user);
					return ResponseApiHandler
							.generateResponse(HttpStatus.CREATED, "valor modificado");
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.BAD_REQUEST, "password invalida");
			}
			return ResponseApiHandler
					.generateResponse(HttpStatus.NOT_FOUND);
		}
		return ResponseApiHandler
				.generateResponse(HttpStatus.FORBIDDEN);
	}

	@Override
	public ResponseEntity<?> create(UsuarioModel model, AuthDTO auth) {
		if(auth == null){
			return ResponseApiHandler
					.generateResponse(HttpStatus.BAD_REQUEST);
		}
		boolean isValidPassword 		= model.getPassword() != null && !model.getPassword().isEmpty() && Regex.VALID_PASSWORD.matcher(model.getPassword()).find();
		boolean isSadmin 				= auth.getRoles().stream().anyMatch(rol -> rol.equals("SADMIN"));
		boolean isAdmin 				= auth.getRoles().stream().anyMatch(rol -> rol.equals("ADMIN"));
		BCryptPasswordEncoder bcrypt 	= new BCryptPasswordEncoder();
		Optional<Usuario> optUser 		= dao.findById(model.getIdUsuario());
		if(!optUser.isPresent()){
			if(isSadmin){
				if (isValidPassword){
					Usuario user = new Usuario();
					user.setUsername(model.getUsername());
					user.setPassword(bcrypt.encode(model.getPassword()));
					user.setApp(daoApp.findById(Long.parseLong("1")).orElseGet(App::new));
					user.setEnabled(model.isEnable());
					user.setDepartamento(daoDepartamento.findById(model.getIdDepartamento()).orElseGet(Departamento::new));
					setRoles(model, user);
					dao.save(user);
					return ResponseApiHandler
							.generateResponse(HttpStatus.CREATED);
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.BAD_REQUEST, "password invalida");
			}
			if(isAdmin){
				Departamento departamento = daoDepartamento.findById(model.getIdDepartamento()).orElse(null);
				if(departamento != null){
					if(auth.getIdEmpresa() == departamento.getArea().getEmpresa().getIdEmpresa() && auth.getRoles().stream().noneMatch(r -> r.equals("SADMIN")) ){
						if (isValidPassword){
							Usuario user = new Usuario();
							user.setUsername(model.getUsername());
							user.setPassword(bcrypt.encode(model.getPassword()));
							user.setApp(daoApp.findById(Long.parseLong("1")).orElseGet(App::new));
							user.setEnabled(model.isEnable());
							user.setDepartamento(daoDepartamento.findById(model.getIdDepartamento()).orElseGet(Departamento::new));
							setRoles(model, user);
							dao.save(user);
							return ResponseApiHandler
									.generateResponse(HttpStatus.CREATED);
						}
					}
					return ResponseApiHandler
							.generateResponse(HttpStatus.FORBIDDEN, "no cumple con los permisos para realizar la operacion");
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.BAD_REQUEST, "departamento no valido");
			}
		}
		return ResponseApiHandler
				.generateResponse(HttpStatus.FORBIDDEN);
	}

	@Override
	public ResponseEntity<?> update(UsuarioModel model, AuthDTO auth) {
		if(auth == null){
			return ResponseApiHandler.generateResponse(HttpStatus.BAD_REQUEST);
		}
		boolean isSadmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("SADMIN"));
		boolean isAdmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("ADMIN"));
		Optional<Usuario> optUser = dao.findById(model.getIdUsuario());
		if(optUser.isPresent()){
			Usuario user = optUser.get();
			if(isSadmin){
				user.setEnabled(model.isEnable());
				user.setUsername(model.getUsername());
				Optional<Departamento> optDepto = daoDepartamento.findById(model.getIdDepartamento());
                optDepto.ifPresent(user::setDepartamento);
				setRoles(model, user);
				dao.save(user);
				return ResponseApiHandler.generateResponse(HttpStatus.CREATED);
			}
			if(isAdmin){
				if(auth.getIdEmpresa() == user.getDepartamento().getArea().getEmpresa().getIdEmpresa() && auth.getRoles().stream().noneMatch(r -> r.equals("SADMIN"))){
					user.setEnabled(model.isEnable());
					user.setUsername(model.getUsername());
					Optional<Departamento> optDepto = daoDepartamento.findById(model.getIdDepartamento());
					optDepto.ifPresent(user::setDepartamento);
					setRoles(model, user);
					dao.save(user);
					return ResponseApiHandler.generateResponse(HttpStatus.CREATED);
				}
			}
			return ResponseApiHandler.generateResponse(HttpStatus.FORBIDDEN);
		}
		return ResponseApiHandler.generateResponse(HttpStatus.NOT_FOUND);
	}
	@Override
	public ResponseEntity<?> enable(long idUser, Boolean enable, AuthDTO auth) {
		if(auth == null){
			return ResponseApiHandler
					.generateResponse(HttpStatus.BAD_REQUEST);
		}

		boolean isSadmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("SADMIN"));
		boolean isAdmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("ADMIN"));

		if(isSadmin){
			dao.enable(enable, idUser);
			return ResponseApiHandler
					.generateResponse(HttpStatus.CREATED);
		}
		if(isAdmin){
			Optional<Usuario> optionalUsuario = dao.findById(idUser);
			if(optionalUsuario.isPresent()){
				if(auth.getIdEmpresa() == optionalUsuario.get().getDepartamento().getArea().getEmpresa().getIdEmpresa()){
					dao.enable(enable, idUser);
					return ResponseApiHandler
							.generateResponse(HttpStatus.CREATED);
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.FORBIDDEN);
			}
			return  ResponseApiHandler
					.generateResponse(HttpStatus.NOT_FOUND);
		}
		return ResponseApiHandler
				.generateResponse(HttpStatus.FORBIDDEN);
	}
	@Override
	public ResponseEntity<?> delete(long idUser, AuthDTO auth) {
		if(auth == null){
			return ResponseApiHandler
					.generateResponse(HttpStatus.BAD_REQUEST);
		}

		boolean isSadmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("SADMIN"));
		boolean isAdmin 	= auth.getRoles().stream().anyMatch(rol -> rol.equals("ADMIN"));

		if(isSadmin){
			Optional<Usuario> optionalUsuario = dao.findById(idUser);
			if(optionalUsuario.isPresent()){
				dao.deleteById(idUser);
				return ResponseApiHandler
						.generateResponse(HttpStatus.CREATED);
			}
			return ResponseApiHandler
					.generateResponse(HttpStatus.NOT_FOUND);
		}
		if(isAdmin){
			Optional<Usuario> optionalUsuario = dao.findById(idUser);
			if(optionalUsuario.isPresent()){
				if(auth.getIdEmpresa() == optionalUsuario.get().getDepartamento().getArea().getEmpresa().getIdEmpresa()){
					dao.deleteById(idUser);
					return ResponseApiHandler
							.generateResponse(HttpStatus.CREATED);
				}
				return ResponseApiHandler
						.generateResponse(HttpStatus.FORBIDDEN);
			}
			return  ResponseApiHandler
					.generateResponse(HttpStatus.NOT_FOUND);
		}
		return ResponseApiHandler
				.generateResponse(HttpStatus.FORBIDDEN);
	}

	@Override
    public String handleAzureCallback(String code, String state) throws Exception {
        Long idEmpresa = Long.parseLong(state);

                ResponseEntity<?> responseEntity = empresaService.findAuthKey(idEmpresa);
        AuthKey authKey = (AuthKey) responseEntity.getBody();
        if (authKey == null) {
            throw new Exception("Azure AD configuration not found for company ID: " + idEmpresa);
        }

        String clientId = authKey.getClientId();
        String clientSecret = authKey.getClientSecret();
        String tenantId = authKey.getTenantId();
        // Asegúrate de que esta URL de redirección esté registrada en tu aplicación de Azure AD
        String redirectUri = "http://localhost:8080/ccm-security/external/azure-callback";

        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String accessToken = rootNode.path("access_token").asText();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("Could not obtain access token from Azure AD.");
        }

        HttpHeaders graphHeaders = new HttpHeaders();
        graphHeaders.setBearerAuth(accessToken);
        HttpEntity<String> graphEntity = new HttpEntity<>(graphHeaders);

        String graphUrl = "https://graph.microsoft.com/v1.0/me";
        ResponseEntity<String> graphResponse = restTemplate.exchange(graphUrl, HttpMethod.GET, graphEntity, String.class);
        JsonNode userNode = objectMapper.readTree(graphResponse.getBody());
        String userPrincipalName = userNode.path("userPrincipalName").asText();

        if (userPrincipalName == null || userPrincipalName.isEmpty()) {
            throw new Exception("Could not get user information from Microsoft Graph.");
        }

        UserDetails userDetails = loadUserByUsername(userPrincipalName);
        return jwtUtil.generateToken(userDetails);
    }

	/*

	@Override
	public ResponseEntity<?> userRole(long idEmpresa, int page, int size) {
		if(idEmpresa <= 0){
			return ResponseEntity.badRequest()
					.body("El id de la empresa debe ser obligatorio");
		}
		Page<UsuarioByRolProjection> listUsuarioRolAzure = dao.findListUsuarioRolAzure(idEmpresa, PageRequest.of(page, size));

		List<UsuarioRolAzureModel> ura = listUsuarioRolAzure
				.stream()
				.map(UsuarioRolAzureModel::new)
				.collect(Collectors.toList());

		PageImpl<UsuarioRolAzureModel> pageUsuarioRolAzureModel = new PageImpl<>(ura, listUsuarioRolAzure.getPageable(), listUsuarioRolAzure.getTotalElements());

		return ResponseEntity.ok(pageUsuarioRolAzureModel);
	}

	@Override
	public ResponseEntity<?> userByRoles(long idEmpresa, long idRol, String codigoGrupo) {
		if(idEmpresa <= 0){
			return ResponseEntity.badRequest()
					.body("El id de la empresa debe ser obligatorio");
		}
		if(idRol <= 0){
			return ResponseEntity.badRequest()
					.body("El id de la rol debe ser obligatorio");
		}

		List<UsuarioGrupoProjection> usuarioByEmpresaIdAndRolId = dao.findUsuarioByEmpresaIdAndRolId(idEmpresa, idRol);

		List<UsuarioGrupoModel> usuarioGrupoFilterList = codigoGrupo == null || codigoGrupo.isEmpty()
				? usuarioByEmpresaIdAndRolId.stream().filter(u -> u.getCode() == null).map(t -> new UsuarioGrupoModel(t.getUsuario(), t.getCode())).collect(Collectors.toList())
				: usuarioByEmpresaIdAndRolId.stream().filter(u -> u.getCode() != null && u.getCode().equals(codigoGrupo)).map(t-> new UsuarioGrupoModel(t.getUsuario(), t.getCode())).collect(Collectors.toList());

		return ResponseEntity.ok(usuarioGrupoFilterList);
	}
	 */
}