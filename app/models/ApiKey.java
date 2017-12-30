package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.OneToOne;


/**
 * Modelo que representa la tabla ApiKeys de la base de datos
 */
@Entity
public class ApiKey extends BaseModel {

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

    
    
    /**
     * Constructor de la clase ApiKey
     */
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

    /**
     * Método que permite buscar un aapikey en la base de datos
     * @param key La clave que se desea buscar
     * @return Un objeto con los datos de la clave
     */
    public static ApiKey findBykey(String key) {
        return find.query().where().isNotNull("key").eq("key", key).findOne();
    }

    /**
     * Getter de key
     * @return La clave del usuario
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter de key
     * @param key Clave del usuario
     */
    public void setKey(String key) {
        this.key = key;
    }

}
