package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Clase controladora de los métodos de acción del recurso recipe.
 * @author MIMO
 *
 */

public class RecipeController extends Controller{

	/**
	 * Método que permite crear una nueva receta. Corresponde con un POST
	 * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
	 */
	public Result createRecipe() {
		
		//Comprobar si la receta ya existe
		return ok();
	}
	
	/**
	 * Método que permite obtener la información de una receta. Corresponde con un GET.
	 * @param name Nombre de la receta que se desea visualizar
	 * @return Respuesta que muestra la receta o error si se produjo alguno
	 */
	public Result retrieveRecipe(String name) {
		
		//Comprobar si la receta existe
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
