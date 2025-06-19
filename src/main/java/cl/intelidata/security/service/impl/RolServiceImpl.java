package cl.intelidata.security.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cl.intelidata.ccm2.security.entity.Empresa;
import cl.intelidata.ccm2.security.entity.Grupo;
import cl.intelidata.ccm2.security.repository.GrupoRepository;
import cl.intelidata.ccm2.security.repository.IEmpresaDAO;
import cl.intelidata.security.model.api.GrupoModel;
import cl.intelidata.security.model.api.GrupoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.intelidata.ccm2.security.entity.Rol;
import cl.intelidata.ccm2.security.entity.Usuario;
import cl.intelidata.ccm2.security.repository.IRolDAO;
import cl.intelidata.ccm2.security.repository.IUsuarioDAO;
import cl.intelidata.security.model.api.RolListRequest;
import cl.intelidata.security.model.api.RolListResponse;
import cl.intelidata.security.service.IRolService;

@Service
public class RolServiceImpl implements IRolService {

	@Autowired
	private IRolDAO dao;
	@Autowired
	private IUsuarioDAO daoUsuario;
	@Autowired
	private GrupoRepository grupoRepository;
	@Autowired
	private IEmpresaDAO empresaRepository;

	private boolean esSuperUser(Usuario user) {
		for (Rol rol : user.getRoles()) {
			if (rol.getNombre().equalsIgnoreCase("SADMIN")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public RolListResponse listar(RolListRequest request) {
		Usuario user = daoUsuario.findUsuario(request.getUser(), request.getApp());
		RolListResponse response = new RolListResponse();
		List<Rol> roles = null;
		if (esSuperUser(user)) {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				roles = dao.findAll(pagination).toList();
			} else {
				roles = (List<Rol>) dao.findAll();
			}
			response.setTotal(dao.count());
		} else {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				roles = dao.listarRoles(pagination);
			} else {
				roles = dao.listarRoles().stream().filter(r-> !r.getNombre().equals("SADMIN") && !r.getNombre().equals("ADMIN")).collect(Collectors.toList());
			}
			response.setTotal(dao.countRoles());
		}
		response.setRoles(roles);
		return response;
	}

	@Override
	public RolListResponse listarRolUsuario(RolListRequest request) {
		Usuario user = daoUsuario.findUsuario(request.getUser(), request.getApp());
		RolListResponse response = new RolListResponse();
		List<Rol> roles = null;
		if (request.isPaged()) {
			int initialIndex = (request.getPage() + 1) * request.getSize();
			int endIndex = initialIndex + request.getSize();
			if (user.getRoles().size() > initialIndex - 1 && user.getRoles().size() >= endIndex - 1) {
				roles = user.getRoles().subList(initialIndex - 1, endIndex - 1);
			} else if (user.getRoles().size() > initialIndex - 1 && user.getRoles().size() < endIndex - 1) {
				roles = user.getRoles().subList(initialIndex - 1, user.getRoles().size() - 1);
			} else {
				roles = new ArrayList<>();
			}
		} else {
			roles = user.getRoles();
		}
		response.setTotal(user.getRoles().size());
		response.setRoles(roles);
		return response;
	}

	@Override
	public ResponseEntity<?> linkGroupToRol(long idRol, GrupoCollection groups) {
		if(idRol <= 0 && (groups == null || groups.getGroups().isEmpty())){
			return ResponseEntity
					.badRequest()
					.build();
		}
		if(groups.getGroups().stream().anyMatch(Objects::isNull) || groups.getGroups().stream().anyMatch(String::isEmpty)){
			return ResponseEntity
					.badRequest()
					.build();
		}
		if(groups.getIdEmpresa() == 0){
			return ResponseEntity
					.badRequest()
					.build();
		}
		Optional<Rol> byId 					= dao.findById(idRol);
		Optional<Empresa> optionalEmpresa 	= empresaRepository.findById(groups.getIdEmpresa());
		if(byId.isPresent() && optionalEmpresa.isPresent()){

			Empresa e 	= optionalEmpresa.get();
			Rol r 		= byId.get();

			List<Grupo> grupoList = groups.getGroups().stream()
					.filter(id -> r.getGrupos().stream().noneMatch(g-> g.getCodigoGrupo().equals(id) && g.getEmpresa().getIdEmpresa() == e.getIdEmpresa()))
					.map(id -> {
						Grupo g = new Grupo();
						g.setCodigoGrupo(id);
						g.setRol(r);
						g.setEmpresa(e);
						return g;
					}).collect(Collectors.toList());
			if(grupoList.isEmpty()){
				return ResponseEntity
						.status(HttpStatus.PRECONDITION_FAILED)
						.contentType(MediaType.APPLICATION_JSON)
						.body("El recurso enviado ya se encuentra los registros");
			}
			grupoRepository.saveAll(grupoList);
			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(grupoList);
		}
		return ResponseEntity
				.notFound()
				.build();
	}

	@Override
	public ResponseEntity<?> findGroupById(long idRol, String groupId) {
		if(idRol <= 0 || (groupId == null || groupId.isEmpty())){
			return ResponseEntity
					.badRequest()
					.build();
		}
		Optional<Grupo> byRolIdAndGroupId = grupoRepository.findByRolIdAndGroupId(idRol, groupId);
		if(byRolIdAndGroupId.isPresent()){
			return ResponseEntity.ok(byRolIdAndGroupId.get());
		}
		return ResponseEntity.notFound().build();
	}

	@Override
	public ResponseEntity<?> findRolByGroupId(String groupId) {
		if(groupId == null || groupId.isEmpty()){
			return ResponseEntity
					.badRequest()
					.build();
		}
		List<String> rolByGroupId = grupoRepository.findRolByGroupId(groupId);
		if(rolByGroupId.isEmpty()){
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(rolByGroupId);
	}

	@Override
	public ResponseEntity<?> findGroupsByRol(long idRol) {
		if(idRol<=0){
			return ResponseEntity
					.badRequest()
					.build();
		}
		Optional<Rol> byId = dao.findById(idRol);
		if(!byId.isPresent()){
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(byId.get().getGrupos());
	}

	@Override
	public ResponseEntity<?> removeGroup(long idRol, String groupId) {
		if(idRol <= 0 || (groupId == null || groupId.isEmpty())){
			return ResponseEntity
					.badRequest()
					.build();
		}
		Optional<Grupo> byRolIdAndGroupId = grupoRepository.findByRolIdAndGroupId(idRol, groupId);
		if(byRolIdAndGroupId.isPresent()){
			Grupo g = byRolIdAndGroupId.get();
			grupoRepository.deleteById(g.getIdGrupo());
			return ResponseEntity
					.noContent()
					.build();
		}
		return ResponseEntity
				.notFound()
				.build();
	}

	@Override
	public ResponseEntity<?> updateGroup(long idRol, List<GrupoModel> groups) {
		if(idRol <= 0 || (groups == null || groups.isEmpty())){
			return ResponseEntity
					.badRequest()
					.build();
		}
		if(groups.stream().anyMatch(g -> g.getCodigo()==null) || groups.stream().anyMatch(g -> g.getCodigo().isEmpty())){
			return ResponseEntity
					.badRequest()
					.build();
		}
		Optional<Rol> byId 		= dao.findById(idRol);
		List<Grupo> groupList 	= grupoRepository.findGruposByList(groups.stream().map(GrupoModel::getId).collect(Collectors.toList()));
		Empresa empresa			= groupList.stream().map(Grupo::getEmpresa).findFirst().orElse(null);
		if(byId.isPresent() && empresa != null){
			Rol r = byId.get();
			List<Grupo> grupoList = groups.stream().filter(g -> r.getGrupos().stream().anyMatch(rg -> rg.getIdGrupo().equals(g.getId()))
					&& r.getGrupos().stream().noneMatch(rg -> rg.getCodigoGrupo().equals(g.getCodigo())))
					.map(g -> {
						Grupo grupo = new Grupo();
						grupo.setIdGrupo(g.getId());
						grupo.setCodigoGrupo(g.getCodigo());
						grupo.setFecha(LocalDateTime.now());
						grupo.setRol(r);
						grupo.setEmpresa(empresa);
						return grupo;
					}).collect(Collectors.toList());
			if(grupoList.isEmpty()){
				return ResponseEntity
						.status(HttpStatus.CONFLICT)
						.body("No pueden existir 2 codigos de igual valor asociados para un rol");
			}
			List<Grupo> persistedGroup = grupoRepository.saveAll(grupoList);
			return ResponseEntity
					.ok(persistedGroup);
		}
		return ResponseEntity
				.notFound()
				.build();
	}
}