package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;


/**
 * Modelo que representa la tabla ApiKeys de la base de datos
 */
@Entity
public class ApiKey extends Model {

    /**
     * Identificador de la clave usuario
     */
    @Id
    private Long id;
    /**
     * Clave usuario
     */
    private String apiKey;

    /**
     * La clave usuario sólo puede pertenecer a un usuario
     */
    @OneToOne(mappedBy = "apiKey")
    private User user;

    public ApiKey() {
        super();
    }

    //Código modificado del de la página "https://stackoverflow.com/questions/20536566/creating-a-random-string-with-a-z-and-0-9-in-java"
    protected void generateRandomApikey() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < 30) {
            stringBuilder.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        this.setApiKey(stringBuilder.toString());
    }


    //Getter y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}