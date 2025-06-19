package cl.intelidata.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cl.intelidata.ccm2.security.entity.Departamento;
import cl.intelidata.security.model.api.DepartamentoListRequest;
import cl.intelidata.security.model.api.DepartamentoListResponse;
import cl.intelidata.security.model.api.DepartamentoModel;
import cl.intelidata.security.service.IDepartamentoService;

@RestController
@RequestMapping("/departamento")
public class DepartamentoController {

	@Autowired
	private IDepartamentoService service;

	@PostMapping(value = "/list-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody DepartamentoListResponse listarAll(@RequestBody DepartamentoListRequest request) {
		return service.listarDepartamento(request);
	}

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody DepartamentoListResponse listar(@RequestBody DepartamentoListRequest request) {
		return service.listarDepartamentoPorArea(request);
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody DepartamentoModel crear(@RequestBody DepartamentoModel request) {
		service.registrar(request);
		return request;
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void update(@RequestBody DepartamentoModel request) {
		service.modificar(request);
	}

	@PostMapping(value = "/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void remove(@RequestBody DepartamentoModel request) {
		service.eliminar(request.getIdDepartamento());
	}

	@PostMapping(value = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void enable(@RequestBody DepartamentoModel request) {
		service.habilitar(request.getIdDepartamento());
	}

	@PostMapping(value = "/disable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void disable(@RequestBody DepartamentoModel request) {
		service.inhabilitar(request.getIdDepartamento());
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Departamento listarById(@PathVariable("id") long id) {
		return service.listarId(id);
	}


	// servicio para listar solo id y nombre de departamentos
	@GetMapping(value = "/list-id-nombre", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody DepartamentoListResponse listarIdNombre() {
		return service.listarIdNombre();
	}
}
