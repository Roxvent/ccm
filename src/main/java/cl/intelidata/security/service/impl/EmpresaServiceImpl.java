package cl.intelidata.security.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import cl.intelidata.ccm2.security.entity.*;
import cl.intelidata.ccm2.security.projections.EmpresaProjection;
import cl.intelidata.ccm2.security.projections.GrupoByRolProjection;
import cl.intelidata.ccm2.security.projections.UsuarioByRolProjection;
import cl.intelidata.ccm2.security.repository.*;
import cl.intelidata.security.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.intelidata.security.service.IEmpresaService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmpresaServiceImpl implements IEmpresaService {

	@Autowired
	private IEmpresaDAO dao;
	@Autowired
	private IPaisDAO daoPais;
	@Autowired
	private IUsuarioDAO daoUsuario;
	@Autowired
	private IAreaDAO daoArea;
	@Autowired
	private IDepartamentoDAO daoDepartamento;
	@Autowired
	private AuthKeyRepository authKeyRepository;
	@Autowired
	GrupoRepository grupoRepository;
	@Autowired
	IServicioDAO servicioRepository;

	@Override
	public void registrar(EmpresaModel empresa) {
		Empresa o = new Empresa();
		o.setNombre(empresa.getNombre());
		o.setPais(daoPais.findById(empresa.getIdPais()).orElseGet(Pais::new));
		o.setRazonSocial(empresa.getRazonSocial());
		o.setLogo(empresa.getLogo());
		o.setPadre(dao.findById(Long.parseLong("1")).orElseGet(Empresa::new));
		o.setEnabled(empresa.isEnable());
		dao.save(o);
		empresa.setIdEmpresa(o.getIdEmpresa());
		empresa.setPadre(o.getPadre().getIdEmpresa());
		createDefaultStructure(o);
	}

	private void createDefaultStructure(Empresa o) {
		Area a = new Area();
		a.setNombre("DEFAULT AREA");
		a.setEmpresa(o);
		a.setEnabled(o.isEnabled());
		daoArea.save(a);
		Departamento d = new Departamento();
		d.setNombre("DEFAULT DEPARTAMENTO");
		d.setArea(a);
		d.setEnabled(o.isEnabled());
		daoDepartamento.save(d);
		Servicio s = new Servicio();
		s.setITokenEnable(false);
		s.setEmpresa(o);
		servicioRepository.save(s);
	}

	@Override
	public void modificar(EmpresaModel empresa) {
		Empresa o = dao.findById(empresa.getIdEmpresa()).orElseGet(Empresa::new);
		o.setNombre(empresa.getNombre());
		o.setPais(daoPais.findById(empresa.getIdPais()).orElseGet(Pais::new));
		o.setRazonSocial(empresa.getRazonSocial());
		o.setLogo(empresa.getLogo());
		o.setEnabled(empresa.isEnable());
		dao.save(o);
	}

	@Override
	public void eliminar(long idEmpresa) {
		for (Area a : daoArea.listarArea(idEmpresa)) {
			for (Departamento d : daoDepartamento.listarDepartamento(a.getIdArea())) {
				daoUsuario.deleteByDepartamento(d.getIdDepartamento());
			}
			daoDepartamento.deleteByArea(a.getIdArea());
		}
		daoArea.deleteByEmpresa(idEmpresa);
		dao.delete(idEmpresa);
	}

	@Override
	public void inhabilitar(long idEmpresa) {
		for (Area a : daoArea.listarArea(idEmpresa)) {
			for (Departamento d : daoDepartamento.listarDepartamento(a.getIdArea())) {
				daoUsuario.inhabilitarByDepartamento(d.getIdDepartamento());
			}
			daoDepartamento.inhabilitarByArea(a.getIdArea());
		}
		daoArea.inhabilitarByEmpresa(idEmpresa);
		dao.inhabilitar(idEmpresa);
	}

	@Override
	public void habilitar(long idEmpresa) {
		for (Area a : daoArea.listarArea(idEmpresa)) {
			for (Departamento d : daoDepartamento.listarDepartamento(a.getIdArea())) {
				daoUsuario.habilitarByDepartamento(d.getIdDepartamento());
			}
			daoDepartamento.habilitarByArea(a.getIdArea());
		}
		daoArea.habilitarByEmpresa(idEmpresa);
		dao.habilitar(idEmpresa);
	}

	@Override
	public Empresa listarId(long idEmpresa) {
		// 2
		Optional<Empresa> opt = dao.findById(idEmpresa);
		return opt.orElseGet(Empresa::new);
	}

	@Override
	public List<Empresa> listar() {
		return (List<Empresa>) dao.findAll();
	}

	private boolean esAdminUser(Usuario user) {
		for (Rol rol : user.getRoles()) {
			if (rol.getNombre().equalsIgnoreCase("SADMIN") || rol.getNombre().equalsIgnoreCase("ADMIN")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public EmpresaListResponse listarEmpresaPorUsuario(EmpresaListRequest request) {
		log.info("Service Listar empresas por usuario");
		Usuario user = daoUsuario.findUsuario(request.getUser(), request.getApp());
		EmpresaListResponse response = new EmpresaListResponse();
		Empresa empresa = user.getDepartamento().getArea().getEmpresa();
		List<Empresa> empresas;
		if (esAdminUser(user)) {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				empresas = dao.listarAllEmpresaChild(empresa.getIdEmpresa(), pagination);
			} else {
				empresas = dao.listarAllEmpresaChild(empresa.getIdEmpresa());
			}
			response.setTotal(dao.countAllEmpresaChild(empresa.getIdEmpresa()));
		} else {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				empresas = dao.listarEmpresaChild(empresa.getIdEmpresa(), pagination);
			} else {
				empresas = dao.listarEmpresaChild(empresa.getIdEmpresa());
			}
			response.setTotal(dao.countEmpresaChild(empresa.getIdEmpresa()));
		}
		response.setEmpresas(empresas);
		return response;
	}

	@Override
	public ResponseEntity<?> listarEmpresas(EmpresaListRequest req) {
		log.info("Service Listar empresas por usuario");
		Usuario user = daoUsuario.findUsuario(req.getUser(), req.getApp());
		Empresa empresa = user.getDepartamento().getArea().getEmpresa();
		ResponseEmpresaList response = new ResponseEmpresaList();
		List<EmpresaDTO> empresasResponse;
		if (esAdminUser(user)) {
			if (req.isPaged()) {
				Pageable pagination = PageRequest.of(req.getPage(), req.getSize());
				Page<Empresa> pagedEmpresa = dao.listarPageAdminEmpresas(empresa.getIdEmpresa(), pagination);
				empresasResponse = pagedEmpresa.getContent().stream().map(EmpresaDTO::new).collect(Collectors.toList());
				PageImpl<EmpresaDTO> empresaDTOPage = new PageImpl<>(empresasResponse, pagedEmpresa.getPageable(), pagedEmpresa.getTotalElements());
				return ResponseEntity.ok(empresaDTOPage);
			} else {
				List<Empresa> empresas = dao.listarAdminEmpresas(empresa.getIdEmpresa());
				empresasResponse = empresas.stream().map(EmpresaDTO::new).collect(Collectors.toList());
				response.setEmpresas(empresasResponse);
				response.setTotal(empresasResponse.size());
				return ResponseEntity.ok(response);
			}
		} else {
			if (req.isPaged()) {
				Pageable pagination = PageRequest.of(req.getPage(), req.getSize());
				Page<Empresa> pagedEmpresas 		= dao.listarPageEmpresas(empresa.getIdEmpresa(), pagination);
				empresasResponse 					= pagedEmpresas.getContent().stream().map(EmpresaDTO::new).collect(Collectors.toList());
				PageImpl<EmpresaDTO> empresaDTOPage = new PageImpl<>(empresasResponse, pagedEmpresas.getPageable(), pagedEmpresas.getTotalElements());
				return ResponseEntity.ok(empresaDTOPage);
			} else {
				List<Empresa> empresas = dao.listarEmpresas(empresa.getIdEmpresa());
				empresasResponse = empresas.stream().map(EmpresaDTO::new).collect(Collectors.toList());
				response.setEmpresas(empresasResponse);
				response.setTotal(empresasResponse.size());
				return ResponseEntity.ok(response);
			}
		}
	}

	// método para listar solo id y nombre de Empresas
	@Override
	public EmpresaListResponse listarIdNombre() {
		log.info("Service listar solo id y nombre de Empresas");
		EmpresaListResponse response = new EmpresaListResponse();
		
		List<Empresa> empresas = new ArrayList<Empresa>();
		List<Object[]> listaDB = dao.listarIdNombre();
		for (Object[] obj: listaDB) {
			Empresa empresa = new Empresa();
			empresa.setIdEmpresa( Long.parseLong(obj[0].toString()) );
			empresa.setNombre(obj[1].toString());
			empresas.add(empresa);
		}
		response.setTotal(empresas.size());
		response.setEmpresas(empresas);
		return response;
	}

	@Override
	public ResponseEntity<?> linkAuthKey(long idEmpresa, AuthKeyModel model) {
		if(model.getClientId().isEmpty() || model.getClientSecret().isEmpty() || model.getTenantId().isEmpty()){
			return ResponseEntity.badRequest()
					.body("Los parametros no deben estar vacíos");
		}
		Optional<Empresa> optionalEmpresa = dao.findById(idEmpresa);
		if(!optionalEmpresa.isPresent()){
			return ResponseEntity.notFound()
					.build();
		}
		if(optionalEmpresa.get().getAuthKey() == null){
			AuthKey ak = new AuthKey();
			ak.setTenantId(model.getTenantId());
			ak.setClientId(model.getClientId());
			ak.setClientSecret(model.getClientSecret());
			ak.setEmpresa(optionalEmpresa.get());
			AuthKey save = authKeyRepository.save(ak);
			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(save);
		}else{
			AuthKey authKeyFromEmpresa = optionalEmpresa.get().getAuthKey();
			authKeyFromEmpresa.setTenantId(model.getTenantId());
			authKeyFromEmpresa.setClientId(model.getClientId());
			authKeyFromEmpresa.setClientSecret(model.getClientSecret());
			authKeyFromEmpresa.setEmpresa(optionalEmpresa.get());
			AuthKey updated = authKeyRepository.save(authKeyFromEmpresa);
			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(updated);
		}
	}

	@Override
	public ResponseEntity<?> findAuthKey(long idEmpresa) {
		if(idEmpresa <= 0){
			return ResponseEntity.badRequest()
					.body("id_empresa no puede ser menor o igual a cero");
		}
		Optional<Empresa> optEmpresa = dao.findById(idEmpresa);
		if(!optEmpresa.isPresent()){
			return ResponseEntity.notFound().build();
		}
		AuthKey authKey = optEmpresa.get().getAuthKey();
		return ResponseEntity
				.ok(authKey);
	}

	@Override
	public ResponseEntity<?> findByTenantId(String tenantId) {
		if(tenantId == null || tenantId.isEmpty()){
			return ResponseEntity.badRequest()
					.body("tenant_id no puede ser null o vacío");
		}

		EmpresaProjection empresaByTenantId = authKeyRepository.findEmpresaByTenantId(tenantId).orElse(null);
		if(empresaByTenantId != null){
			EmpresaModel em = new EmpresaModel();
			em.setIdEmpresa(empresaByTenantId.getIdEmpresa());
			em.setRazonSocial(empresaByTenantId.getRazonSocial());
			em.setNombre(empresaByTenantId.getNombre());
			em.setLogo(empresaByTenantId.getLogo());
			em.setEnable(empresaByTenantId.getEnabled());
			em.setIdPais(empresaByTenantId.getPais().getIdPais());

			return ResponseEntity.ok(em);
		}
		return ResponseEntity.notFound().build();
	}

	@Override
	public ResponseEntity<?> findRolByEmpresaId(long idEmpresa, int page, int size) {
		if(idEmpresa <= 0){
			return ResponseEntity.badRequest().build();
		}
		Page<Long> rolGroupBy 				= dao.findRolGroupBy(idEmpresa, PageRequest.of(page, size));
		List<UsuarioByRolProjection> roles 	= dao.findUsuariosGroupByRolByEmpresaId(rolGroupBy.getContent());
		if(roles.isEmpty()){
			return ResponseEntity.notFound().build();
		}
		List<Long> distinctRolId 				= roles.stream().map(UsuarioByRolProjection::getIdRol).distinct().collect(Collectors.toList());
		List<GrupoByRolProjection> groupList 	= grupoRepository.findGroupsByRolId(distinctRolId);

		Map<Long, List<String>> codesGroupedByIdRol = groupList.stream()
				.collect(Collectors.groupingBy(
						GrupoByRolProjection::getIdRol, Collectors.mapping(
								GrupoByRolProjection::getCodigoGrupo, Collectors.toList())));

		Map<Long, List<LocalDateTime>> rolesFechas = groupList.stream()
				.collect(Collectors.groupingBy(GrupoByRolProjection::getIdRol, Collectors.mapping(GrupoByRolProjection::getFecha, Collectors.toList())));

		Map<Long, List<String>> mapRoles = roles.stream()
				.collect(Collectors.groupingBy(UsuarioByRolProjection::getIdRol, Collectors.mapping(UsuarioByRolProjection::getUsername, Collectors.toList())));

		List<RolGrupoVinculacion> rgvList = new ArrayList<>();

		roles.forEach(r -> {
			if(rgvList.stream().noneMatch(l -> l.getIdRol().equals(r.getIdRol()))){
				RolGrupoVinculacion rolGrupo = new RolGrupoVinculacion();
				rolGrupo.setIdRol(r.getIdRol());
				rolGrupo.setRol(r.getRol());
				if(codesGroupedByIdRol.entrySet().stream().anyMatch(c -> c.getKey().equals(r.getIdRol()))){
					List<String> codes = codesGroupedByIdRol.entrySet().stream().filter(c->c.getKey().equals(r.getIdRol())).findFirst().get().getValue();
					rolGrupo.setGroupCodes(codes);
				}
				if(mapRoles.entrySet().stream().anyMatch(sr-> sr.getKey().equals(r.getIdRol()))){
					List<String> users = mapRoles.entrySet().stream().filter(sr -> sr.getKey().equals(r.getIdRol())).findFirst().get().getValue();
					rolGrupo.setRecuento((long) users.size());
					rolGrupo.setUsuarios(users);
				}
				if(rolesFechas.entrySet().stream().anyMatch(rf-> rf.getKey().equals(r.getIdRol()))){
					List<LocalDateTime> fechas = rolesFechas.entrySet().stream().filter(fr -> fr.getKey().equals(r.getIdRol())).findFirst().get().getValue();
					LocalDateTime maxFecha = fechas.stream().max(LocalDateTime::compareTo).get();
					rolGrupo.setFechaMax(maxFecha);
				}
				rgvList.add(rolGrupo);
			}
		});

		PageImpl<RolGrupoVinculacion> pageRolGrupoVinculacion = new PageImpl<>(rgvList, rolGroupBy.getPageable(), rolGroupBy.getTotalElements());

		return ResponseEntity.ok(pageRolGrupoVinculacion);
	}
}
