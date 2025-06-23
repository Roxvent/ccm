package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class AuthExtendedWithAzureDTO extends AuthExtendedDTO {
    private Integer azureAd;

    public AuthExtendedWithAzureDTO(long idEmpresa, 
                                  String username, 
                                  List<String> roles, 
                                  Long idArea,
                                  Long idDepartamento,
                                  Integer azureAd) {
        super(idEmpresa, username, roles, idArea, idDepartamento); // Ahora este constructor existe
        this.azureAd = azureAd;
    }
}