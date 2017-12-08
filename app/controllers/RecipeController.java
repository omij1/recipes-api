package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.PagedList;
import models.Category;
import models.Recipe;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Clase controladora de los métodos de acción del recurso recipe.
 * @author MIMO
 *
 */

public class RecipeController extends Controller{
	
	/**
	 * Variable formulario
	 */
	@Inject
	FormFactory formFactory;

	/**
	 * Método que permite crear una nueva receta. Corresponde con un POST
	 * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
	 */
	public Result createRecipe() {
		// TODO Añadir ingredientes
		Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest(); 
		if(f.hasErrors()) {
			return Results.status(409, f.errorsAsJson());
		}
		
		Recipe r = f.get();
		if(r.checkCategory()) {
			if(r.checkRecipe()) {
				return Results.ok("Receta creada correctamente");
			}
			else {
				return Results.status(409, "Ya existe una receta con ese nombre");
			}
		}
		else {
			return Results.notFound("La categoría introducida no existe");
		}
		
	}
	
	/**
	 * Método que permite obtener la información de una receta. Corresponde con un GET.
	 * @param name Nombre de la receta que se desea visualizar
	 * @return Respuesta que muestra la receta o error si se produjo alguno
	 */
	public Result retrieveRecipe(String name) {
		
		//TODO Poner cache
		String formattedName = name.replace("-", " ");
		Recipe recipe = Recipe.findByName(formattedName);
		if(recipe == null) {
			return Results.notFound("No existe ninguna receta con ese nombre");
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.toJson(recipe));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml._recipe.render(recipe));
			}
			else {
				return Results.status(415);
			}
		}
		
	}
	
	/**
	 * Método que permite actualizar la información de una receta. Corresponde con un PUT.
	 * @param name Nombre de la receta que se desea actualizar
	 * @return Respuesta que indica el resultado de la operación
	 */
	public Result updateRecipe(String name) {
		
		
		return ok();
	}
	
	/**
	 * Método que permite eliminar una receta. Corresponde con un DELETE.
	 * @param name Nombre de la receta que se desea eliminar
	 * @return Respuesta que indica si la receta se borró o si se produjo un error
	 */
	public Result deleteRecipe(String name) {
		
		// TODO Comprobar que el usuario que quiere borrar la receta es el admin o el creador
		String formattedName = name.replace("-", " "); //En curl no se puede poner por ejemplo Pollo con patatas como variable en la ruta DELETE
		Recipe r = Recipe.findByName(formattedName);
		if(r == null) {
			return Results.notFound("No existe ninguna receta con ese nombre");
		}
		else {
			if(r.delete()) {
				return ok("Receta eliminada satisfactoriamente");
			}
			else {
				return internalServerError();
			}
		}
		
	}
	
	/**
	 * Método que permite visualizar las recetas existentes sin tener en cuenta su categoría. Corresponde con un GET.
	 * @param page Página que se va a mostrar
	 * @return Respuesta que muestra todas las recetas existentes
	 */
	public Result retrieveRecipeCollection(Integer page) {

		PagedList<Recipe> list = Recipe.findPage(page);
		List<Recipe> recipes = list.getList();
		Integer number = list.getTotalCount();
		
		sortAlphabetically(recipes);
		if(request().accepts("application/json")) {
			return ok(Json.toJson(recipes)).withHeader("X-Count", number.toString());
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.recipes.render(recipes));
		}
		else {
			return Results.status(415);//tipo de medio no soportado
		}
		
	}
	
	/**
	 * Método que ordena alfabéticamente las recetas
	 * @param recipes Lista con las recetas
	 */
	private void sortAlphabetically(List<Recipe> recipes) {
		
		if(recipes.size() > 0) {
			Collections.sort(recipes, new Comparator<Recipe>() {

				@Override
				public int compare(Recipe o1, Recipe o2) {
					return o1.getNombre().compareTo(o2.getNombre());
				}
			});
		}
	}
	
}
