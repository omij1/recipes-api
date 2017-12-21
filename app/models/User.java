package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * Modelo que representa la tabla Users de la base de datos
 */

@Entity
public class User extends Model {

    /**
     * Identificador de usuario
     */
    @Id
    Long id_user;

    /**
     * Nick del usuario
     */
    private String nick;
    /**
     * Nombre del usuario
     */
    private String name;
    /**
     * Apellido del usuario
     */
    private String surname;
    /**
     * Ciudad del usuario
     */
    private String city;
    /**
     * Se asigna una apiKey al usuario
     */
    @OneToOne(cascade = CascadeType.ALL)
    private ApiKey apiKey;


    /**
     * Constructor del modelo User
     *
     * @param nick    Nick del usuario
     * @param name    Nombre del usuario
     * @param surname Apellido del usuario
     * @param city    Dirección del usuario
     */
    public User(String nick, String name, String surname, String city) {
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
     * @return <p>Devuelve el usuario o usuarios que vivan en la ciudad indicada</p>
     */
    public static PagedList<User> findByCity(String city, Integer page) {
        return find.query().where().isNotNull("city").eq("city", city).setMaxRows(25)
                .setFirstRow(25 * page).findPagedList();
    }

    /**
     * Listado completo mostrado en pagínas de 25 usuarios
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
            this.generateApiKey();
            this.save();
            return true;
        }
        return false;
    }

    /**
     * Método que genera una clave API
     */
    public void generateApiKey() {
       this.apiKey = new ApiKey();
       this.apiKey.generateRandomApikey();
    }

    //Getter y Setters

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
    }
}
