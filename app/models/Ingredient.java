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
	Long ingredientId;

	/**
	 * Nombre del ingrediente
	 */
	@Required
	String ingredientName;

	/**
	 * Unidades del ingrediente.
	 */
	@Required
	String units;
	

	/**
	 * Getter de ingredientId
	 * @return Devuelve el identificador del ingrediente
	 */
	public Long getIngredientId() {
		return ingredientId;
	}

	/**
	 * Setter de ingredientId
	 * @param ingredientId El identificador del ingrediente
	 */
	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}
	
	/**
	 * Getter de ingredientName
	 * @return Devuelve el nombre del ingrediente
	 */
	public String getIngredientName() {
		return ingredientName;
	}

	/**
	 * Setter de ingredientName
	 * @param ingredientName El nombre del ingrediente
	 */
	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}
	
	/**
	 * Getter de units
	 * @return Devuelve las unidades del ingrediente
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * Setter de units
	 * @param units Las unidades del ingrediente
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
}
