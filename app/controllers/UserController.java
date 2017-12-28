package controllers;


import io.ebean.PagedList;
import models.ApiKey;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
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
     * Método para crear un usuario nuevo
     *
     * @return <ul>
     * <li>Devuelve error en caso de que la petición no sea correcta</li>
     * <li>Devuelve error en caso de que el nick ya exista en la base de datos</li>
     * <li>Devuelve mensaje de éxito si el usuario se ha creado correctamente</li>
     * </ul>
     */
    public Result createUser() {

        Messages messages = Http.Context.current().messages();

        Form<User> f = formFactory.form(User.class).bindFromRequest(); //Creación de objeto Form para obtener los datos de la petición
        if (f.hasErrors()) {         //Comprobar si hay errores
            return Results.status(409, f.errorsAsJson());
        }
        
        User user = f.get();  //Objeto User donde se guarda la información de la petición

        //Validación y guardado en caso de que el nick no exista. En caso contrario se muestra el error correspondiente
        if (user.checkAndSave()) {
            if (request().accepts("application/xml")) {
                return Results.created(views.xml.apiKey.render(user));
            } else if (request().accepts("application/json")) {
                ObjectNode apiKey = Json.newObject();
                apiKey.put("apiKey", user.getApiKey().getKey());
                return Results.created(Json.prettyPrint(apiKey));
            }
        }
        return Results.status(409, new ErrorObject("X", messages.at("user.alreadyExist")).convertToJson()).as("application/json");

    }

    /**
     * Método para obtener los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUser(Long id_user) {

        Messages messages = Http.Context.current().messages();

        User user = User.findById(id_user);

        //Si el Id no existe, se devuelve un error
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(user)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");

    }

    /**
     * Método para obtener los datos de un usuario a través de su nick
     *
     * @param nick nick del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByNick(String nick) {

        Messages messages = Http.Context.current().messages();

        User user = User.findByNick(nick);
        //Si no existe ningún usuario con ese nick
        if (user == null) {
            return Results.notFound(messages.at("user.wrongNick"));
        }
        //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(user)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
    }


    /**
     * Método para buscar a todos los usuarios que tengan el nombre proporcionado como parámetro
     *
     * @param name nombre de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByName(String name) {

        Messages messages = Http.Context.current().messages();
        //Obtenemos la página
        Integer page = Integer.parseInt(request().getQueryString("page"));
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByName(name, page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(messages.at("user.wrongName"));
            } else if (request().accepts("application/json")) {
                return Results.notFound(messages.at("user.wrongName"));
            }
            return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(usersList)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el apellido proporcionado como parámetro
     *
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserBySurname(String surname) {

        Messages messages = Http.Context.current().messages();
        //Obtenemos la página
        Integer page = Integer.parseInt(request().getQueryString("page"));
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findBySurname(surname, page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(messages.at("user.wrongSurname"));
            } else if (request().accepts("application/json")) {
                return Results.notFound(messages.at("user.wrongSurname"));
            }
            return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(usersList)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el nombre y apellido proporcionados como parámetros
     *
     * @param name    nombre de los usuarios que se quieren buscar
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByFullName(String name, String surname) {

        Messages messages = Http.Context.current().messages();
        //Obtenemos la página
        Integer page = Integer.parseInt(request().getQueryString("page"));
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByFullName(name, surname, page);
        List<User> userList = list.getList();

        //Si la lista está vacía
        if (userList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(messages.at("user.wrongFullName"));
            } else if (request().accepts("application/json")) {
                return Results.notFound(messages.at("user.wrongFullName"));
            }
            return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(userList));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(userList)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");

    }


    /**
     * Método que devuelve un listado de los usuarios que son de la ciudad proporcionada como parámetro
     *
     * @param city ciudad de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByCity(String city) {

        Messages messages = Http.Context.current().messages();
        //Obtenemos la página
        Integer page = Integer.parseInt(request().getQueryString("page"));
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByCity(city, page);
        List<User> userList = list.getList();

        //Si la lista está vacía
        if (userList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(messages.at("user.wrongCity"));
            } else if (request().accepts("application/json")) {
                return Results.notFound(messages.at("user.wrongCity"));
            }
            return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(userList));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(userList)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");

    }


    /**
     * Método para actualizar los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere realizar una modificación de los datos
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result updateUser(Long id_user) {

        Messages messages = Http.Context.current().messages();

        //Creación de objeto Form para obtener los datos de la petición
        Form<User> f = formFactory.form(User.class).bindFromRequest();
        //Comprobar si hay errores
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Se obtiene el apiKey de la cadena
        String apiKey = request().getQueryString("apiKey");

        //Objeto User donde se guarda la información de la petición
        User updateUser = f.get();
        User user = User.findById(id_user);

        //Comprobar si existe el usuario con el Id indicado
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Si existe el usuario y su apiKey coincide con el apiKey suministrado, ejecutamos la actualización
        if (user.getApiKey().getKey().matches(apiKey)) {
            updateUser.setId(user.getId());
            updateUser.update();
            return ok(messages.at("user.updated"));
        }
        return Results.badRequest(messages.at("user.authorization"));

        //TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
        //TODO Sólo pueden modificar los datos de un usuario el propio usuario o el administrador
    }

    /**
     * Método para borrar un usuario
     *
     * @param id_user Id del usuario que se quiere borrar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result deleteUser(Long id_user) {

        Messages messages = Http.Context.current().messages();
        User user = User.findById(id_user);
        //Si el usuario existe
        if (user != null) {
            //Se obtiene el apiKey de la cadena
            String apiKey = request().getQueryString("apiKey");
            //Si el apiKey del usuario con el id indicado coincide con el apiKey suministrado ejecutamos la operación
            if (user.getApiKey().getKey().matches(apiKey)) {
                if (user.delete()) {
                    return ok(messages.at("user.deleted"));
                } else {
                    return Results.internalServerError(messages.at("user.deletedFailed"));
                }
            }
            return Results.badRequest(messages.at("user.authorization"));
        }
        //Por idempotencia, aunque no exista el usuario, la respuesta debe ser correcta.
        return ok(messages.at("user.deleted"));

        //TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
        //TODO Sólo pueden borrar un usuario el propio usuario y el administrador
    }


    /**
     * Método para obtener un listado de los usuarios
     *
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserCollection() {

        Messages messages = Http.Context.current().messages();

        //Obtenemos la página
        Integer page = Integer.parseInt(request().getQueryString("page"));
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findAll(page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(messages.at("user.listEmpty"));
            } else if (request().accepts("application/json")) {
                return Results.notFound(messages.at("user.listEmpty"));
            }
            return status(415); //Unsupported media type
        }

        //Si la lista tiene usuarios
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(usersList)));
        }
        return Results.status(415, new ErrorObject("X", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
    }

}
