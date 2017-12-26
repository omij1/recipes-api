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

		String apiKey = request().getQueryString("apiKey");
		Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest(); 
		if(f.hasErrors()) {
			return Results.status(409, f.errorsAsJson());
		}
		
		Recipe r = f.get();
		
		//Usuario que sube la receta
		User u = User.findByApiKey(apiKey);
		
		if(u == null) {
			return Results.notFound("El usuario introducido no existe");
		}
		
		//Asignamos el creador de la receta
		r.setUser(u);
		if(r.checkCategory()) {
			if(r.checkRecipe()) {
				return Results.ok("Receta creada correctamente");
			}
			else {
				return Results.status(409, new ErrorObject("2","Ya existe una receta con ese nombre").convertToJson()).as("application/json");
			}
		}
		else {
			return Results.notFound("La categoría introducida no existe");
		}
		
	}
	
	/**
	 * Método que permite obtener la información de una receta. Corresponde con un GET.
	 * @param id Id de la receta que se desea visualizar
	 * @return Respuesta que muestra la receta o error si se produjo alguno
	 */
	public Result retrieveRecipe(Long id) {
		
		//TODO Poner cache
		Recipe recipe = Recipe.findById(id);
		if(recipe == null) {
			return Results.notFound("No existe ninguna receta con ese identificador");
		}
		else {
			if(request().accepts("application/json")) {
				return ok(Json.prettyPrint(Json.toJson(recipe)));
			}
			else if(request().accepts("application/xml")) {
				return ok(views.xml._recipe.render(recipe));
			}
			
			return Results.status(415);
		}
		
	}
	
	/**
	 * Método que permite actualizar la información de una receta. Corresponde con un PUT.
	 * @param id Id de la receta que se desea actualizar
	 * @return Respuesta que indica el resultado de la operación
	 */
	public Result updateRecipe(Long id) {
		//TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
		//TODO Solo puede actualizar una receta el admin o el creador

		String apiKey = request().getQueryString("apiKey");
		if(!request().hasBody()) {
			return Results.badRequest("Parámetros obligatorios");
		}
		
		Recipe r = Recipe.findById(id);
		if(r == null) {
			return Results.notFound("No existe ninguna receta con ese identificador");
		}
		else {
			Form<Recipe> f = formFactory.form(Recipe.class).bindFromRequest();
			if(f.hasErrors()) {
				return Results.ok(f.errorsAsJson());
			}
			if(updateFields(r,f)) {
				return ok("Receta actualizada correctamente");
			}
			else {
				return Results.notFound("La categoría introducida no existe");
			}
		}
		
	}
	
	/**
	 * Método que actualiza la información de una receta en la base de datos. Se ha utilizado un form para soportar posibles
	 * ampliaciones o modificaciones del modelo.
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
		if(!r.checkCategory()) {
			return false;
		}
		r.save();
		return true;
	}

	/**
	 * Método que permite eliminar una receta. Corresponde con un DELETE.
	 * @param id Id de la receta que se desea eliminar
	 * @return Respuesta que indica si la receta se borró o si se produjo un error
	 */
	public Result deleteRecipe(Long id) {
		//TODO Comprobar si el apiKey existe ejemplo en metodo de accion createUser
		// TODO Comprobar que el usuario que quiere borrar la receta es el admin o el creador
		String apiKey = request().getQueryString("apiKey");
		Recipe r = Recipe.findById(id);
		if(r == null) {
			return Results.notFound("No existe ninguna receta con ese identificador");
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
	 * @return Respuesta que muestra todas las recetas existentes
	 */
	public Result retrieveRecipeCollection() {

		Integer page = Integer.parseInt(request().getQueryString("page"));
		PagedList<Recipe> list = Recipe.findPage(page);
		List<Recipe> recipes = list.getList();
		Integer number = list.getTotalCount();
		
		sortAlphabetically(recipes);
		if(request().accepts("application/json")) {
			return ok(Json.prettyPrint(Json.toJson(recipes))).withHeader("X-Count", number.toString());
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml.recipes.render(recipes)).withHeader("X-Count", number.toString());
		}
		
		return Results.status(415);//tipo de medio no soportado
		
	}
	
	/**
	 * Método que permite buscar una receta por su título
	 * @return Respuesta que muestra la receta o error
	 */
	public Result searchRecipe() {

		String title = request().getQueryString("title");
		Recipe recipe = Recipe.findByName(title.toUpperCase());
		if(recipe == null){
			return Results.notFound("No existe ninguna receta con ese nombre");
		}
		
		if(request().accepts("application/json")) {
			return ok(Json.toJson(recipe));
		}
		else if(request().accepts("application/xml")) {
			return ok(views.xml._recipe.render(recipe));
		}
		
		return Results.status(415);//tipo de medio no soportado
		
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
					return o1.getTitle().compareTo(o2.getTitle());
				}
			});
		}
	}
	
}
