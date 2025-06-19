package cl.intelidata.security.service;

import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.security.model.api.UsuarioListRequest;
import cl.intelidata.security.model.api.UsuarioModel;
import org.springframework.http.ResponseEntity;

public interface IUsuarioService {
	ResponseEntity<?> findById(long id);
	ResponseEntity<?> findUsuarioByDepartamento(UsuarioListRequest req);
	ResponseEntity<?> findUsuarioByFilters(UsuarioListRequest req);
	ResponseEntity<?> findUsuarioByUsername(String username);

	ResponseEntity<?> findServiciosByUsername(String username);
	ResponseEntity<?> create(UsuarioModel model , AuthDTO auth);
	ResponseEntity<?> changePassword(UsuarioModel model, AuthDTO auth);
	ResponseEntity<?> update(UsuarioModel model, AuthDTO auth);
	ResponseEntity<?> enable(long idUser, Boolean enable, AuthDTO auth);
	ResponseEntity<?> delete(long idUser, AuthDTO auth);
	AuthDTO getIdentification(String username);
	ResponseEntity<?> findIdentification(String username);
}
