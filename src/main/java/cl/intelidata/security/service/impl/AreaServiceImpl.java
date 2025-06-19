package cl.intelidata.security.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cl.intelidata.ccm2.security.entity.Area;
import cl.intelidata.ccm2.security.entity.Departamento;
import cl.intelidata.ccm2.security.entity.Empresa;
import cl.intelidata.ccm2.security.entity.Rol;
import cl.intelidata.ccm2.security.entity.Usuario;
import cl.intelidata.ccm2.security.repository.IAreaDAO;
import cl.intelidata.ccm2.security.repository.IDepartamentoDAO;
import cl.intelidata.ccm2.security.repository.IEmpresaDAO;
import cl.intelidata.ccm2.security.repository.IUsuarioDAO;
import cl.intelidata.security.model.api.AreaListRequest;
import cl.intelidata.security.model.api.AreaListResponse;
import cl.intelidata.security.model.api.AreaModel;
import cl.intelidata.security.service.IAreaService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AreaServiceImpl implements IAreaService {

	@Autowired
	private IAreaDAO dao;

	@Autowired
	private IEmpresaDAO daoEmpresa;

	@Autowired
	private IDepartamentoDAO daoDepartamento;

	@Autowired
	private IUsuarioDAO daoUsuario;

	@Override
	public void registrar(AreaModel area) {
		Area o = new Area();
		o.setNombre(area.getNombre());
		o.setEmpresa(daoEmpresa.findById(area.getIdEmpresa()).orElseGet(Empresa::new));
		o.setEnabled(area.isEnable());
		dao.save(o);
		area.setIdArea(o.getIdArea());
	}

	@Override
	public void modificar(AreaModel area) {
		Area o = dao.findById(area.getIdArea()).orElseGet(Area::new);
		o.setNombre(area.getNombre());
		o.setEmpresa(daoEmpresa.findById(area.getIdEmpresa()).orElseGet(Empresa::new));
		o.setEnabled(area.isEnable());
		dao.save(o);
	}

	@Override
	public void eliminar(long idArea) {
		for (Departamento d : daoDepartamento.listarDepartamento(idArea)) {
			daoUsuario.deleteByDepartamento(d.getIdDepartamento());
		}
		daoDepartamento.deleteByArea(idArea);
		dao.delete(idArea);
	}

	@Override
	public void inhabilitar(long idArea) {
		for (Departamento d : daoDepartamento.listarDepartamento(idArea)) {
			daoUsuario.inhabilitarByDepartamento(d.getIdDepartamento());
		}
		daoDepartamento.inhabilitarByArea(idArea);
		dao.inhabilitar(idArea);
	}

	@Override
	public void habilitar(long idArea) {
		for (Departamento d : daoDepartamento.listarDepartamento(idArea)) {
			daoUsuario.habilitarByDepartamento(d.getIdDepartamento());
		}
		daoDepartamento.habilitarByArea(idArea);
		dao.habilitar(idArea);
	}

	@Override
	public Area listarId(long idArea) {
		Optional<Area> opt = dao.findById(idArea);
		return opt.orElseGet(Area::new);
	}

	@Override
	public List<Area> listar() {
		log.info("Listando Area");
		return (List<Area>) dao.findAll();
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
	public AreaListResponse listarAreaPorEmpresa(AreaListRequest request) {
		log.info("Service Listar area por empresa");
		AreaListResponse response = new AreaListResponse();
		List<Area> areas;
		Usuario user = daoUsuario.findUsuario(request.getUser(), request.getApp());
		if (esAdminUser(user)) {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				areas = dao.listarAllArea(request.getIdEmpresa(), pagination);
			} else {
				areas = dao.listarAllArea(request.getIdEmpresa());
			}
			response.setTotal(dao.countAllArea(request.getIdEmpresa()));
		} else {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				areas = dao.listarArea(request.getIdEmpresa(), pagination);
			} else {
				areas = dao.listarArea(request.getIdEmpresa());
			}
			response.setTotal(dao.countArea(request.getIdEmpresa()));
		}
		response.setAreas(areas);
		return response;
	}
	

	// método para listar solo id y nombre de areas
	@Override
	public AreaListResponse listarIdNombre() {
		log.info("Service Listar solo id y nombre de areas");
		AreaListResponse response = new AreaListResponse();
		
		List<Area> areas = new ArrayList<Area>();
		List<Object[]> listaDB = dao.listarIdNombre();
		for (Object[] obj: listaDB) {
			Area area = new Area();
			area.setIdArea( Long.parseLong(obj[0].toString()) );
			area.setNombre(obj[1].toString());
			areas.add(area);
		}
		
		response.setTotal(areas.size());
		response.setAreas(areas);
		return response;
	}

}
