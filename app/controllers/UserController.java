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

        /**
         * Creación de objeto Form para obtener los datos de la petición
         */
        Form<User> f = formFactory.form(User.class).bindFromRequest();

        //Comprobar si hay errores
        if (f.hasErrors()) {
            //TODO Crear objeto Error
            return ok(f.errorsAsJson());
        }
        /**
         * Objeto User donde se guarda la información de la petición
         */
        User user = f.get();


        //TODO Crear formato de respuestas.
        //TODO Crear objeto respuestas
        //TODO Internacionalización de las respuestas
        /**
         * Validación y guardado en caso de que el nick no exista. EN caso contrario se muestra el error
         * correspondiente
         */
        if (user.checkAndSave()) {
            //TODO Personalizar mensaje e internacionalización
            return Results.created();
        } else {
            //TODO Crear objeto Error
            return Results.status(409, "{\"Error:\": \"usuario repetido\"}");
        }

    }

    /**
     * Método para obtener los datos de un usuario
     *
     * @param nick Nombre del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUser(String nick) {

        User user = User.findByNick(nick);

        /**
         * Si el nick no existe, se devuelve un error
         */
        if (user == null) {
            return Results.notFound(); //TODO cambiar cuando se implemente el objeto error
        }

        /**
         * Formato de respuesta dependiendo de que lo que acepte la petición
         */
        if (request().accepts("application/xml")) {
            return ok(views.xml.user.render(user));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(user));
        } else {
            //TODO cambiar cuando se implemente el objeto Error
            return status(415); //Unsupported media type
        }


        //TODO idioma de la respuesta
    }

    /**
     * Método para actualizar los datos de un usuario
     *
     * @param nick Nombre del usuario del que se quiere realizar una modificación de los datos
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result updateUser(String nick) {

        /**
         * Creación de objeto Form para obtener los datos de la petición
         */
        Form<User> f = formFactory.form(User.class).bindFromRequest();

        //Comprobar si hay errores
        if (f.hasErrors()) {
            //TODO Crear objeto Error
            return ok(f.errorsAsJson());
        }
        /**
         * Objeto User donde se guarda la información de la petición
         */
        User updateUser = f.get();
        User user = User.findByNick(nick);

        /**
         * Comprobar si existe el usuario con el nick indicado
         */
        if (user == null) {
            return Results.notFound();
        }

        /**
         * Si existe, asignamos el id del usuario a los nuevos datos y actualizamos
         */
        updateUser.setId_user(user.getId_user());
        updateUser.update();
        return ok();

        //TODO Sólo pueden modificar los datos de un usuario el propio usuario o el administrador
    }

    /**
     * Método para borrar un usuario
     *
     * @param nick Nombre del usuario que se quiere borrar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result deleteUser(String nick) {

        User user = User.findByNick(nick);
        /**
         * Si el usuario existe, borrar
         */
        if (user != null) {
            if (user.delete()) {
                return ok();
            } else {
                return Results.internalServerError("Error al eliminar usuario"); //TODO cambiar cuando se implemente el objeto Error
            }
        }

        /**
         * Por idempotencia, aunque no exista el usuario, la respuesta debe ser correcta.
         */
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

        /**
         * Creamos un objeto de la clase PagedList para obtener la lista
         */
        PagedList<User> list = User.findAll(page);
        List<User> usersList = list.getList();

        if (usersList.isEmpty()) {
            if (request().accepts("application/xml")) {
                return ok(views.xml.users.render(usersList));
            } else if (request().accepts("application/json")) {
                return ok(Json.toJson(usersList));
            } else {
                return status(415); //Unsupported media type
            }
        }

        if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(usersList));
        } else if (request().accepts("application/json")) {
            return ok(Json.toJson(usersList));
        }

        return status(415); //Unsupported media type


        //TODO Comprobar el idioma de la respuesta
    }


}
