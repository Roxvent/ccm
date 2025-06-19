package cl.intelidata.security.model.api;

import cl.intelidata.ccm2.security.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private long idUsuario;
    private String username;
    private boolean enabled;
    private List<Rol> roles;
}
