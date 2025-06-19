package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmpresaModel {

	private long idEmpresa;
	private String razonSocial;
	private String nombre;
	private String idPais;
	private String logo;
	private long padre;
	private boolean enable;
}
