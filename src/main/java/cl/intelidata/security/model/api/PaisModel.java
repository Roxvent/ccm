package cl.intelidata.security.model.api;

import cl.intelidata.ccm2.security.entity.Pais;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaisModel {

	private String idPais;
	private String descripcion;

	public PaisModel(Pais p){
		this.idPais 	= p.getIdPais();
		this.descripcion = p.getDescripcion();
	}
}
