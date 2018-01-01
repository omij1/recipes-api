package controllers;

import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

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
 * @author MIMO
 *
 */

public class CategoryController extends Controller{
	
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
	 * Método que permite crear una nueva categoría de recetas. Corresponde con un POST.
	 * @return Respuesta que indica si la categoría se creó correctamente o si hubo algún problema
	 */
	public Result createCategory() {
		//TODO solo puede crear una categoria el admin
		//TODO Comprobar si el apiKey existe y si se ha introducido
		String apiKey = request().getQueryString("apiKey");

		//Le asigno el contexto actual del método de acción
		messages = Http.Context.current().messages();
		
		//Formulario para obtener los datos de la petición
		Form<Category> f = formFactory.form(Category.class).bindFromRequest(); 
		if(f.hasErrors()) {
			return Results.status(409, f.errorsAsJson());
		}
		
		//Objeto Category donde se guardan los datos de la petición
		Category c = f.get();
		
		//Comprobación de la existencia de la categoría y guardado en caso de que no exista
		if(!c.checkCategory()) {
			return Results.created(messages.at("category.created"));
		}
		else {//TODO Revisar codigos de errores del ErrorObject
			return Results.status(409, new ErrorObject("1",messages.at("category.alreadyExist")).convertToJson()).as("application/json");
		}
	}
	
	/**
	 * Método que ver la categoría de recetas correspondiente a un id
	 * @param id El identificador de la categoría de recetas
	 * @return La categoría de recetas correspondiente
	 */
	public Result retrieveCategory(Long id) {
		
		//Le asigno el contexto actual del método de acción
		messages = Http.Context.current().messages();
		
		//Se busca la categoría solicitada y en caso de encontrarla se muestra al usuario
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}

		if(request().accepts("application/json")) {
			return ok(Json.prettyPrint(Json.toJson(c)));
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml._category.render(c));
		}
		//TODO Revisar codigos de errores del ErrorObject
		return Results.status(415, new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
	
	}
	
	/**
	 * Método que permite actualizar una categoría de recetas. Corresponde con un PUT.
	 * @param id Id de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(Long id) {
		//TODO solo el admin puede hacerlo
		//TODO Comprobar si el apiKey existe y si se ha introducido
		String apiKey = request().getQueryString("apiKey");

		messages = Http.Context.current().messages();
		
		//Formulario para obtener los datos de la petición
		Form<Category> f = formFactory.form(Category.class).bindFromRequest(); 
		if(f.hasErrors()) {
			return Results.status(409, f.errorsAsJson());
		}
		
		//Objeto Category donde se guardan los datos de la petición
		Category updateCategory = f.get();
		Category c = Category.findByCategoryId(id);
		
		//Se busca la categoría que se quiere actualizar y se actualiza
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}
		else {
			updateCategory.setId(c.getId());
			updateCategory.setCategoryName(updateCategory.getCategoryName().toUpperCase());
			updateCategory.update();
			return ok(messages.at("category.updated"));
		}		
		
	}
	
	/**
	 * Método que permite eliminar una categoría de recetas de la base de datos. Corresponde con un DELETE.
	 * @param id Id de la categoría de recetas que se desea borrar
	 * @return Respuesta indicativa del estado de la operación 
	 */
	public Result deleteCategory(Long id) {
		//TODO solo el admin puede borrar una categoria. 
		//TODO Comprobar si el apiKey existe y si se ha introducido
		String apiKey = request().getQueryString("apiKey");

		messages = Http.Context.current().messages();
		
		//Se busca la categoría que se desea borrar y se elimina en caso de que exista
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}
		else {
			if(c.delete()) {
				return ok(messages.at("category.deleted"));
			}
			else {
				return internalServerError();
			}
		}
		
	}
	
	/**
	 * Método que permite visualizar todas las categorías de recetas existentes. Corresponde con un GET.
	 * @return Respuesta que muestra las categorías de recetas existentes o error
	 */
	public Result retrieveCategoryCollection() {
		
		Integer page = Integer.parseInt(request().getQueryString("page"));
		
		messages = Http.Context.current().messages();	

		//Se obtienen las categorías de recetas de forma paginada
		PagedList<Category> list = Category.findPage(page);
		List<Category> categories = list.getList();
		Integer number = list.getTotalCount();

		//Se ordenan las categorías de recetas alfabéticamente y se muestran al usuario
		sortAlphabetically(categories);
		if(request().accepts("application/json")) {
			return ok(Json.prettyPrint(Json.toJson(categories))).withHeader("X-Count", number.toString());
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.categories.render(categories)).withHeader("X-Count", number.toString());
		}
		//TODO Revisar codigos de errores del ErrorObject
		return Results.status(415,new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
		
	}
	
	/**
	 * Método que permite visualizar todas las recetas pertenecientes a una categoría. Corresponde con un GET.
	 * @param id Id de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(Long id) {
		
		messages = Http.Context.current().messages();
		
		//Se obtienen las recetas de la categoría elegida y se muestran al usuario
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}

		if(request().accepts("application/json")) {
			return ok(Json.prettyPrint(Json.toJson(c.relatedRecipes)));
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.recipes.render(c.relatedRecipes));
		}
		//TODO Revisar codigos de errores del ErrorObject
		return Results.status(415,new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
	
	}
	
	/**
	 * Método que ordena alfabéticamente las categorías de recetas
	 * @param categories Lista con las categorías de recetas
	 */
	private void sortAlphabetically(List<Category> categories) {
		
		if(categories.size() > 0) {
			Collections.sort(categories, new Comparator<Category>() {

				@Override
				public int compare(Category o1, Category o2) {
					return o1.getCategoryName().compareTo(o2.getCategoryName());
				}
			});
		}
	}
	
}
