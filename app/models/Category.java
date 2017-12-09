package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
	Long categoryId;

	/**
	 * Nombre de la categoría de receta
	 */
	@Required
	String categoryName;

	/**
	 * Lista de recetas pertenecientes a una categoría
	 */
	@JsonBackReference
	@OneToMany(cascade=CascadeType.ALL,mappedBy="category")
	public List<Recipe> relatedRecipes = new ArrayList<Recipe>();
	
	

	/**
	 * Constructor de la clase Category
	 * @param id_categoria Identificador de la categoría de recetas
	 * @param nombre_categoria Nombre de la categoría de recetas
	 */
	public Category(@Required String categoryName) {
		
		super();
		this.categoryName = categoryName;
	}
	
	/**
	 * Método que busca una categoría basándose en su identificador
	 * @param id Identificador de la categoría
	 * @return Un objeto con los datos de la categoría
	 */
	public static Category findByCategoryId(String id) {
		return find.query().where().isNotNull("categoryId").eq("categoryId", id).findOne();
	}
	
	/**
	 * Método que busca una categoría basándose en su nombre
	 * @param nombreCategoria Nombre de la categoría
	 * @return Un objeto con los datos de la categoría
	 */
	public static Category findByCategoryName(String categoryName) {
		
		return find.query().where().isNotNull("categoryName").eq("categoryName", categoryName).findOne();
	}
	
	/**
	 * Método que muestra las categorías existentes de forma paginada
	 * @param page Número de página que se desea ver
	 * @return Devuelve una lista con las categorías
	 */
	public static PagedList<Category> findPage(Integer page){
		
		return find.query().setMaxRows(10).setFirstRow(10*page).findPagedList();
	}
	
	/**
	 * Método que comprueba si una categoría está repetida
	 * @return Verdadero si está repetida y falso en caso contrario
	 */
	public boolean checkCategory() {
		
		if(Category.findByCategoryName(this.categoryName) == null) {
			
			Ebean.beginTransaction();
			this.save();
			Ebean.commitTransaction();
			Ebean.endTransaction();
			return false;
			
		}
		return true;
	}

	/**
	 * Getter de categoryId
	 * @return Devuelve el id de la categoría de receta
	 */
	public Long getCategoryId() {
		return categoryId;
	}

	/**
	 * Setter de categoryId
	 * @param categoryId El identificador de la categoría de receta
	 */
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Getter de categoryName
	 * @return Devuelve el nombre de la categoría de receta
	 */
	public String getCategoryName() {
		return categoryName;
	}

	/**
	 * Setter de categoryName
	 * @param categoryName El nombre de la categoría de receta
	 */
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
