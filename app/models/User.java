package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.PagedList;
import org.hibernate.validator.constraints.NotBlank;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import validators.FirstCapitalLetter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa la tabla Users de la base de datos
 */

@Entity
public class User extends BaseModel {

    /**
     * Rol del usuario
     */
    @JsonIgnore
    private Boolean admin;

    /**
     * Nick del usuario
     */
    @NotBlank(message = "validation.blank")
    @MinLength(value = 4, message = "validation.nickMinLength")
    @MaxLength(value = 15, message = "validation.nickMaxLength")
    private String nick;

    /**
     * Nombre del usuario
     */
    @NotBlank(message = "validation.blank")
    @FirstCapitalLetter(message = "validation.capitalLetter")
    private String name;

    /**
     * Apellido del usuario
     */
    @NotBlank(message = "validation.blank")
    @FirstCapitalLetter(message = "validation.capitalLetter")
    private String surname;

    /**
     * Ciudad del usuario
     */
    @NotBlank(message = "validation.blank")
    @FirstCapitalLetter(message = "validation.capitalLetter")
    private String city;

    /**
     * Se asigna una apiKey al usuario
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private ApiKey apiKey;

    /**
     * Lista de recetas perteneciente a un usuario
     */
    @JsonBackReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<Recipe> userRecipes = new ArrayList<>();


    /**
     * Constructor del modelo User
     *
     * @param nick    Nick del usuario
     * @param name    Nombre del usuario
     * @param surname Apellido del usuario
     * @param city    Dirección del usuario
     */
    public User(@MinLength(value = 4, message = "validation.minLength")
                @MaxLength(value = 15, message = "validation.maxLength") String nick,
                String name,
                String surname,
                String city) {
        this.nick = nick;
        this.name = name;
        this.surname = surname;
        this.city = city;
    }

    /**
     * Atributo find para realizar la consulta de datos
     */
    public static final Finder<Long, User> find = new Finder<>(User.class);


    //Métodos para realizar búsquedas de usuarios

    /**
     * Búsqueda por id
     *
     * @param id Id del usuario
     * @return <p>Devuelve el usuario con el id indicado</p>
     */
    public static User findById(Long id) {

        return find.byId(id);
    }


    /**
     * Método para obtener el usuario a partir del ApiKey
     *
     * @param apikey Clave del usuario a buscar
     * @return <p>Devuelve el usuario con el id indicado</p>
     */
    public static User findByApiKey(String apikey) {

        ApiKey key = ApiKey.findBykey(apikey);
        if (key != null) {
            Long apiKeyId = key.getId();
            return find.query().where().isNotNull("api_key_id").eq("api_key_id", apiKeyId).findOne();
        }
        return null;
    }

    /**
     * Búsqueda por nick
     *
     * @param nick Nick del usuario
     * @return <p>Devuelve el usuario con el nick indicado</p>
     */
    public static User findByNick(String nick) {

        return find.query().where().isNotNull("nick").eq("nick", nick).findOne();
    }

    /**
     * Búsqueda por nombre
     *
     * @param name Nombre del usuario
     * @param page Página del listado a mostrar
     * @return <p>Devuelve el usuario o usuarios con el nombre indicado</p>
     */
    public static PagedList<User> findByName(String name, Integer page) {

        return find.query().where().isNotNull("name").eq("name", name).setMaxRows(25)
                .setFirstRow(25 * page).findPagedList();
    }

    /**
     * Búsqueda por apellido
     *
     * @param surname Apellido del usuario
     * @param page Página del listado a mostrar
     * @return <p>Devuelve el usuario o usuarios con el apellido indicado</p>
     */
    public static PagedList<User> findBySurname(String surname, Integer page) {

        return find.query().where().isNotNull("surname").eq("surname", surname).setMaxRows(25)
                .setFirstRow(25 * page).findPagedList();
    }

    /**
     * Búsqueda por nombre completo
     *
     * @param name    Nombre del usuario
     * @param surname Apellido del usuario
     * @param page Página del listado a mostrar
     * @return <p>Devuelve el usuario o usuarios con el nombre y apellido indicados</p>
     */
    public static PagedList<User> findByFullName(String name, String surname, Integer page) {

        return find.query().where().isNotNull("name").eq("name", name).and().isNotNull("surname")
                .eq("surname", surname).setMaxRows(25).setFirstRow(25 * page).findPagedList();
    }

    /**
     * Búsqueda por ciudad
     *
     * @param city Ciudad del usuario
     * @param page Página del listado a mostrar
     * @return <p>Devuelve el usuario o usuarios que vivan en la ciudad indicada</p>
     */
    public static PagedList<User> findByCity(String city, Integer page) {

        return find.query().where().isNotNull("city").eq("city", city).setMaxRows(25)
                .setFirstRow(25 * page).findPagedList();
    }

    /**
     * Listado completo mostrado en páginas de 25 usuarios
     *
     * @param page Página del listado a mostrar
     * @return <p>Devuelve el listado de usuarios</p>
     */
    public static PagedList<User> findAll(Integer page) {

        return find.query().setMaxRows(25).setFirstRow(25 * page).findPagedList();
    }


    /**
     * Método que comprueba si el nick ya existe en la base de datos
     *
     * @return <ul>
     * <li>true: genera una clave API y guarda el usuario</li>
     * <li>false: el nick ya existe en la base de datos</li>
     * </ul>
     */
    public boolean checkAndSave() {
        if (User.findByNick(this.nick) == null) {

            Ebean.beginTransaction();
            try {
                do {
                    this.generateApiKey();
                } while (ApiKey.findBykey(this.apiKey.getKey()) != null);
                this.save();
                Ebean.commitTransaction();
            } finally {
                Ebean.endTransaction();
            }
            return true;

        }
        return false;
    }

    /**
     * Método que genera una clave API
     */
    public void generateApiKey() {
        this.apiKey = new ApiKey();
        this.apiKey.generateRandomKey();
    }

    //Getter y Setters

    /**
     * Getter de nick
     *
     * @return Devuelve el nick del usuario
     */
    public String getNick() {
        return nick;
    }

    /**
     * Setter de nick
     *
     * @param nick Nick del usuario
     */
    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * Getter de name
     *
     * @return Devuelve el nombre del usuario
     */
    public String getName() {
        return name;
    }

    /**
     * Setter de name
     *
     * @param name Nombre del usuario
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter de surname
     *
     * @return Devuelve el apellido del usuario
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Setter de surname
     *
     * @param surname Apellido del usuario
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Getter de city
     *
     * @return Devuelve la ciudad del usuario
     */
    public String getCity() {
        return city;
    }

    /**
     * Setter de city
     *
     * @param city Ciudad del usuario
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Getter de apiKey
     *
     * @return Devuelve la clave del usuario
     */
    public ApiKey getApiKey() {
        return apiKey;
    }

    /**
     * Setter de apiKey
     *
     * @param apiKey Clave del usuario
     */
    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Getter de userRecipes
     *
     * @return Devuelve las recetas de un usuario
     */
    public List<Recipe> getUserRecipes() {
        return userRecipes;
    }

    /**
     * Setter de userRecipes
     *
     * @param userRecipes Lista de recetas de un usuario
     */
    public void setUserRecipes(List<Recipe> userRecipes) {
        this.userRecipes = userRecipes;
    }

    /**
     * Getter de admin
     *
     * @return Devuelve el estado de admin (true o false)
     */
    public Boolean getAdmin() {
        return admin;
    }

    /**
     * Setter de admin
     *
     * @param admin Rol del usuario
     */
    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}
