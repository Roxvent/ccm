package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AuthExtendedDTO extends AuthDTO {
    private Long idArea;
    private Long idDepartamento;

    public AuthExtendedDTO(){}

    public AuthExtendedDTO(long id_empresa, String user_name, List<String> roles, Long id_area, Long id_departamento){
        super(id_empresa, user_name, roles);
        this.idArea = id_area;
        this.idDepartamento = id_departamento;
    }
}
