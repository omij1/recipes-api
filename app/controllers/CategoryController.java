package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	 * Lista que contiene los datos de las categorías de las recetas
	 */
	private List<Category> categorias = new ArrayList<>();
	
	
	/**
	 * Método que permite crear una nueva categoría de recetas. Corresponde con un POST.
	 * @return Respuesta que indica si la categoría se creó correctamente o si hubo algún problema
	 */
	public Result createCategory() {
		//TODO solo puede crear una categoria el admin
		JsonNode jn = request().body().asJson();
		if(!request().hasBody() || jn == null) {
			return Results.badRequest("Parámetros obligatorios");
		}
		
		String categoryName = jn.get("categoryName").asText();
		if(categoryName == null || categoryName == "") {
			return Results.badRequest("La categoría introducida no tiene el formato correcto");
		}
		
		Category categoria = new Category(categoryName.toUpperCase());
		if(!categoria.checkCategory()) {
			return Results.created("Categoría creada correctamente");
		}
		else {
			return Results.status(409, "Categoría ya existente");
		}
	}
	
	/**
	 * Método que ver la categoría de recetas correspondiente a un id
	 * @param id El identificador de la categoría de recetas
	 * @return La categoría de recetas correspondiente
	 */
	public Result retrieveCategory(String id) {
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound("La categoría introducida no existe");
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.toJson(c));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml._category.render(c));
			}
			else {
				return Results.status(415);
			}
		}
		
	}
	
	/**
	 * Método que permite actualizar una categoría de recetas. Corresponde con un PUT.
	 * @param name Nombre de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(String id) { // Referencia a https://stackoverflow.com/questions/7543391/how-to-update-an-object-in-play-framework
		//TODO solo el admin puede hacerlo
		JsonNode jn = request().body().asJson();
		if(!request().hasBody() || jn == null) {
			return Results.badRequest("Parámetros obligatorios");
		}
		
		String newCategory = jn.get("newCategory").asText();
		if(newCategory == null || newCategory == "") {
			return Results.badRequest("La nueva categoría introducida no tiene el formato correcto");
		}
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound("La categoría introducida no existe");
		}
		else {
			c.setCategoryName(newCategory.toUpperCase());
			c.save();
			return ok("Categoría actualizada correctamente");
		}		
		
	}
	
	/**
	 * Método que permite eliminar una categoría de recetas de la base de datos. Corresponde con un DELETE.
	 * @param name Nombre de la categoría de recetas que se desea borrar
	 * @return Respuesta indicativa del estado de la operación 
	 */
	public Result deleteCategory(String id) {
		//TODO solo el admin puede borrar una categoria. 
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound("La categoría introducida no existe");
		}
		else {
			if(c.delete()) {
				return ok("Categoría eliminada correctamente");
			}
			else {
				return internalServerError();
			}
		}
		
	}
	
	/**
	 * Método que permite visualizar todas las categorías de recetas existentes. Corresponde con un GET.
	 * @param page Página que se va a mostrar
	 * @return Respuesta que muestra las categorías de recetas existentes o error 
	 */
	public Result retrieveCategoryCollection(Integer page) {
		
		PagedList<Category> list = Category.findPage(page);
		List<Category> categories = list.getList();
		Integer number = list.getTotalCount();

		sortAlphabetically(categories);
		if(request().accepts("application/json")) {
			return ok(Json.toJson(categories)).withHeader("X-Count", number.toString());
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.categories.render(categories));
		}
		else {
			return Results.status(415);//tipo de medio no soportado
		}
		
	}
	
	/**
	 * Método que permite visualizar todas las recetas pertenecientes a una categoría. Corresponde con un GET.
	 * @param name Nombre de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(String id) {
		
		Category c = Category.findByCategoryId(id);
		if(c == null) {
			return Results.notFound("La categoría introducida no existe");
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.toJson(c.relatedRecipes));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml.recipes.render(c.relatedRecipes));
			}
			else {
				return Results.status(415);
			}
		}
		
	}
	
	/**
	 * Método que ordena alfabéticamente las categorías de recetas
	 * @param recipes Lista con las categorías de recetas
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
