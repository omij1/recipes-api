package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.PagedList;
import models.Recipe;
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
            }

            //Si acepta json, no indica el formato o el formato indicado es incorrecto, se envía en json
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
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
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
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
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
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
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
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
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

            //Si era administrador, seguirá siéndolo
            if (user.getId() == loggedUser.getId()) {
                updateUser.setAdmin(loggedUser.getAdmin());
            } else {
                updateUser.setAdmin(user.getAdmin());
            }

            //Comprobamos que si actualiza el nick, no coja uno repetido
            User u = User.findByNick(f.get().getNick());
            if (u != null && u.getId() != id_user) {
                return Results.status(409, new ErrorObject("6", messages.at("user.nickAlreadyExist")).convertToJson()).as("application/json");
            }

            Ebean.beginTransaction();
            try {
                deleteUserCache(user);
                //Se borra el cache de las recetas asociadas al usuario
                List<Recipe> list = user.getUserRecipes();
                for (Recipe recipe : list) {
                    deleteRecipeCache(recipe);
                }
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
     * Método para que un administrador pueda dar o quitar privilegios de administrador a otro usuario
     *
     * @param id_user Id del usuario al que se le quieren dar o quitar privilegios
     * @return Indica si se ha realizado correctamente o no la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result toggleAdmin(Long id_user) {

        messages = Http.Context.current().messages();

        //Obtenemos el usuario de la cabecera Authorization
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Si no es administrador no tiene permiso
        if (!loggedUser.getAdmin()) {
            return Results.status(401, messages.at("user.authorization"));
        }

        //Obtenemos el usuario que va a ser o dejar de ser Administrador
        User userToAdmin = User.findById(id_user);
        if (userToAdmin == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Si el usuario elegido ya es administrador
        if (userToAdmin.getAdmin()) {
            //Si sólo hay un Administrador no se puede quitar
            if (User.findByAdmin(0).getTotalCount() > 1) {
                Ebean.beginTransaction();
                try {
                    userToAdmin.setAdmin(false);     //Deja de ser Administrador
                    userToAdmin.update();           //Se guarda en la base de datos
                    Ebean.commitTransaction();
                } finally {
                    Ebean.endTransaction();
                }
                return ok(messages.at("user.setNoAdmin"));
            }
            return Results.status(401, messages.at("user.adminError"));
        } else {
            Ebean.beginTransaction();
            try {
                userToAdmin.setAdmin(true);     //Pasa a ser administrador
                userToAdmin.update();           //Se guarda en la base de datos
                Ebean.commitTransaction();
            } finally {
                Ebean.endTransaction();
            }
            return ok(messages.at("user.setAdmin"));
        }

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
        //Se obtiene el usuario que se quiere borrar
        User user = User.findById(id_user);
        //Aunque el usuario ya no exista, por idempotencia la respuesta debe ser correcta
        if (user == null) {
            return ok(messages.at("user.deleted"));
        }

        //Obtenemos el usuario de la cabecera Authorization
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        /*Comprobamos que si se va a borrar el admin, haya al menos otro, de lo contrario no podrá borrarse
        Si el que quiere borrar al administrador no es administrador, saltará el error de autorización y no éste*/
        if (user.getAdmin() && loggedUser.getAdmin() && User.findByAdmin(0).getTotalCount() == 1) {
            return Results.status(401, messages.at("user.adminError"));
        }

        //Si la petición la realiza el propio usuario que se va a borrar, o un administrador
        if (user.getId() == loggedUser.getId() || loggedUser.getAdmin()) {
            if (user.delete()) {
                deleteUserCache(user);
                return ok(messages.at("user.deleted"));
            }
            return Results.internalServerError(messages.at("user.deletedFailed"));
        }
        return Results.status(401, messages.at("user.authorization"));

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
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
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


    /**
     * Método para obtener un listado de los administradores
     *
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveAdmin() {

        messages = Http.Context.current().messages();

        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "adminList-" + page;
        PagedList<User> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = User.findByAdmin(page);
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
            key = "adminList-" + page + "-json";
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
     * Método que borra el caché de los usuarios
     *
     * @param user usuario del que se quiere borrar el caché
     */
    public void deleteUserCache(User user) {
        String key = "user-" + user.getId();
        cache.remove(key);
        key = "user-" + user.getNick();
        cache.remove(key);
        //Se borran la caché de las respuestas
        key = "user-" + user.getId() + "-json";
        cache.remove(key);
        key = "user-" + user.getNick() + "-json";
        cache.remove(key);
    }

    /**
     * Método que borra el caché de las recetas
     *
     * @param recipe receta de la que se quiere borrar el caché
     */
    public void deleteRecipeCache(Recipe recipe) {
        String key = "recipe-" + recipe.getId();
        cache.remove(key);
        key = "recipe-" + recipe.getId() + "-json";
        cache.remove(key);
    }


}
