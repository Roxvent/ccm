package cl.intelidata.security.model.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UsuarioRolAzureModel {
    private long idRol;
    private String rol;
    @JsonProperty(value = "qty")
    private int qtyUsuario;
    private String code;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;
}
