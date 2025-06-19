package cl.intelidata.security.model.api;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Departamento;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepartamentoListResponse {
	private long total;
	private List<Departamento> departamentos;
}
