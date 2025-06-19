package cl.intelidata.security.controller;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cl.intelidata.ccm2.security.entity.Menu;
import cl.intelidata.security.model.NavigationResponse;
import cl.intelidata.security.model.api.UsuarioRequest;
import cl.intelidata.security.service.IMenuService;

@RestController
@RequestMapping("/menus")
public class MenuController {

	@Autowired
	private IMenuService service;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Menu> listar() {
		return service.listar();
	}

	@PostMapping(value = "/usuario", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<NavigationResponse> listar(@RequestBody UsuarioRequest request) {
		return service.listarMenuPorUsuario(request);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Menu listarById(@PathVariable("id") long id) {
		return service.listarId(id);
	}

}
