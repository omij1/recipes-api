package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.PagedList;
import models.User;
import play.cache.SyncCacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import java.util.List;

public class UserController extends Controller {

    /**
     * Variable formulario
     */
    @Inject
    FormFactory formFactory;

    /**
     * Variable caché
     */
    @Inject
    private SyncCacheApi cache;

    /**
     * Variable para presentar los mensajes al usuario según el idioma
     */
    private Messages messages;


    /**
     * Método para crear un usuario nuevo
     *
     * @return <ul>
     * <li>Devuelve error en caso de que la petición no sea correcta</li>
     * <li>Devuelve error en caso de que el nick ya exista en la base de datos</li>
     * <li>Devuelve mensaje de éxito si el usuario se ha creado correctamente</li>
     * </ul>
     */
    public Result createUser() {

        messages = Http.Context.current().messages();

        Form<User> f = formFactory.form(User.class).bindFromRequest(); //Creación de objeto Form para obtener los datos de la petición
        if (f.hasErrors()) {         //Comprobar si hay errores
            return Results.status(409, f.errorsAsJson());
        }

        User user = f.get();  //Objeto User donde se guarda la información de la petición
        user.setAdmin(false);

        //Validación y guardado en caso de que el nick no exista. En caso contrario se muestra el error correspondiente
        if (user.checkAndSave()) {
            if (request().accepts("application/xml")) {
                return Results.created(views.xml.apiKey.render(user));
            } //Si acepta json, no indica el formato o el formato indicado es incorrecto, se envía en json
            ObjectNode apiKey = Json.newObject();
            apiKey.put("apiKey", user.getApiKey().getKey());
            return Results.created(Json.prettyPrint(apiKey));
        }

        return Results.status(409, new ErrorObject("3", messages.at("user.alreadyExist")).convertToJson()).as("application/json");

    }

    /**
     * Método para obtener los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUser(Long id_user) {

        messages = Http.Context.current().messages();

        //Comprobamos si el usuario está en caché
        String key = "user-" + id_user;
        User user = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (user == null) {
            user = User.findById(id_user);
            cache.set(key, user);
        }

        //Si el Id no existe, se devuelve un error
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "user-" + id_user + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(user);
                cache.set(key, json);
            }
            return ok(Json.prettyPrint(json));
        }

        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método para obtener los datos de un usuario a través de su nick
     *
     * @param nick nick del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByNick(String nick) {

        messages = Http.Context.current().messages();

        //Comprobamos si el usuario está en caché
        String key = "user-" + nick;
        User user = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (user == null) {
            user = User.findByNick(nick);
            cache.set(key, user);
        }

        //Si no existe ningún usuario con ese nick
        if (user == null) {
            return Results.notFound(messages.at("user.wrongNick"));
        }
        //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "user-" + nick + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(user);
                cache.set(key, json);
            }
            return ok(Json.prettyPrint(json));
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que permite obtener las recetas creadas por un usuario
     *
     * @param id Identificador del usuario del que se desean ver sus recetas
     * @return Devuelve las recetas creadas por el usuario seleccionado o error
     */
    public Result retrieveUserRecipes(Long id) {

        messages = Http.Context.current().messages();

        //Comprobamos si el usuario está en caché
        String key = "userRecipes-" + id;
        User user = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (user == null) {
            user = User.findById(id);
            cache.set(key, user);
        }

        //Comprobamos si el id introducido corresponde a un usuario
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Miramos si el usuario tiene recetas publicadas
        if (user.getUserRecipes().size() > 0) {
            if (request().accepts("application/json")) {
                //Buscamos la respuesta en caché
                key = "userRecipes-" + id + "-json";
                JsonNode json = cache.get(key);
                //Si no está, la creamos y la guardamos en caché
                if (json == null) {
                    json = Json.toJson(user.getUserRecipes());
                    cache.set(key, json);
                }
                return ok(Json.prettyPrint(json));
            } else if (request().accepts("application/xml")) {
                return ok(views.xml.recipes.render(user.getUserRecipes()));
            }
            return Results.status(415, messages.at("wrongOutputFormat"));
        }
        return Results.ok(messages.at("user.listEmpty"));

    }

