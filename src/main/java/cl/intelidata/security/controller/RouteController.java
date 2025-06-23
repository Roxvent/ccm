package cl.intelidata.security.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@RestController
public class RouteController {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/routes")
    public Map<String, Object> listRoutes() {
        return handlerMapping.getHandlerMethods().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                entry -> entry.getValue().getMethod().getName()
            ));
    }
}