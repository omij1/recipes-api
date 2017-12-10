package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;
import play.data.validation.Constraints.Required;

/**
 * Clase modelo que representa la tabla Recipe donde se almacenan las recetas del API.
 * @author MIMO
 *
 */

@Entity
public class Recipe extends Model{
	//TODO agregar ingredientes
	/**
	 * Permite hacer búsquedas de recetas
	 */
	public static final Finder<Long, Recipe> find = new Finder<>(Recipe.class);

	/**
	 * Identificador de la receta
	 */
	@Id
	Long recipeId;
	
	/**
	 * Nombre de la receta
	 */
	@Required
	String title;
	
	/**
	 * Pasos para elaborar la receta
	 */
	@Required
	String steps;
	
	/**
	 * Unidad de tiempo necesario para elaborar la receta. Pueden ser minutos u horas
	 */
	@Required
	String time;
	
	/**
	 * Valor de dificultad de la receta
	 */
	@Enumerated(EnumType.STRING)
	@Required
	public Difficulty difficulty;
	
	/**
	 * Categoría de la receta
	 */
	@JsonManagedReference
	@ManyToOne
	public Category category;

	
	
	/**
	 * Constructor de la clase Recipe
	 * @param title Nombre de la receta
	 * @param steps Pasos para elaborar la receta
	 * @param time Unidad de tiempo para elaborar la receta
	 * @param difficulty Dificultad de la receta
	 * @param category Categoría de la receta
	 */
	public Recipe(@Required String title, @Required String steps, @Required String time, Difficulty difficulty,
			Category category) {
		
		super();
		this.title = title;
		this.steps = steps;
		this.time = time;
		this.difficulty = difficulty;
		this.category = category;
	}
	
	/**
	 * Método que busca una receta basándose en su identificcador
	 * @param id Identificador de la receta
	 * @return Un objeto con la receta
	 */
	public static Recipe findById(String id) {
		
		return find.query().where().isNotNull("recipeId").eq("recipeId", id).findOne();
	}
	
	/**
	 * Método que comprueba si una receta ya existe
	 * @param title Nombre de la receta
	 * @return Un objeto con la receta
	 */
	public static Recipe findByName(String title) {
		
		return find.query().where().isNotNull("title").eq("title", title).findOne();
	}
	
	/**
	 * Método que muestra las recetas existentes de forma paginada
	 * @param page Número de página que se desea ver
	 * @return Devuelve una lista con las recetas
	 */
	public static PagedList<Recipe> findPage(Integer page){
		
		return find.query().setMaxRows(10).setFirstRow(10*page).findPagedList();
	}
	
	/**
	 * Método que comprueba si la categoría de la receta introducida existe
	 * @return Devuelve true si la categoría existe y false en caso contrario
	 */
	public boolean checkCategory() {

		Category c = Category.findByCategoryId(this.category.getCategoryId().toString());
		if(c != null) {
			this.category = c;
			return true;
		}
		return false;
	}
	
	/**
	 * Método que comprueba si una receta ya existe y la crea en caso de que sea posible
	 * @return Devuelve false si la receta ya existe y true si se creó correctamente
	 */
	public boolean checkRecipe() {
		
		if(Recipe.findByName(this.title.toUpperCase()) == null) {
			this.title = this.title.toUpperCase();
			Ebean.beginTransaction();
			this.save();
			Ebean.commitTransaction();
			Ebean.endTransaction();
			return true;
		}
		return false;
	}

	/**
	 * Getter de recipeId
	 * @return Identificador de la receta
	 */
	public Long getRecipeId() {
		return recipeId;
	}

	/**
	 * Setter de recipeId
	 * @param recipeId El identificador de la receta
	 */
	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	/**
	 * Getter de title
	 * @return El título de la receta
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter de title
	 * @param title Título de la receta
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter de steps
	 * @return Los pasos para elaborar la receta
	 */
	public String getSteps() {
		return steps;
	}

	/**
	 * Setter de steps
	 * @param steps Pasos para elaborar la receta
	 */
	public void setSteps(String steps) {
		this.steps = steps;
	}

	/**
	 * Getter de time
	 * @return El tiempo necesario para elaborar una receta
	 */
	public String getTime() {
		return time;
	}

	/**
	 * Setter de time
	 * @param time El tiempo necesario para elaborar una receta
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Getter de difficulty
	 * @return Valor de dificultad para elaborar la receta
	 */
	public Difficulty getDifficulty() {
		return difficulty;
	}

	/**
	 * Setter de difficulty
	 * @param difficulty Dificultad de la receta
	 */
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Getter de category
	 * @return Categoría de la receta
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * Setter de category
	 * @param category Categoría de la receta
	 */
	public void setCategory(Category category) {
		this.category = category;
	}
	
}
