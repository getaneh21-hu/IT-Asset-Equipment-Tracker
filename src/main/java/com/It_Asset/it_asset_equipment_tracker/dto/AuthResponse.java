
package com.It_Asset.it_asset_equipment_tracker.dto;

public class AuthResponse {

    private String token;
    private String tokenType;
    private String username;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
