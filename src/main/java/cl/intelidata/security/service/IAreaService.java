package cl.intelidata.security.service;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Area;
import cl.intelidata.security.model.api.AreaListRequest;
import cl.intelidata.security.model.api.AreaListResponse;
import cl.intelidata.security.model.api.AreaModel;

public interface IAreaService {
	public void registrar(AreaModel area);

	public void modificar(AreaModel area);

	public void eliminar(long idArea);
	public void inhabilitar(long idArea);
	public void habilitar(long idArea);

	public Area listarId(long idArea);

	public List<Area> listar();

	public AreaListResponse listarAreaPorEmpresa(AreaListRequest request);

	// método para listar solo id y nombre de areas
	AreaListResponse listarIdNombre();

}
