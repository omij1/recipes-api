package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class RecipeController extends Controller{

	/**
	 * 
	 * @return Indica si la receta se creó satisfactoriamente o si por el contrario hubo algún error
	 */
	public Result createRecipe() {
		
		//Comprobar si la receta ya existe
		return ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la receta que se desea visualizar
	 * @return Respuesta que muestra la receta o error si se produjo alguno
	 */
	public Result retrieveRecipe(String name) {
		
		//Comprobar si la receta existe
		return ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la receta que se desea actualizar
	 * @return Respuesta que indica el resultado de la operación
	 */
	public Result updateRecipe(String name) {
		
		
		return ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la receta que se desea eliminar
	 * @return Respuesta que indica si la receta se borró o si se produjo un error
	 */
	public Result deleteRecipe(String name) {
		
		//Comprobar que el usuario que quiere borrar la receta es el admin o el creador
		return ok();
	}
	
	/**
	 * 
	 * @param page Página que se va a mostrar
	 * @return Respuesta que muestra todas las recetas existentes
	 */
	public Result retrieveRecipeCollection(Integer page) {
		return ok();
	}
	
}
