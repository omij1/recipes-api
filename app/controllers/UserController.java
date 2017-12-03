package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

public class UserController extends Controller {

    /**
     * @return Indica si el usuario se creó con éxito o no
     */

    public Result createUser() {

        //TODO Comprobar que el usuario que se quiere crear no existe
        return Results.ok();
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
