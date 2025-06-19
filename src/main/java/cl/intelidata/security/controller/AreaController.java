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

import cl.intelidata.ccm2.security.entity.Area;
import cl.intelidata.security.model.api.AreaListRequest;
import cl.intelidata.security.model.api.AreaListResponse;
import cl.intelidata.security.model.api.AreaModel;
import cl.intelidata.security.service.IAreaService;

@RestController
@RequestMapping("/area")
public class AreaController {

	@Autowired
	private IAreaService service;

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AreaListResponse listar(@RequestBody AreaListRequest request) {
		return service.listarAreaPorEmpresa(request);
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AreaModel crear(@RequestBody AreaModel request) {
		service.registrar(request);
		return request;
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void update(@RequestBody AreaModel request) {
		service.modificar(request);
	}

	@PostMapping(value = "/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void remove(@RequestBody AreaModel request) {
		service.eliminar(request.getIdArea());
	}

	@PostMapping(value = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void enable(@RequestBody AreaModel request) {
		service.habilitar(request.getIdArea());
	}

	@PostMapping(value = "/disable", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void disable(@RequestBody AreaModel request) {
		service.inhabilitar(request.getIdArea());
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Area listarById(@PathVariable("id") long id) {
		return service.listarId(id);
	}


	// servicio para listar solo id y nombre de areas
	@GetMapping(value = "/list-id-nombre", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AreaListResponse listarIdNombre() {
		return service.listarIdNombre();
	}
}
