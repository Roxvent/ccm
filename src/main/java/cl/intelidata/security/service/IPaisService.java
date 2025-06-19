package cl.intelidata.security.service;

import cl.intelidata.security.model.api.PaisListRequest;
import cl.intelidata.security.model.api.PaisListResponse;

public interface IPaisService {

	public PaisListResponse listar(PaisListRequest request);

}
