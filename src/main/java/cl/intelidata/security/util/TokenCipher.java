package cl.intelidata.security.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public class TokenCipher {
    private final String token;

    public TokenCipher(String token){
        this.token = token;
    }

    public String getUsername(){
        try {
            String[] chunks     = this.token.split("\\.");
            String payload      = new String(Base64.getDecoder().decode(chunks[1]));
            JsonNode jsonNode   = new ObjectMapper().readTree(payload);
            String username     = jsonNode.get("user_name").asText();
            if(!username.isEmpty()){
                return username;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
