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
import cl.intelidata.ccm2.security.entity.Rol;
import cl.intelidata.ccm2.security.entity.Usuario;
import cl.intelidata.ccm2.security.repository.IAreaDAO;
import cl.intelidata.ccm2.security.repository.IDepartamentoDAO;
import cl.intelidata.ccm2.security.repository.IUsuarioDAO;
import cl.intelidata.security.model.api.DepartamentoListRequest;
import cl.intelidata.security.model.api.DepartamentoListResponse;
import cl.intelidata.security.model.api.DepartamentoModel;
import cl.intelidata.security.service.IDepartamentoService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DepartamentoServiceImpl implements IDepartamentoService {

	@Autowired
	private IDepartamentoDAO dao;

	@Autowired
	private IAreaDAO daoArea;

	@Autowired
	private IUsuarioDAO daoUsuario;

	@Override
	public void registrar(DepartamentoModel dpto) {
		Departamento o = new Departamento();
		o.setNombre(dpto.getNombre());
		o.setArea(daoArea.findById(dpto.getIdArea()).orElseGet(Area::new));
		o.setEnabled(dpto.isEnable());
		dao.save(o);
		dpto.setIdDepartamento(o.getIdDepartamento());
	}

	@Override
	public void modificar(DepartamentoModel dpto) {
		Departamento o = dao.findById(dpto.getIdDepartamento()).orElseGet(Departamento::new);
		o.setNombre(dpto.getNombre());
		o.setArea(daoArea.findById(dpto.getIdArea()).orElseGet(Area::new));
		o.setEnabled(dpto.isEnable());
		dao.save(o);
	}

	@Override
	public void eliminar(long idDepartamento) {
		daoUsuario.deleteByDepartamento(idDepartamento);
		dao.deleteById(idDepartamento);
	}

	@Override
	public void inhabilitar(long idDepartamento) {
		daoUsuario.inhabilitarByDepartamento(idDepartamento);
		dao.inhabilitar(idDepartamento);
	}

	@Override
	public void habilitar(long idDepartamento) {
		daoUsuario.habilitarByDepartamento(idDepartamento);
		dao.habilitar(idDepartamento);
	}

	@Override
	public Departamento listarId(long idDepartamento) {
		Optional<Departamento> opt = dao.findById(idDepartamento);
		return opt.orElseGet(Departamento::new);
	}

	@Override
	public List<Departamento> listar() {
		log.info("Listando Departamento");
		return (List<Departamento>) dao.findAll();
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
	public DepartamentoListResponse listarDepartamentoPorArea(DepartamentoListRequest request) {
		log.info("Service Listar departamento por area");
		DepartamentoListResponse response = new DepartamentoListResponse();
		List<Departamento> deparatamentos;
		Usuario user = daoUsuario.findUsuario(request.getUser(), request.getApp());
		if (esAdminUser(user)) {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				deparatamentos = dao.listarAllDepartamento(request.getIdArea(), pagination);
			} else {
				deparatamentos = dao.listarAllDepartamento(request.getIdArea());
			}
			response.setTotal(dao.countAllDepartamento(request.getIdArea()));
		} else {
			if (request.isPaged()) {
				Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
				deparatamentos = dao.listarDepartamento(request.getIdArea(), pagination);
			} else {
				deparatamentos = dao.listarDepartamento(request.getIdArea());
			}
			response.setTotal(dao.countDepartamento(request.getIdArea()));
		}
		response.setDepartamentos(deparatamentos);
		return response;
	}

	@Override
	public DepartamentoListResponse listarDepartamento(DepartamentoListRequest request) {
		log.info("Service Listar departamento por area");
		DepartamentoListResponse response = new DepartamentoListResponse();
		List<Departamento> deparatamentos;
		if (request.isPaged()) {
			Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
			deparatamentos = dao.findAll(pagination).toList();

		} else {
			deparatamentos = (List<Departamento>) dao.findAll();
		}
		response.setTotal(dao.count());
		response.setDepartamentos(deparatamentos);
		return response;
	}

	// método para listar solo id y nombre de departamentos
	@Override
	public DepartamentoListResponse listarIdNombre() {
		log.info("Service Listar solo id y nombre de departamentos");
		DepartamentoListResponse response = new DepartamentoListResponse();
		List<Departamento> departamentos = new ArrayList<Departamento>();
		List<Object[]> listaDB = dao.listarIdNombre();
		
		for (Object[] obj: listaDB) {
			Departamento deparatamento = new Departamento();
			deparatamento.setIdDepartamento( Long.parseLong(obj[0].toString()) );
			deparatamento.setNombre(obj[1].toString());
			departamentos.add(deparatamento);
		}
		
		response.setTotal(departamentos.size());
		response.setDepartamentos(departamentos);
		return response;
	}
}
