package cl.intelidata.security.controller;

import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.security.util.EstadoUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.intelidata.security.model.api.UsuarioListRequest;
import cl.intelidata.security.model.api.UsuarioModel;
import cl.intelidata.security.service.IUsuarioService;

@RestController
@Tag(name = "Usuario")
@RequestMapping("${application.api.version-1}/usuario")
public class UsuarioController {
	@Autowired
	private IUsuarioService service;

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			description = "Lista los usuarios existentes",
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> listar(@RequestBody UsuarioListRequest request) {
		return service.findUsuarioByDepartamento(request);
	}
	@PostMapping(value = "list-by-filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> listarByFilter(@RequestBody UsuarioListRequest req){
		return service.findUsuarioByFilters(req);
	}
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> crear(@RequestBody UsuarioModel request,
								   @RequestAttribute("auth") AuthDTO auth) {
		return service.create(request, auth);
	}
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> update(@RequestBody UsuarioModel request ,
									@RequestAttribute("auth") AuthDTO auth) {
		return service.update(request, auth);
	}

	@PutMapping(value = "/password-reset", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> passwordReset(@RequestBody UsuarioModel request,
										   @RequestAttribute("auth") AuthDTO auth) {
		return service.changePassword(request, auth);
	}

	@DeleteMapping(value = "/{idUsuario}")
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> remove(@PathVariable long idUsuario,
									@RequestAttribute("auth") AuthDTO auth) {
		return service.delete(idUsuario, auth);
	}

	@PutMapping(value = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> enable(
			@RequestBody UsuarioModel request,
			@RequestAttribute("auth") AuthDTO auth) {
		return service.enable(request.getIdUsuario(), EstadoUser.ENABLE.getValue(), auth);
	}

	@PutMapping(value = "/disable", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> disable(
			@RequestBody UsuarioModel request,
			@RequestAttribute("auth") AuthDTO auth) {
		return service.enable(request.getIdUsuario(), EstadoUser.DISABLE.getValue(), auth);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> listarById(@PathVariable("id") long id) {
		return service.findById(id);
	}

	@GetMapping(value = "/findByUsername/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> findOneByUsername(@PathVariable("username") String username) {
		return service.findUsuarioByUsername(username);
	}

	@GetMapping(value = "/findServiciosByUsername/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			security = @SecurityRequirement(name = "Bearer Authentication"))
	public ResponseEntity<?> findServiciosByUsername(@PathVariable("username") String username) {
		return service.findServiciosByUsername(username);
	}
}
