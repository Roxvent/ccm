package cl.intelidata.security.model.api;

import cl.intelidata.ccm2.security.entity.Empresa;
import cl.intelidata.ccm2.security.entity.Servicio;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class EmpresaDTO implements Serializable {
    private final long idEmpresa;
    private final String razonSocial;
    private final String nombre;
    private final PaisModel pais;
    private final String logo;
    private final boolean enabled;
    private final Servicio servicio;
    private final boolean tieneIntegracionAzure;

    public EmpresaDTO(Empresa e){
        this.idEmpresa = e.getIdEmpresa();
        this.razonSocial = e.getRazonSocial();
        this.nombre = e.getNombre();
        this.pais = new PaisModel(e.getPais());
        this.logo = e.getLogo();
        this.enabled = e.isEnabled();
        this.servicio = e.getServicios();
        this.tieneIntegracionAzure = e.getAuthKey() != null;
    }
}
