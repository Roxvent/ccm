package cl.intelidata.security.model.api;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Empresa;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmpresaListResponse {
	private long total;
	private List<Empresa> empresas;
}
