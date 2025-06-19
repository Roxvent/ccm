package cl.intelidata.security.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEmpresaList {
    private long total;
    private List<EmpresaDTO> empresas;
}
