package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmpresaListRequest {

    private String user;
    private String app;
    private boolean paged;
    private int page;
    private int size;
}