    /**
     * Método para buscar a todos los usuarios que tengan el nombre proporcionado como parámetro
     *
     * @param name nombre de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByName(String name) {

        messages = Http.Context.current().messages();
        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, messages.at("page.null"));
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "listByName-" + name + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findByName(name, page);
            cache.set(key, list, 60 * 2);
        }
        List<User> usersList = list.getList();
        Integer number = list.getTotalCount();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            return Results.notFound(messages.at("user.wrongName"));
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "listByName-" + name + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(usersList);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        }

        return Results.status(415, messages.at("wrongOutputFormat"));
    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el apellido proporcionado como parámetro
     *
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserBySurname(String surname) {

        messages = Http.Context.current().messages();
        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, messages.at("page.null"));
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "listBySurname-" + surname + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findBySurname(surname, page);
            cache.set(key, list, 60 * 2);
        }
        List<User> usersList = list.getList();
        Integer number = list.getTotalCount();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            return Results.notFound(messages.at("user.wrongSurname"));
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "listBySurname-" + surname + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(usersList);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el nombre y apellido proporcionados como parámetros
     *
     * @param name    nombre de los usuarios que se quieren buscar
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByFullName(String name, String surname) {

        messages = Http.Context.current().messages();
        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, messages.at("page.null"));
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "listByFullName-" + name + surname + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findByFullName(name, surname, page);
            cache.set(key, list, 60 * 2);
        }
        List<User> usersList = list.getList();
        Integer number = list.getTotalCount();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            return Results.notFound(messages.at("user.wrongFullName"));
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "listByFullName-" + name + surname + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(usersList);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }


    /**
     * Método que devuelve un listado de los usuarios que son de la ciudad proporcionada como parámetro
     *
     * @param city ciudad de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByCity(String city) {

        messages = Http.Context.current().messages();
        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, messages.at("page.null"));
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "listByCity-" + city + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findByCity(city, page);
            cache.set(key, list, 60 * 2);
        }
        List<User> usersList = list.getList();
        Integer number = list.getTotalCount();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            return Results.notFound(messages.at("user.wrongCity"));
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "listByCity-" + city + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(usersList);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }


    /**
     * Método para actualizar los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere realizar una modificación de los datos
     * @return Indica si se ha realizado correctamente o no la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result updateUser(Long id_user) {

        messages = Http.Context.current().messages();

        //Creación de objeto Form para obtener los datos de la petición
        Form<User> f = formFactory.form(User.class).bindFromRequest();
        //Comprobar si hay errores
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Obtenemos el usuario de la cabecera Authorization
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Objeto User donde se guarda la información de la petición
        User updateUser = f.get();

        //User correspondiente al id enviado en la petición
        User user = User.findById(id_user);
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        if (user.getId() == loggedUser.getId() || loggedUser.getAdmin()) {
            Ebean.beginTransaction();
            try {
                String key = "user-" + id_user;
                cache.remove(key);
                key = "user-" + user.getNick();
                cache.remove(key);
                key = "user-" + id_user + "-json";
                cache.remove(key);
                key = "user-" + user.getNick() + "-json";
                cache.remove(key);
                updateUser.setId(user.getId());
                updateUser.update();
                Ebean.commitTransaction();
            } finally {
                Ebean.endTransaction();
            }
            return ok(messages.at("user.updated"));
        }
        return Results.status(401, messages.at("user.authorization"));

    }

    /**
     * Método para borrar un usuario
     *
     * @param id_user Id del usuario que se quiere borrar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result deleteUser(Long id_user) {

        messages = Http.Context.current().messages();
        User user = User.findById(id_user);
        //Si el usuario existe
        if (user != null) {

            //Obtenemos el usuario de la cabecera Authorization
            User loggedUser = (User) Http.Context.current().args.get("loggedUser");

            if (user.getId() == loggedUser.getId() || loggedUser.getAdmin()) {
                if (user.delete()) {
                    //Se borran la caché de las peticiones de usuario único
                    String key = "user-" + id_user;
                    cache.remove(key);
                    key = "user-" + user.getNick();
                    cache.remove(key);
                    //Se borran la caché de las respuestas
                    key = "user-" + id_user + "-json";
                    cache.remove(key);
                    key = "user-" + user.getNick() + "-json";
                    cache.remove(key);
                    return ok(messages.at("user.deleted"));
                }
                return Results.internalServerError(messages.at("user.deletedFailed"));
            }
            return Results.status(401, messages.at("user.authorization"));
        }

        //Por idempotencia, aunque no exista el usuario, la respuesta debe ser correcta.
        return ok(messages.at("user.deleted"));

    }


    /**
     * Método para obtener un listado de los usuarios
     *
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserCollection() {

        messages = Http.Context.current().messages();

        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, messages.at("page.null"));
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "usersList-" + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findAll(page);
            cache.set(key, list, 60 * 2);
        }
        List<User> usersList = list.getList();
        Integer number = list.getTotalCount();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            return Results.notFound(messages.at("user.listEmpty"));
        }

        //Si la lista tiene usuarios
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "usersList-" + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(usersList);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

}
