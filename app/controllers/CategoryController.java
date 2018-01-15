package controllers;

import models.User;
import play.cache.SyncCacheApi;
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
     * Variable caché
     */
    @Inject
    private SyncCacheApi cache;

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

        //Si la categoría existe
        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "category-" + id + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(category);
                cache.set(key, json);
            }
            return ok(Json.prettyPrint(json));
        } else if (request().accepts("application/xml")) {
            return ok(views.xml._category.render(category));
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

        //Comprobamos que si actualiza el título, no coja uno repetido
        if (Category.findByCategoryName(updateCategory.getCategoryName().toUpperCase()).getId() != id) {
            return Results.status(409, new ErrorObject("8", messages.at("category.titleAlreadyExists")).convertToJson()).as("application/json");
        }

        //Obtenemos mediante el id pasado como parámetro la categoría que se quiere modificar
        Category c = Category.findByCategoryId(id);

        //Se busca la categoría que se quiere actualizar y se actualiza

        if (loggedUser.getAdmin()) {
            if (c == null) {
                return Results.notFound(messages.at("category.notExist"));
            }
            updateCategory.setId(c.getId());
            updateCategory.setCategoryName(updateCategory.getCategoryName().toUpperCase());
            updateCategory.update();
            String key = "category-" + id;
            cache.remove(key);
            key = "category-" + id + "-json";
            cache.remove(key);
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
                    String key = "category-" + id;
                    cache.remove(key);
                    key = "category-" + id + "-json";
                    cache.remove(key);
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

        //Obtenemos la página
        String pageString = request().getQueryString("page");
        if (pageString == null) {
            return Results.status(409, new ErrorObject("5", messages.at("page.null")).convertToJson()).as("application/json");
        }
        Integer page = Integer.parseInt(pageString);

        //Comprobamos si la lista está en caché
        String key = "categoriesList-" + page;
        PagedList<Category> list = cache.get(key);
        //Si no lo tenemos en caché, lo buscamos y lo guardamos
        if (list == null) {
            list = Category.findPage(page);
            cache.set(key, list, 60 * 2);
        }
        List<Category> categories = list.getList();
        Integer number = list.getTotalCount();
        
        //Si no hay categorias
        if(categories.isEmpty()) {
        		return Results.notFound(messages.at("category.empty"));
        }

        //Se ordenan las categorías de recetas alfabéticamente y se muestran al usuario
        sortAlphabetically(categories);
        if (request().accepts("application/json")) {
            //Buscamos la respuesta en caché
            key = "categoriesList-" + page + "-json";
            JsonNode json = cache.get(key);
            //Si no está, la creamos y la guardamos en caché
            if (json == null) {
                json = Json.toJson(categories);
                cache.set(key, json, 60 * 2);
            }
            return ok(Json.prettyPrint(json)).withHeader("X-Count", number.toString());
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.categories.render(categories)).withHeader("X-Count", number.toString());
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
