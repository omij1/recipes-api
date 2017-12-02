package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

public class CategoryController extends Controller{
	
	/**
	 * 
	 * @return Respuesta que indica si la categoría se creó correctamente o si hubo algún problema
	 */
	public Result createCategory() {
		//TODO solo puede crear una categoria el admin
		//Hay que comprobar que se ha introducido la categoria que se desea crear y que no este repetida
		return Results.ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(String name) {
		
		//Comprobar si la categoria existe
		return ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(String name) {
		//TODO solo el admin puede hacerlo
		return ok();
	}
	
	/**
	 * 
	 * @param name Nombre de la categoría de recetas que se desea borrar
	 * @return Respuesta indicativa del estado de la operación 
	 */
	public Result deleteCategory(String name) {
		//TODO solo el admin puede borrar una categoria
		return ok();
	}
	
	/**
	 * 
	 * @param page Página que se va a mostrar
	 * @return Respuesta que muestra las categorías de recetas existentes o error 
	 */
	public Result retrieveCategoryCollection(Integer page) {
		
		//Comprobaciones de salida
		if(request().accepts("application/xml")) {
			return ok();
		}
		else if(request().accepts("application/json")) {
			return ok();
		}
		else {
			return Results.status(415);//tipo de medio no soportado
		}
		
	}
}
