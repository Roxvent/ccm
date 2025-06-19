package cl.intelidata.security.service.impl;

import static cl.intelidata.security.util.Constants.INHABILITED_USER;
import static cl.intelidata.security.util.Constants.NO_USER;

import java.util.*;
import java.util.stream.Collectors;

import cl.intelidata.ccm2.security.entity.*;
import cl.intelidata.ccm2.security.projections.DTO.ExternalIdentificationDTO;
import cl.intelidata.ccm2.security.projections.DTO.UserDTO;
import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.ccm2.security.projections.UserListProjection;
import cl.intelidata.ccm2.security.repository.*;
import cl.intelidata.security.model.api.*;
import cl.intelidata.security.util.Regex;
import cl.intelidata.security.util.ResponseApiHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cl.intelidata.security.service.IUsuarioService;
import lombok.extern.slf4j.Slf4j;

@Service("userDetailsService")
@Slf4j
public class UserServiceImpl implements UserDetailsService, IUsuarioService {

	@Autowired
	private IUsuarioDAO dao;

	@Autowired
	private IAppDAO daoApp;

	@Autowired
	private IRolDAO daoRol;

	@Autowired
	private IDepartamentoDAO daoDepartamento;

	@Autowired
	private IServicioDAO servicioDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario user = dao
				.findUsuario(
						username.substring(0, username.indexOf("-")),
						username.substring(username.indexOf("-") + 1));
		if (user == null) {
			throw new UsernameNotFoundException(String.format(NO_USER, username));
		}
		if (!user.isEnabled()) {
			throw new UsernameNotFoundException(String.format(INHABILITED_USER, username));
		}
		List<GrantedAuthority> authorities = new ArrayList<>();
		user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getNombre())));
		return new User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
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
		try{
			Long idEmpresa = dao.findEmpresaByUsername(username).orElse(null);
			List<Rol> rolesByUsername = dao.findRolesByUsername(username);
			if(idEmpresa != null && !rolesByUsername.isEmpty()){
				return new AuthDTO(idEmpresa, username, rolesByUsername.stream().map(Rol::getNombre).collect(Collectors.toList()));
			}
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public ResponseEntity<?> findIdentification(String username) {
		try{
			ExternalIdentificationDTO ei = dao.findIdentification(username).orElse(null);
			List<Rol> rolesByUsername = dao.findRolesByUsername(username);
			if(ei != null && !rolesByUsername.isEmpty()){
				return ResponseApiHandler
						.generateResponse(HttpStatus.OK, new AuthExtendedDTO(ei.getIdEmpresa(),
								username,
								rolesByUsername.stream().map(Rol::getNombre).collect(Collectors.toList()),
								ei.getIdArea(),
								ei.getIdDepartamento()));
			}
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return null;
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