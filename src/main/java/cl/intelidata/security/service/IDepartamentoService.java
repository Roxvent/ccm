package cl.intelidata.security.service;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Departamento;
import cl.intelidata.security.model.api.DepartamentoListRequest;
import cl.intelidata.security.model.api.DepartamentoListResponse;
import cl.intelidata.security.model.api.DepartamentoModel;

public interface IDepartamentoService {
	public void registrar(DepartamentoModel dpto);

	public void modificar(DepartamentoModel dpto);

	public void eliminar(long idDepartamento);

	public void inhabilitar(long idDepartamento);

	public void habilitar(long idDepartamento);

	public Departamento listarId(long idDepartamento);

	public List<Departamento> listar();

	public DepartamentoListResponse listarDepartamentoPorArea(DepartamentoListRequest request);

	public DepartamentoListResponse listarDepartamento(DepartamentoListRequest request);

	// método para listar solo id y nombre de departamento
	DepartamentoListResponse listarIdNombre();
}
