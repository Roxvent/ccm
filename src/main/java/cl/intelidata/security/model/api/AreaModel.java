package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AreaModel {
	private long idArea;
	private String nombre;
	private long idEmpresa;
	private boolean enable;
}
