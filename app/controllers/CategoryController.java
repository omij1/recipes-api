package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Clase controladora de los métodos de acción del recurso category. 
 * @author MIMO
 *
 */

public class CategoryController extends Controller{
	
	/**
	 * Método que permite crear una nueva categoría de recetas. Corresponde con un PUT.
	 * @return Respuesta que indica si la categoría se creó correctamente o si hubo algún problema
	 */
	public Result createCategory() {
		//TODO solo puede crear una categoria el admin
		//Hay que comprobar que se ha introducido la categoria que se desea crear y que no este repetida
		return Results.ok();
	}
	
	/**
	 * Método que permite visualizar todas las recetas pertenecientes a una categoría. Corresponde con un GET.
	 * @param name Nombre de la categoría de recetas que se quiere visualizar
	 * @return Devuelve las recetas pertenecientes a la categoría especificada o error
	 */
	public Result retrieveRecipesByCategory(String name) {
		
		//Comprobar si la categoria existe
		return ok();
	}
	
	/**
	 * Método que permite actualizar una categoría de recetas. Corresponde con un PUT.
	 * @param name Nombre de la categoría de recetas que se desea actualizar
	 * @return Respuesta indicativa del éxito o fracaso de la operación 
	 */
	public Result updateCategory(String name) {
		//TODO solo el admin puede hacerlo
		return ok();
	}
	
	/**
	 * Método que permite eliminar una categoría de recetas de la base de datos. Corresponde con un DELETE.
	 * @param name Nombre de la categoría de recetas que se desea borrar
	 * @return Respuesta indicativa del estado de la operación 
	 */
	public Result deleteCategory(String name) {
		//TODO solo el admin puede borrar una categoria
		return ok();
	}
	
	/**
	 * Método que permite visualizar todas las categorías de recetas existentes. Corresponde con un GET.
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
