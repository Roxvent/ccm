package cl.intelidata.security.service;

import cl.intelidata.security.model.api.GrupoCollection;
import cl.intelidata.security.model.api.GrupoModel;
import cl.intelidata.security.model.api.RolListRequest;
import cl.intelidata.security.model.api.RolListResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRolService {

	RolListResponse listar(RolListRequest request);
	RolListResponse listarRolUsuario(RolListRequest request);
    ResponseEntity<?> linkGroupToRol(long idRol, GrupoCollection groups);
	ResponseEntity<?> findRolByGroupId(String groupId);
	ResponseEntity<?> findGroupsByRol(long idRol);
	ResponseEntity<?> findGroupById(long idRol, String groupId);
	ResponseEntity<?> removeGroup(long idRol, String groupId);
	ResponseEntity<?> updateGroup(long idRol, List<GrupoModel> groups);
}
