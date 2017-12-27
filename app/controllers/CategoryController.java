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
		//TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
		String apiKey = request().getQueryString("apiKey");

		messages = Http.Context.current().messages();//le asigno el contexto actual del método de acción
		
		Form<Category> f = formFactory.form(Category.class).bindFromRequest(); 
		if(f.hasErrors()) {
			return Results.status(409, f.errorsAsJson());
		}
		
		Category c = f.get();
		if(!c.checkCategory()) {
			return Results.created(messages.at("category.created"));
		}
		else {
			//TODO todos los mensajes deberían estar guardados en algún sitio para no ponerlos directamente aqui
			return Results.status(409, new ErrorObject("1",messages.at("category.alreadyExist")).convertToJson()).as("application/json");
		}
	}
	
	/**
	 * Método que ver la categoría de recetas correspondiente a un id
	 * @param id El identificador de la categoría de recetas
	 * @return La categoría de recetas correspondiente
	 */
	public Result retrieveCategory(Long id) {
		
		messages = Http.Context.current().messages();//le asigno el contexto actual del método de acción
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.prettyPrint(Json.toJson(c)));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml._category.render(c));
			}

			return Results.status(415, new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
		}
		
	}
	
	/**
	 * Método que permite actualizar una categoría de recetas. Corresponde con un PUT.
	 * @param id Id de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(Long id) { // Referencia a https://stackoverflow.com/questions/7543391/how-to-update-an-object-in-play-framework
		//TODO solo el admin puede hacerlo
		//TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
		String apiKey = request().getQueryString("apiKey");

		messages = Http.Context.current().messages();
		
		JsonNode jn = request().body().asJson();
		if(!request().hasBody() || jn == null) {
			return Results.badRequest(messages.at("emptyParams"));
		}
		
		String newCategory = jn.get("categoryName").asText();
		if(newCategory == null || newCategory == "") {
			return Results.badRequest(messages.at("category.wrongFormat"));
		}
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}
		else {
			c.setCategoryName(newCategory.toUpperCase());
			c.save();
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
		//TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
		String apiKey = request().getQueryString("apiKey");

		messages = Http.Context.current().messages();
		
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

		PagedList<Category> list = Category.findPage(page);
		List<Category> categories = list.getList();
		Integer number = list.getTotalCount();

		sortAlphabetically(categories);
		if(request().accepts("application/json")) {
			return ok(Json.prettyPrint(Json.toJson(categories))).withHeader("X-Count", number.toString());
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.categories.render(categories)).withHeader("X-Count", number.toString());
		}

		return Results.status(415,new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
		
	}
	
	/**
	 * Método que permite visualizar todas las recetas pertenecientes a una categoría. Corresponde con un GET.
	 * @param id Id de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(Long id) {
		
		messages = Http.Context.current().messages();
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound(messages.at("category.notExist"));
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.prettyPrint(Json.toJson(c.relatedRecipes)));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml.recipes.render(c.relatedRecipes));
			}

			return Results.status(415,new ErrorObject("2", messages.at("wrongOutputFormat")).convertToJson()).as("application/json");
		}
		
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
