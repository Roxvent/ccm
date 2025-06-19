package cl.intelidata.security.controller;

import cl.intelidata.security.model.api.GrupoCollection;
import cl.intelidata.security.model.api.GrupoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.intelidata.security.model.api.RolListRequest;
import cl.intelidata.security.model.api.RolListResponse;
import cl.intelidata.security.service.IRolService;

import java.util.List;

@RestController
@RequestMapping("/rol")
public class RolController {

	@Autowired
	private IRolService service;

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody RolListResponse listar(@RequestBody RolListRequest request) {
		return service.listar(request);
	}

	@PostMapping(value = "/list-usuario", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody RolListResponse listarRolUsuario(@RequestBody RolListRequest request) {
		return service.listarRolUsuario(request);
	}

	@PostMapping("/{id_rol}/group")
	public ResponseEntity<?> linkGroup(@PathVariable("id_rol") long idRol,
									   @RequestBody GrupoCollection body){
		return service.linkGroupToRol(idRol, body);
	}
	@PutMapping("/{id_rol}/group")
	public ResponseEntity<?> updateGroup(@PathVariable("id_rol") long idRol,
										 @RequestBody List<GrupoModel> body){
		return service.updateGroup(idRol, body);
	}
	@DeleteMapping("/{id_rol}/group/{group_id}")
	public ResponseEntity<?> removeGroup(@PathVariable("id_rol") long idRol,
										 @PathVariable("group_id") String groupId){
		return service.removeGroup(idRol, groupId);
	}
	@GetMapping("/{id_rol}/group")
	public ResponseEntity<?> findByGroup(@PathVariable("id_rol") long idRol){
		return service.findGroupsByRol(idRol);
	}
	@GetMapping("/{id_rol}/group/{group_id}")
	public ResponseEntity<?> findGroupById(@PathVariable("id_rol") long idRol,
										   @PathVariable("group_id") String groupId){
		return service.findGroupById(idRol, groupId);
	}
	@GetMapping
	public ResponseEntity<?> findByGroup(@RequestParam("id_grupo") String groupId){
		return service.findRolByGroupId(groupId);
	}

}
