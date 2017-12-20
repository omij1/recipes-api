package controllers;


import io.ebean.PagedList;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.List;

public class UserController extends Controller {

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


        Form<User> f = formFactory.form(User.class).bindFromRequest(); //Creación de objeto Form para obtener los datos de la petición
        if (f.hasErrors()) {         //Comprobar si hay errores
            //TODO Crear objeto Error
            return ok(f.errorsAsJson());
        }
        User user = f.get();  //Objeto User donde se guarda la información de la petición


        //TODO Crear formato de respuestas.
        //TODO Crear objeto respuestas
        //TODO Internacionalización de las respuestas
        //Validación y guardado en caso de que el nick no exista. EN caso contrario se muestra el error correspondiente
        if (user.checkAndSave()) {
            //TODO Personalizar mensaje e internacionalización
            return Results.created("Usuario creado correctamente");
        } else {
            //TODO Crear objeto Error
            return Results.status(409, "{\"Error:\": \"usuario repetido\"}");
        }

    }

    /**
     * Método para obtener los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUser(Long id_user) {

        User user = User.findById(id_user);

        //Si el Id no existe, se devuelve un error
        if (user == null) {
            return Results.notFound(); //TODO cambiar cuando se implemente el objeto error
        }

       //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(user));
        }
        //TODO cambiar cuando se implemente el objeto Error
        return status(415); //Unsupported media type
        //TODO idioma de la respuesta
    }

    /**
     * Método para obtener los datos de un usuario a través de su nick
     *
     * @param nick nick del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByNick(String nick) {
        User user = User.findByNick(nick);
        //Si no existe ningún usuario con ese nick
        if (user == null) {
            return Results.notFound(); //TODO Cambiar cuando se implemente el objeto error
        }
        //Formato de respuesta dependiendo de lo que acepte la petición
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(user));        }
        return status(415); //Unsupported media type
    }


    /**
     * Método para buscar a todos los usuarios que tengan el nombre proporcionado como parámetro
     *
     * @param name nombre de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByName(String name, Integer page) {

        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByName(name, page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) { //TODO devuelve cadenas vacías o error???
            if (request().accepts("application/xml")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            } else if (request().accepts("application/json")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            }
            return status(415);  //Unsupported media type
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(usersList));
        }
        return status(415); //Unsupported media type
    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el apellido proporcionado como parámetro
     *
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserBySurname(String surname, Integer page) {

        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findBySurname(surname, page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            } else if (request().accepts("application/json")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            }
            return status(415);   //Unsupported media type
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(usersList));
        }
        return status(415);  //Unsupported media type
    }

    /**
     * Método que devuelve un listado de los usuarios que tienen el nombre y apellido proporcionados como parámetros
     *
     * @param name    nombre de los usuarios que se quieren buscar
     * @param surname apellido de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByFullName(String name, String surname, Integer page) {
        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByFullName(name, surname, page);
        List<User> userList = list.getList();

        //Si la lista está vacía
        if (userList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            } else if (request().accepts("application/json")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            }
            return status(415);  //Usupported media type
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(userList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(userList));
        }
        return status(415); //Unsupported media type

    }


    /**
     * Método que devuelve un listado de los usuarios que son de la ciudad proporcionada como parámetro
     *
     * @param city ciudad de los usuarios que se quieren buscar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserByCity(String city, Integer page) {

        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findByCity(city, page);
        List<User> userList = list.getList();

       //Si la lista está vacía
        if (userList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            } else if (request().accepts("application/json")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            }
            return status(415);  //Unsupported media type
        }

        //Si la lista no está vacía
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(userList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(userList));
        }
        return status(415);  //Unsupported media type

    }


    /**
     * Método para actualizar los datos de un usuario
     *
     * @param id_user Id del usuario del que se quiere realizar una modificación de los datos
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result updateUser(Long id_user) {

        //Creación de objeto Form para obtener los datos de la petición
        Form<User> f = formFactory.form(User.class).bindFromRequest();

        //Comprobar si hay errores
        if (f.hasErrors()) {
            //TODO Crear objeto Error
            return ok(f.errorsAsJson());
        }
        //Objeto User donde se guarda la información de la petición
        User updateUser = f.get();
        User user = User.findById(id_user);

        //Comprobar si existe el usuario con el Id indicado
        if (user == null) {
            return Results.notFound();
        }

        //Si existe, asignamos el id del usuario a los nuevos datos y actualizamos
        updateUser.setId_user(user.getId_user());
        updateUser.update();
        return ok();

        //TODO Sólo pueden modificar los datos de un usuario el propio usuario o el administrador
    }

    /**
     * Método para borrar un usuario
     *
     * @param id_user Id del usuario que se quiere borrar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result deleteUser(Long id_user) {

        User user = User.findById(id_user);
        //Si el usuario existe, borrar
        if (user != null) {
            if (user.delete()) {
                return ok();
            } else {
                return Results.internalServerError("Error al eliminar usuario"); //TODO cambiar cuando se implemente el objeto Error
            }
        }
        //Por idempotencia, aunque no exista el usuario, la respuesta debe ser correcta.
        return ok();
        //TODO Sólo pueden borrar un usuario el propio usuario y el administrador
    }


    /**
     * Método para obtener un listado de los usuarios
     *
     * @param page Página que se quiere obtener
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserCollection(Integer page) {

        //Creamos un objeto de la clase PagedList para obtener la lista
        PagedList<User> list = User.findAll(page);
        List<User> usersList = list.getList();

        //Si la lista está vacía
        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            } else if (request().accepts("application/json")) {
                return Results.notFound(); //TODO Cambiar cuando se implemente el objeto Errores
            }
            return status(415); //Unsupported media type
        }

        //Si la lista tiene usuarios
        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(usersList));
        }
        return status(415); //Unsupported media type
        //TODO Comprobar el idioma de la respuesta
    }


}
