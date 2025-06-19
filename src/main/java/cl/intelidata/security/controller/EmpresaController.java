package cl.intelidata.security.controller;

import cl.intelidata.security.model.api.AuthKeyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.intelidata.ccm2.security.entity.Empresa;
import cl.intelidata.security.model.api.EmpresaListRequest;
import cl.intelidata.security.model.api.EmpresaListResponse;
import cl.intelidata.security.model.api.EmpresaModel;
import cl.intelidata.security.service.IEmpresaService;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

	@Autowired
	private IEmpresaService service;

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody EmpresaListResponse listar(@RequestBody EmpresaListRequest request) {
		return service.listarEmpresaPorUsuario(request);
	}
	@PostMapping(value = "/administracion/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> listarModulo(@RequestBody EmpresaListRequest request) {
		return service.listarEmpresas(request);
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody EmpresaModel crear(@RequestBody EmpresaModel request) {
		service.registrar(request);
		return request;
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void update(@RequestBody EmpresaModel request) {
		service.modificar(request);
	}

	@PostMapping(value = "/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void remove(@RequestBody EmpresaModel request) {
		service.eliminar(request.getIdEmpresa());
	}

	@PostMapping(value = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void enable(@RequestBody EmpresaModel request) {
		service.habilitar(request.getIdEmpresa());
	}

	@PostMapping(value = "/disable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void disable(@RequestBody EmpresaModel request) {
		service.inhabilitar(request.getIdEmpresa());
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Empresa listarById(@PathVariable("id") long id) {
		return service.listarId(id);
	}


	// servicio para listar solo id y nombre de empresas
	@GetMapping(value = "/list-id-nombre", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody EmpresaListResponse listarIdNombre() {
		return service.listarIdNombre();
	}

	@PostMapping("/{id_empresa}/auth-key")
	public ResponseEntity<?> createAuthKey(@PathVariable("id_empresa") long idEmpresa,
										   @RequestBody AuthKeyModel model){
		return service.linkAuthKey(idEmpresa, model);
	}
	@GetMapping("/{id_empresa}/auth-key")
	public ResponseEntity<?> getAuthKey(@PathVariable("id_empresa") long idEmpresa) {
		return service.findAuthKey(idEmpresa);
	}

	@GetMapping
	public ResponseEntity<?> getByTenantId(@RequestParam("tenant_id") String tenantId){
		return service.findByTenantId(tenantId);
	}

	@GetMapping("/{id_empresa}/roles")
	public ResponseEntity<?> findRolByEmpresaId(@PathVariable("id_empresa") long idEmpresa,
												@RequestParam(value = "page", defaultValue = "0") int page,
												@RequestParam(value = "size", defaultValue = "5") int size){
		return service.findRolByEmpresaId(idEmpresa, page, size);
	}
}
