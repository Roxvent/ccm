package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AuthDTO {
    private long idEmpresa;
    private String username;
    private List<String> roles = new ArrayList<>();

    public AuthDTO(){}

    public AuthDTO(long id_empresa, String user_name, List<String> roles){
        this.idEmpresa = id_empresa;
        this.username = user_name;
        this.roles = roles;
    }
}
