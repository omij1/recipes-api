package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.PagedList;
import models.Category;
import models.Difficulty;
import models.Recipe;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Clase controladora de los métodos de acción del recurso recipe.
 *
 * @author MIMO
 */

public class RecipeController extends Controller {

    /**
     * Variable formulario
     */
    @Inject
    FormFactory formFactory;

    /**
     * Variable para presentar los mensajes al usuario según el idioma
     */
    private Messages messages;


    /**
     * Método que permite crear una nueva receta. Corresponde con un POST
     *
     * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
     */
    public Result createRecipe() {

        messages = Http.Context.current().messages();//le asigno el contexto actual del método de acción

        String apiKey = request().getQueryString("apiKey");
        if (apiKey == null) {
            return Results.status(409, messages.at("apiKey.null"));
        }

        //Formulario para obtener los datos de la petición
        Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest();
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Objeto Recipe donde se guardan los datos de la petición
        Recipe r = f.get();

        //Usuario que sube la receta
        User u = User.findByApiKey(apiKey);

        if (u == null) {
            return Results.notFound(messages.at("user.notExists"));
        }

        //Asignamos el creador de la receta
        r.setUser(u);
        if (r.checkCategory()) {
            if (r.checkRecipe()) {
                return Results.ok(messages.at("recipe.created"));
            } else {
                return Results.status(409, new ErrorObject("1", messages.at("recipe.alreadyExist")).convertToJson()).as("application/json");
            }
        } else {
            return Results.notFound(messages.at("category.notExist"));
        }

    }

