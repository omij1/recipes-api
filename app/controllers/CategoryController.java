package controllers;

import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.PagedList;
import models.Category;
import models.Recipe;


/**
 * Clase controladora de los métodos de acción del recurso category.
 *
 * @author MIMO
 */

public class CategoryController extends Controller {

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
     * Método que permite al administrador crear una nueva categoría de recetas.
     *
     * @return Respuesta que indica si la categoría se creó correctamente o si hubo algún problema
     */
    @Security.Authenticated(Authorization.class)
    public Result createCategory() {

        //Le asigno el contexto actual del método de acción
        messages = Http.Context.current().messages();

        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Formulario para obtener los datos de la petición
        Form<Category> f = formFactory.form(Category.class).bindFromRequest();
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Objeto Category donde se guardan los datos de la petición
        Category c = f.get();

        //Comprobación de la existencia de la categoría y guardado en caso de que no exista
        if (loggedUser.getAdmin()) {
            if (!c.checkCategory()) {
                return Results.created(messages.at("category.created"));
            }
            return Results.status(409, new ErrorObject("4", messages.at("category.alreadyExist")).convertToJson()).as("application/json");
        }
        return Results.status(401, messages.at("user.authorization"));

    }

    /**
     * Método que permite ver la categoría de recetas correspondiente a un id
     *
     * @param id El identificador de la categoría de recetas
     * @return La categoría de recetas correspondiente
     */
    public Result retrieveCategory(Long id) {

        //Le asigno el contexto actual del método de acción
        messages = Http.Context.current().messages();

        //Se busca la categoría solicitada y en caso de encontrarla se muestra al usuario
        Category c = Category.findByCategoryId(id);
        if (c == null) {
            return Results.notFound(messages.at("category.notExist"));
        }

        if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(c)));
        } else if (request().accepts("application/xml")) {
            return ok(views.xml._category.render(c));
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que permite al administrador actualizar una categoría de recetas.
     *
     * @param id Id de la categoría de recetas que se desea actualizar
     * @return Respuesta indicativa del éxito o fracaso de la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result updateCategory(Long id) {

        messages = Http.Context.current().messages();

        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Formulario para obtener los datos de la petición
        Form<Category> f = formFactory.form(Category.class).bindFromRequest();
        if (f.hasErrors()) {
            return Results.status(409, f.errorsAsJson());
        }

        //Objeto Category donde se guardan los datos de la petición
        Category updateCategory = f.get();
        Category c = Category.findByCategoryId(id);

        //Se busca la categoría que se quiere actualizar y se actualiza

        if (loggedUser.getAdmin()) {
            if (c == null) {
                return Results.notFound(messages.at("category.notExist"));
            }
            updateCategory.setId(c.getId());
            updateCategory.setCategoryName(updateCategory.getCategoryName().toUpperCase());
            updateCategory.update();
            return ok(messages.at("category.updated"));
        }
        return Results.status(401, messages.at("user.authorization"));

    }

    /**
     * Método que permite al administrador eliminar una categoría de recetas de la base de datos.
     *
     * @param id Id de la categoría de recetas que se desea borrar
     * @return Respuesta indicativa del estado de la operación
     */
    @Security.Authenticated(Authorization.class)
    public Result deleteCategory(Long id) {

        messages = Http.Context.current().messages();

        User loggedUser = (User) Http.Context.current().args.get("loggedUser");

        //Se busca la categoría que se desea borrar y se elimina en caso de que exista
        if (loggedUser.getAdmin()) {
            Category c = Category.findByCategoryId(id);
            if (c != null) {
                if (c.delete()) {
                    return ok(messages.at("category.deleted"));
                }
                return internalServerError(messages.at("category.deletedFailed"));
            }
            return ok(messages.at("category.deleted")); //Por idempotencia
        }
        return Results.status(401, messages.at("user.authorization"));

    }

    /**
     * Método que permite visualizar todas las categorías de recetas existentes.
     *
     * @return Respuesta que muestra las categorías de recetas existentes o error
     */
    public Result retrieveCategoryCollection() {

        messages = Http.Context.current().messages();

        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
        }
        Integer page = Integer.parseInt(pageString);

        //Se obtienen las categorías de recetas de forma paginada
        PagedList<Category> list = Category.findPage(page);
        List<Category> categories = list.getList();
        Integer number = list.getTotalCount();
        
        //Si no hay categorias
        if(categories.isEmpty()) {
        		return Results.notFound(messages.at("category.empty"));
        }

        //Se ordenan las categorías de recetas alfabéticamente y se muestran al usuario
        sortAlphabetically(categories);
        if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(categories))).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.categories.render(categories)).withHeader("X-Count", number.toString());
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

        PagedList<Recipe> list = Recipe.findRecipesByCategory(id, page);
        List<Recipe> recipes = list.getList();
        Integer number = list.getTotalCount();
        
        //Se obtienen las recetas de la categoría elegida y se muestran al usuario
        Category c = Category.findByCategoryId(id);
        if (c == null) {
            return Results.notFound(messages.at("category.notExist"));
        }
        
        //Si no hay recetas de esa categoría
        if(recipes.isEmpty()) {
        		return Results.notFound(messages.at("recipe.empty"));
        }

        if (request().accepts("application/json")) {
            return ok(Json.prettyPrint(Json.toJson(recipes))).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.recipes.render(recipes)).withHeader("X-Count", number.toString());
        }
        return Results.status(415, messages.at("wrongOutputFormat"));

    }

    /**
     * Método que ordena alfabéticamente las categorías de recetas
     *
     * @param categories Lista con las categorías de recetas
     */
    private void sortAlphabetically(List<Category> categories) {

        if (categories.size() > 0) {
            Collections.sort(categories, new Comparator<Category>() {

                @Override
                public int compare(Category o1, Category o2) {
                    return o1.getCategoryName().compareTo(o2.getCategoryName());
                }
            });
        }
    }

}
