package models;

import io.ebean.Finder;
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
    private String key;

    /**
     * La clave usuario sólo puede pertenecer a un usuario
     */
    @OneToOne(mappedBy = "apiKey")
    private User user;

    /**
     * Permite hacer búsquedas de apiKeys
     */
    public static final Finder<Long, ApiKey> find = new Finder<>(ApiKey.class);

    public ApiKey() {
        super();
    }

    //Código modificado del de la página "https://stackoverflow.com/questions/20536566/creating-a-random-string-with-a-z-and-0-9-in-java"
    protected void generateRandomKey() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < 30) {
            stringBuilder.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        this.setKey(stringBuilder.toString());
    }

    public static ApiKey findBykey(String key) {
        return find.query().where().isNotNull("key").eq("key", key).findOne();
    }


    //Getter y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
