package edu.comillas.icai.gitt.pat.spring.grupo5.model;


/**
 * Respuesta de /auth/login.
 * Mínimo exige devolver un token. Añadimos opcionales habituales por si te resultan útiles.
 */
public class LoginResponse {

    private String token;

    public LoginResponse() { }

    public LoginResponse(String token) {
        this.token = token;
    }

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

}
