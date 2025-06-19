package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UsuarioListRequest {
	private String user;
	private String app;
	private long idDepartamento;
	private boolean paged;
	private int page;
	private int size;

	//nuevos

	private long idArea;
	private long idEmpresa;
}
