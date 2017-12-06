package controllers;

import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;

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
     * @param nick Nombre del usuario del que se quiere obtener la información
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUser(String nick) {

        //TODO Comprobar que el usuario existe, el formato y el idioma de la respuesta
        return Results.ok();
    }

    /**
     * @param nick Nombre del usuario del que se quiere realizar una modificación de los datos
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result updateUser(String nick) {

        //TODO Comprobar que el usuario existe. Sólo pueden modificar los datos de un usuario el propio usuario o el administrador
        return Results.ok();
    }

    /**
     * @param nick Nombre del usuario que se quiere borrar
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result deleteUser(String nick) {

        //TODO Comprobar que el usuario existe y borrar. Aunque ya no exista se debe dar una respuesta correcta (idempotencia)
        //TODO Sólo pueden borrar un usuario el propio usuario y el administrador
        return Results.ok();
    }


    //TODO PENDIENTE DE APROBACIÓN

    /**
     * @param page Página que se quiere obtener
     * @return Indica si se ha realizado correctamente o no la operación
     */
    public Result retrieveUserCollection(Integer page) {

        //TODO Comprobar el formato y el idioma de la respuesta
        return Results.ok();
    }


}
