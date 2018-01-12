package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.PagedList;
import models.Category;
import models.Recipe;
import models.User;
import play.cache.SyncCacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;

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
     * Variable caché
     */
    @Inject
    private SyncCacheApi cache;

    /**
     * Variable para presentar los mensajes al usuario según el idioma
     */
    private Messages messages;


    /**
     * Método que permite crear una nueva receta.
     *
     * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
     */
    @Security.Authenticated(Authorization.class)
    public Result createRecipe() {

        messages = Http.Context.current().messages();//le asigno el contexto actual del método de acción

        //Obtenemos el usuario que crea la receta de la cabecera Authorization
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Formulario para obtener los datos de la petición
        Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest();
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Objeto Recipe donde se guardan los datos de la petición
        Recipe r = f.get();

        //Asignamos el creador de la receta
        r.setUser(loggedUser);
        if (r.checkCategory()) {
            if (r.checkRecipe()) {
                //Borramos el caché
                String key = "userRecipes-" + r.getUser().getId();
                cache.remove(key);
                key = "userRecipes-" + r.getUser().getId() + "-json";
                cache.remove(key);
                return Results.created(messages.at("recipe.created"));
            } else {
                return Results.status(409, new ErrorObject("1", messages.at("recipe.alreadyExist")).convertToJson()).as("application/json");
            }
        } else {
            return Results.notFound(messages.at("category.notExist"));
        }

    }

    /**
     * Método que permite obtener la información de una receta.
     *
     * @param id Id de la receta que se desea visualizar
     * @return Respuesta que muestra la receta o error si se produjo alguno
     */
    public Result retrieveRecipe(Long id) {

        messages = Http.Context.current().messages();

        //Comprobamos si la receta está en caché
        String key = "recipe-" + id;
        Recipe recipe = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (recipe == null) {
            recipe = Recipe.findById(id);
            cache.set(key, recipe);
        }

        //Si la receta no existe
        if (recipe == null) {
            return Results.notFound(messages.at("recipe.wrongId"));
        }

        //Si la receta existe
        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "recipe-" + id + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(recipe);
                cache.set(key, json);
            }
            return ok(Json.prettyPrint(json));
        } else if (request().accepts("application/xml")) {
            return ok(views.xml._recipe.render(recipe));
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que permite actualizar la información de una receta.
     *
     * @param id Id de la receta que se desea actualizar
     * @return Respuesta que indica el resultado de la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result updateRecipe(Long id) {

        messages = Http.Context.current().messages();

        //Obtenemos el usuario que quiere modificar la receta
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        if (!request().hasBody()) {
            return Results.badRequest(messages.at("emptyParams"));
        }

        //Buscamos la receta
        Recipe r = Recipe.findById(id);
        if (r == null) {
            return Results.notFound(messages.at("recipe.wrongId"));
        }

        //Obtenemos el usuario que ha creado esa receta
        User user = r.getUser();

        //Comprobamos que coinciden el creador y el que la quiere modificar
        if (user.getId() == loggedUser.getId() || loggedUser.getAdmin()) {
            Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest();
            if (f.hasErrors()) {
                return Results.ok(f.errorsAsJson());
            }
            if (updateFields(r, f)) {
                return ok(messages.at("recipe.updated"));
            }
            return Results.notFound(messages.at("category.notExist"));
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
        String key = "recipe-" + r.getId();
        cache.remove(key);
        key = "recipe-" + r.getId() + "-json";
        cache.remove(key);
        r.save();
        return true;
    }

    /**
     * Método que permite eliminar una receta.
     *
     * @param id Id de la receta que se desea eliminar
     * @return Respuesta que indica si la receta se borró o si se produjo un error
     */
    @Security.Authenticated(Authorization.class)
    public Result deleteRecipe(Long id) {

        messages = Http.Context.current().messages();

        //Obtenemos el usuario que quiere borrar la receta
        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Miramos a ver si la receta que se quiere eliminar existe
        Recipe r = Recipe.findById(id);
        if (r == null) {
            return Results.notFound(messages.at("recipe.wrongId"));
        }

        //Buscamos al usuario que hizo la receta
        User user = r.getUser();

        //Comprobamos que coinciden
        if (user.getId() == loggedUser.getId() || loggedUser.getAdmin()) {
            if (r.delete()) {
                String key = "recipe-" + r.getId();
                cache.remove(key);
                key = "recipe-" + r.getId() + "-json";
                cache.remove(key);
                return ok(messages.at("recipe.deleted"));
            }
            return internalServerError();
        }
        return Results.status(401, messages.at("user.authorization"));
    }

    /**
     * Método que permite visualizar las recetas existentes sin tener en cuenta su categoría.
     *
     * @return Respuesta que muestra todas las recetas existentes
     */

    public Result retrieveRecipeCollection() {

        messages = Http.Context.current().messages();

        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si las recetas están en caché
        String key = "recipeList-" + page;
        PagedList<Recipe> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = Recipe.findPage(page);
            cache.set(key, list, 60 * 2);
        }
        List<Recipe> recipes = list.getList();
        Integer number = list.getTotalCount();
        
        //Si no hay recetas, poco habitual
        if(recipes.isEmpty()) {
        		return Results.notFound(messages.at("recipe.emptyList"));
        }

        //Se ordenan las recetas alfabéticamente y se devuelven al usuario
        sortAlphabetically(recipes);
        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "recipeList-" + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(recipes);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
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

        //Comprobamos si la receta está en caché
        String key = "recipe-" + title.toUpperCase();
        Recipe recipe = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (recipe == null) {
            recipe = Recipe.findByName(title.toUpperCase());
            cache.set(key, recipe);
        }

        //Miramos si la receta solicitada existe
        if (recipe == null) {
            return Results.notFound(messages.at("recipe.wrongName"));
        }

        //Se devuelve la receta al usuario
        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "recipe-" + title.toUpperCase() + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(recipe);
                cache.set(key, json);
            }
            return ok(Json.prettyPrint(json));
        } else if (request().accepts("application/xml")) {
            return ok(views.xml._recipe.render(recipe));
        }

        return Results.status(415, messages.at("wrongOutputFormat"));

    }
    
    /**
     * Método que permite visualizar todas las recetas pertenecientes a una categoría.
     *
     * @param id Id de la categoría de recetas que se quiere visualizar
     * @return Devuelve las recetas pertenecientes a la categoría especificada o error
     */
    public Result retrieveRecipesByCategory(Long id) {

        messages = Http.Context.current().messages();

        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la categoría está en caché
        String key = "category-" + id;
        Category category = cache.get(key);
        //Si no la tenemos en caché, la buscamos y la guardamos
        if (category == null) {
            category = Category.findByCategoryId(id);
            cache.set(key, category);
        }

        //Si la categoría no existe
        if (category == null) {
            return Results.notFound(messages.at("category.notExist"));
        }

        //Comprobamos si la lista de recetas de esa categoría está en caché
        key = "categoryRecipes-" + id + page;
        PagedList<Recipe> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = Recipe.findRecipesByCategory(id, page);
            cache.set(key, list, 60 * 2);
        }
        List<Recipe> recipes = list.getList();
        Integer number = list.getTotalCount();

        //Si no hay recetas de esa categoría
        if(recipes.isEmpty()) {
            return Results.notFound(messages.at("recipe.empty"));
        }

        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "categoryRecipes-" + id + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(recipes);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.recipes.render(recipes)).withHeader("X-Count", number.toString());
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
