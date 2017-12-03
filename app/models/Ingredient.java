package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Model;
import play.data.validation.Constraints.Required;

/**
 * Clase modelo que representa la tabla Ingredient donde se guardan los ingredientes de las recetas.
 * @author MIMO
 *
 */

@Entity
public class Ingredient extends Model {

	/**
	 * Id del ingrediente
	 */
	@Id
	Long id_ingrediente;
	
	/**
	 * Nombre del ingrediente
	 */
	@Required
	String nombre_ing;
	
	/**
	 * Unidades del ingrediente.
	 */
	@Required
	String unidades;
}
