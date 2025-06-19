package cl.intelidata.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cl.intelidata.security.model.api.PaisListRequest;
import cl.intelidata.security.model.api.PaisListResponse;
import cl.intelidata.security.service.IPaisService;

@RestController
@RequestMapping("/pais")
public class PaisController {

	@Autowired
	private IPaisService service;

	@PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody PaisListResponse listar(@RequestBody PaisListRequest request) {
		return service.listar(request);
	}

}
