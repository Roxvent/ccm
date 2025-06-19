package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartamentoModel {
	private long idDepartamento;
	private long idArea;
	private String nombre;
	private boolean enable;
}
