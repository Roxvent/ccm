package cl.intelidata.security.model.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UsuarioModel {
	private long idUsuario;
	private long idDepartamento;
	private String username;
	private String password;
	private List<Long> roles;
	private boolean enable;
}
