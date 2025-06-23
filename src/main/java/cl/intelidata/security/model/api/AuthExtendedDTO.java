package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class AuthExtendedDTO extends AuthDTO {
    private Long idArea;
    private Long idDepartamento;

    public AuthExtendedDTO() {}

    // Añade este nuevo constructor
    public AuthExtendedDTO(long idEmpresa, String username, List<String> roles, 
                         Long idArea, Long idDepartamento) {
        super(idEmpresa, username, roles);
        this.idArea = idArea;
        this.idDepartamento = idDepartamento;
    }
}