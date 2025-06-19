package cl.intelidata.security.service;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Empresa;
import cl.intelidata.security.model.api.AuthKeyModel;
import cl.intelidata.security.model.api.EmpresaListRequest;
import cl.intelidata.security.model.api.EmpresaListResponse;
import cl.intelidata.security.model.api.EmpresaModel;
import org.springframework.http.ResponseEntity;

public interface IEmpresaService {
	void registrar(EmpresaModel empresa);
	void modificar(EmpresaModel empresa);
	void eliminar(long idEmpresa);
	void inhabilitar(long idEmpresa);
	void habilitar(long idEmpresa);
	Empresa listarId(long idEmpresa);
	List<Empresa> listar();
	EmpresaListResponse listarEmpresaPorUsuario(EmpresaListRequest request);
	ResponseEntity<?> listarEmpresas(EmpresaListRequest req);
	// método para listar solo id y nombre de Empresas
	EmpresaListResponse listarIdNombre();
    ResponseEntity<?> linkAuthKey(long idEmpresa, AuthKeyModel model);
	ResponseEntity<?> findAuthKey(long idEmpresa);
	ResponseEntity<?> findByTenantId(String tenantId);

    ResponseEntity<?> findRolByEmpresaId(long idEmpresa, int page, int size);
}
