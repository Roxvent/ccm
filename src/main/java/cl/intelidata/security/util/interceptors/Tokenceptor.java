package cl.intelidata.security.util.interceptors;

import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.security.service.IUsuarioService;
import cl.intelidata.security.util.TokenCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class Tokenceptor implements HandlerInterceptor {
    @Autowired
    IUsuarioService service;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getHeader("Authorization") != null){
            String token = request.getHeader("Authorization");
            TokenCipher tc = new TokenCipher(token);
            if(!tc.getUsername().isEmpty()){
                AuthDTO identification = service.getIdentification(tc.getUsername());
                request.setAttribute("auth", identification);
                return true;
            }
        }
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return false;
    }
}
