package cl.intelidata.security.dto;

import java.io.Serializable;

public class AzureRedirectResponse implements Serializable {
    private String redirectUrl;

    public AzureRedirectResponse(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
