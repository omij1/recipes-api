package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.PagedList;
import models.Category;


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
		
		String categoryName = jn.get("category").asText();
		if(categoryName == null || categoryName == "") {
			return Results.badRequest("La categoría introducida no tiene el formato correcto");
		}
		
		Category categoria = new Category(categoryName.toLowerCase());
		if(!categoria.checkCategory()) {
			return Results.created("Categoría creada correctamente");
		}
		else {
			return Results.status(409, "Categoría repetida");
		}
	}
	
	/**
	 * Método que permite visualizar todas las recetas pertenecientes a una categoría. Corresponde con un GET.
	 * @param name Nombre de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(String name) {
		
		
		return ok();
	}
	
	/**
	 * Método que permite actualizar una categoría de recetas. Corresponde con un PUT.
	 * @param name Nombre de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(String name) { //https://stackoverflow.com/questions/7543391/how-to-update-an-object-in-play-framework
		//TODO solo el admin puede hacerlo
		JsonNode jn = request().body().asJson();
		if(!request().hasBody() || jn == null) {
			return Results.badRequest("Parámetros obligatorios");
		}
		
		String newCategory = jn.get("newCategory").asText();
		if(newCategory == null || newCategory == "") {
			return Results.badRequest("La nueva categoría introducida no tiene el formato correcto");
		}
		
		Category c = Category.findByCategoryName(name);
		if(c == null) {
			return Results.notFound("La categoría introducida no existe");
		}
		else {
			c.setNombre_categoria(newCategory);
			c.save();
			return ok("Categoría actualizada correctamente");
		}		
		
	}
	
	/**
	 * Método que permite eliminar una categoría de recetas de la base de datos. Corresponde con un DELETE.
	 * @param name Nombre de la categoría de recetas que se desea borrar
	 * @return Respuesta indicativa del estado de la operación 
	 */
	public Result deleteCategory(String name) {
		//TODO solo el admin puede borrar una categoria. 
		
		
		return ok();
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
}
