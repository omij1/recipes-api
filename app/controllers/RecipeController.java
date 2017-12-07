package controllers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import models.Recipe;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Clase controladora de los métodos de acción del recurso recipe.
 * @author MIMO
 *
 */

public class RecipeController extends Controller{
	
	@Inject
	FormFactory formFactory;

	/**
	 * Método que permite crear una nueva receta. Corresponde con un POST
	 * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
	 */
	public Result createRecipe() {
		
		//Comprobar si la receta ya existe
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
		
		
		return ok();
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
		
		//Comprobar que el usuario que quiere borrar la receta es el admin o el creador
		return ok();
	}
	
	/**
	 * Método que permite visualizar las recetas existentes. Corresponde con un GET.
	 * @param page Página que se va a mostrar
	 * @return Respuesta que muestra todas las recetas existentes
	 */
	public Result retrieveRecipeCollection(Integer page) {
		return ok();
	}
	
}
