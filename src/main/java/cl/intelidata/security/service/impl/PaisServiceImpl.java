package cl.intelidata.security.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cl.intelidata.ccm2.security.entity.Pais;
import cl.intelidata.ccm2.security.repository.IPaisDAO;
import cl.intelidata.security.model.api.PaisListRequest;
import cl.intelidata.security.model.api.PaisListResponse;
import cl.intelidata.security.service.IPaisService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaisServiceImpl implements IPaisService {

	@Autowired
	private IPaisDAO dao;

	@Override
	public PaisListResponse listar(PaisListRequest request) {
		log.info("Listando Pais");
		PaisListResponse response = new PaisListResponse();
		List<Pais> paises;
		if (request.isPaged()) {
			Pageable pagination = PageRequest.of(request.getPage(), request.getSize());
			paises = dao.findAll(pagination).toList();
		} else {
			paises = (List<Pais>) dao.findAll();
		}
		response.setPaises(paises);
		response.setTotal(dao.count());
		return response;
	}

}
