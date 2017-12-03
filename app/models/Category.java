package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints.Required;

/**
 * Clase modelo que representa la tabla Category de la base de datos en la que se almacena información de las categorías de recetas.
 * @author MIMO
 *
 */

@Entity
public class Category extends Model{

	/**
	 * Identificador de la categoría de receta
	 */
	@Id
	Long id_categoria;
	
	/**
	 * Nombre de la categoría de receta
	 */
	@Required
	String nombre_categoria;
	
	/**
	 * Permite hacer búsquedas en las categorías de recetas
	 */
	public static final Finder<Long, Category> find = new Finder<>(Category.class);
	
	/**
	 * Constructor de la clase Category
	 * @param id_categoria Identificador de la categoría de recetas
	 * @param nombre_categoria Nombre de la categoría de recetas
	 */
	public Category(Long id_categoria, @Required String nombre_categoria) {
		super();
		this.id_categoria = id_categoria;
		this.nombre_categoria = nombre_categoria;
	}

	/**
	 * Getter de id_categoría
	 * @return Devuelve el id de la categoría de receta
	 */
	public Long getId_categoria() {
		return id_categoria;
	}

	/**
	 * Setter de id_categoria
	 * @param id_categoria El identificador de la categoría de receta
	 */
	public void setId_categoria(Long id_categoria) {
		this.id_categoria = id_categoria;
	}

	/**
	 * Getter de nombre_categoria
	 * @return Devuelve el nombre de la categoría de receta
	 */
	public String getNombre_categoria() {
		return nombre_categoria;
	}

	/**
	 * Setter de nombre_categoria
	 * @param nombre_categoria El nombre de la categoría de receta
	 */
	public void setNombre_categoria(String nombre_categoria) {
		this.nombre_categoria = nombre_categoria;
	}
}
