package cl.intelidata.security.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResponseApiHandler {
    public static ResponseEntity<?> generateResponse(HttpStatus status){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status.value());
        return new ResponseEntity<>(map, status);
    }
    public static ResponseEntity<?> generateResponse(HttpStatus status, String message){
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        return new ResponseEntity<>(map, status);
    }
    public static ResponseEntity<?> generateResponse(HttpStatus status, String message, Object responseObj){
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status",status.value());
        map.put("data", responseObj);
        if(responseObj.getClass().equals(ArrayList.class)){
            map.put("total", ((ArrayList<?>)responseObj).size());
        }
        return new ResponseEntity<>(map,status);
    }
    public static ResponseEntity<?> generateResponse(HttpStatus status, Object responseObj){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status.value());
        map.put("data", responseObj);
        if(responseObj.getClass().equals(ArrayList.class)){
            map.put("total", ((ArrayList<?>)responseObj).size());
        }
        return new ResponseEntity<>(map, status);
    }
}
