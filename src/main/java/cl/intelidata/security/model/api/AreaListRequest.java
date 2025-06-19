package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AreaListRequest {
    private String user;
    private String app;
    private long idEmpresa;
    private boolean paged;
    private int page;
    private int size;
}
