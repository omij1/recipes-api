package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;
import play.data.validation.Constraints.Required;

/**
 * Clase modelo que representa la tabla Category de la base de datos en la que se almacena información de las categorías de recetas.
 * @author MIMO
 *
 */

@Entity
public class Category extends Model{
	
	/**
	 * Permite hacer búsquedas en las categorías de recetas
	 */
	public static final Finder<Long, Category> find = new Finder<>(Category.class);

	/**
	 * Identificador de la categoría de receta
	 */
	@Id
	@JsonIgnore
	Long id_categoria;
	
	/**
	 * Nombre de la categoría de receta
	 */
	@Required
	String nombre_categoria;
	
	/**
	 * Lista de recetas pertenecientes a una categoría
	 */
	@OneToMany(cascade=CascadeType.ALL,mappedBy="categoria")
	public List<Recipe> relatedRecipes = new ArrayList<Recipe>();
	
	

	/**
	 * Constructor de la clase Category
	 * @param id_categoria Identificador de la categoría de recetas
	 * @param nombre_categoria Nombre de la categoría de recetas
	 */
	public Category(@Required String nombre_categoria) {
		
		super();
		this.nombre_categoria = nombre_categoria;
	}
	
	/**
	 * 
	 * @param nombreCategoria
	 * @return Un objeto con los datos de la categoría
	 */
	public static Category findByCategoryName(String categoryName) {
		
		return find.query().where().isNotNull("nombre_categoria").eq("nombre_categoria", categoryName).findOne();
	}
	
	public static PagedList<Category> findPage(Integer page){
		
		return find.query().setMaxRows(10).setFirstRow(10*page).findPagedList();
	}
	
	/**
	 * Método que comprueba si una categoría está repetida
	 * @return Verdadero si está repetida y falso en caso contrario
	 */
	public boolean checkCategory() {
		
		if(Category.findByCategoryName(this.nombre_categoria) == null) {
			
			Ebean.beginTransaction();
			this.save();
			Ebean.commitTransaction();
			Ebean.endTransaction();
			return false;
			
		}
		return true;
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