    /**
     * Método que permite obtener la información de una receta. Corresponde con un GET.
     *
     * @param id Id de la receta que se desea visualizar
     * @return Respuesta que muestra la receta o error si se produjo alguno
     */
    public Result retrieveRecipe(Long id) {
        //TODO Poner cache
        messages = Http.Context.current().messages();

        //Miramos a ver si la receta solicitada existe y en caso afirmativo se muestra al usuario
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            return Results.notFound(messages.at("recipe.wrongId"));
        } else {
            if (request().accepts("application/json")) {
                return ok(Json.prettyPrint(Json.toJson(recipe)));
            } else if (request().accepts("application/xml")) {
                return ok(views.xml._recipe.render(recipe));
            }

            return Results.status(415, messages.at("wrongOutputFormat"));
        }

    }

    /**
     * Método que permite actualizar la información de una receta. Corresponde con un PUT.
     *
     * @param id Id de la receta que se desea actualizar
     * @return Respuesta que indica el resultado de la operación
     */
    public Result updateRecipe(Long id) {

        messages = Http.Context.current().messages();
        //TODO Solo puede actualizar una receta el admin o el creador (falta el admin)
        String apiKey = request().getQueryString("apiKey");
        if (apiKey == null) {
            return Results.status(409, new ErrorObject("2", messages.at("apiKey.null")).convertToJson()).as("application/json");
        }

        if (!request().hasBody()) {
            return Results.badRequest(messages.at("emptyParams"));
        }

        //Buscamos al usuario que coincide con la clave enviada y confirmamos que existe
        User user = User.findById(id);
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Comprobamos que coinciden el apikey enviado con el user indicado
        if (user.getApiKey().getKey().matches(apiKey)) {
            //Miramos a ver si la receta que se quiere actualizar existe y se actualiza en caso afirmativo
            Recipe r = Recipe.findById(id);
            if (r == null) {
                return Results.notFound(messages.at("recipe.wrongId"));
            } else {
                Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest();
                if (f.hasErrors()) {
                    return Results.ok(f.errorsAsJson());
                }
                if (updateFields(r, f)) {
                    return ok(messages.at("recipe.updated"));
                } else {
                    return Results.notFound(messages.at("category.notExist"));
                }
            }
        }
        return Results.status(401, messages.at("user.authorization"));
    }

    /**
     * Método que actualiza la información de una receta en la base de datos. Se ha utilizado un form para soportar posibles
     * ampliaciones o modificaciones del modelo.
     *
     * @param r La receta que se va a modificar
     * @param f La nueva información
     * @return Verdadero si la receta se actualizó y falso en caso contrario
     */
    private boolean updateFields(Recipe r, Form<Recipe> f) {

        r.setTitle(f.get().getTitle().toUpperCase());

        r.updateRecipeIngredients(f.get().getIngredients());

        r.setSteps(f.get().getSteps());
        r.setTime(f.get().getTime());
        r.setDifficulty(f.get().getDifficulty());

        r.setCategory(f.get().getCategory());
        if (!r.checkCategory()) {
            return false;
        }
        r.save();
        return true;
    }

    /**
     * Método que permite eliminar una receta. Corresponde con un DELETE.
     *
     * @param id Id de la receta que se desea eliminar
     * @return Respuesta que indica si la receta se borró o si se produjo un error
     */
    public Result deleteRecipe(Long id) {

        messages = Http.Context.current().messages();

        // TODO Comprobar que el usuario que quiere borrar la receta es el admin o el creador (falta el admin)
        String apiKey = request().getQueryString("apiKey");
        if (apiKey == null) {
            return Results.status(409, new ErrorObject("2", messages.at("apiKey.null")).convertToJson()).as("application/json");
        }

        //Buscamos al usuario que coincide con la clave enviada y confirmamos que existe
        User user = User.findById(id);
        if (user == null) {
            return Results.notFound(messages.at("user.wrongId"));
        }

        //Comprobamos que coinciden el apikey enviado con el user indicado
        if (user.getApiKey().getKey().matches(apiKey)) {
            //Miramos a ver si la receta que se quiere eliminar existe
            Recipe r = Recipe.findById(id);
            if (r == null) {
                return Results.notFound(messages.at("recipe.wrongId"));
            } else {
                if (r.delete()) {
                    return ok(messages.at("recipe.deleted"));
                } else {
                    return internalServerError();
                }
            }
        }
        return Results.status(401, messages.at("user.authorization"));
    }

    /**
     * Método que permite visualizar las recetas existentes sin tener en cuenta su categoría. Corresponde con un GET.
     *
     * @return Respuesta que muestra todas las recetas existentes
     */
    public Result retrieveRecipeCollection() {

        messages = Http.Context.current().messages();

        //Se obtienen las recetas de forma paginada
        Integer page = Integer.parseInt(request().getQueryString("page"));
        PagedList<Recipe> list = Recipe.findPage(page);
        List<Recipe> recipes = list.getList();
        Integer number = list.getTotalCount();

        //Se ordenan las recetas alfabéticamente y se devuelven al usuario
        sortAlphabetically(recipes);
        if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(recipes))).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.recipes.render(recipes)).withHeader("X-Count", number.toString());
        }

        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que permite buscar una receta por su título
     *
     * @return Respuesta que muestra la receta o error
     */
    public Result searchRecipe() {

        messages = Http.Context.current().messages();

        //Miramos a ver si el usuario ha introducido el nombre de una receta
        String title = request().getQueryString("title");
        if (title == null) {
            return Results.badRequest(messages.at("recipe.emptyName"));
        }

        //Miramos si la receta solicitada existe
        Recipe recipe = Recipe.findByName(title.toUpperCase());
        if (recipe == null) {
            return Results.notFound(messages.at("recipe.wrongName"));
        }

        //Se devuelve la receta al usuario
        if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(recipe)));
        } else if (request().accepts("application/xml")) {
            return ok(views.xml._recipe.render(recipe));
        }

        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que ordena alfabéticamente las recetas
     *
     * @param recipes Lista con las recetas
     */
    private void sortAlphabetically(List<Recipe> recipes) {

        if (recipes.size() > 0) {
            Collections.sort(recipes, new Comparator<Recipe>() {

                @Override
                public int compare(Recipe o1, Recipe o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
        }
    }
}
