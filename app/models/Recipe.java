package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
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
	@JsonIgnore
	Long id_receta;
	
	/**
	 * Nombre de la receta
	 */
	@Required
	String nombre;
	
	/**
	 * Pasos para elaborar la receta
	 */
	@Required
	String pasos;
	
	/**
	 * Unidad de tiempo necesario para elaborar la receta. Pueden ser minutos u horas
	 */
	@Required
	String tiempo;
	
	/**
	 * Valor de dificultad de la receta
	 */
	@Enumerated(EnumType.STRING)
	@Required
	public Dificultad dificultad;
	
	/**
	 * Categoría de la receta
	 */
	@ManyToOne
	public Category categoria;

	
	
	/**
	 * Constructor de la clase Recipe
	 * @param nombre Nombre de la receta
	 * @param pasos Pasos para elaborar la receta
	 * @param tiempo Unidad de tiempo para elaborar la receta
	 * @param dificultad Dificultad de la receta
	 * @param category Categoría de la receta
	 */
	public Recipe(@Required String nombre, @Required String pasos, @Required String tiempo, Dificultad dificultad,
			Category categoria) {
		
		super();
		this.nombre = nombre;
		this.pasos = pasos;
		this.tiempo = tiempo;
		this.dificultad = dificultad;
		this.categoria = categoria;
	}
	
	/**
	 * Método que comprueba si una receta ya existe
	 * @param name Nombre de la receta
	 * @return Un objeto con la receta
	 */
	public static Recipe findByName(String name) {
		
		return find.query().where().isNotNull("nombre").eq("nombre", name).findOne();
	}
	
	/**
	 * Método que comprueba si la categoría de la receta introducida existe
	 * @return Devuelve true si la categoría existe y false en caso contrario
	 */
	public boolean checkCategory() {
		
		Category c = Category.findByCategoryName(this.categoria.getNombre_categoria());
		if(c != null) {
			this.categoria = c;
			return true;
		}
		return false;
	}
	
	/**
	 * Método que comprueba si una receta ya existe y la crea en caso de que sea posible
	 * @return Devuelve false si la receta ya existe y true si se creó correctamente
	 */
	public boolean checkRecipe() {
		
		if(Recipe.findByName(this.nombre) == null) {
			Ebean.beginTransaction();
			this.save();
			Ebean.commitTransaction();
			Ebean.endTransaction();
			return true;
		}
		return false;
	}

	/**
	 * Método que permite obtener el id de una receta
	 * @return Id de la receta
	 */
	public Long getId_receta() {
		return id_receta;
	}

	/**
	 * 
	 * @param id_receta
	 */
	public void setId_receta(Long id_receta) {
		this.id_receta = id_receta;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPasos() {
		return pasos;
	}

	public void setPasos(String pasos) {
		this.pasos = pasos;
	}

	public String getTiempo() {
		return tiempo;
	}

	public void setTiempo(String tiempo) {
		this.tiempo = tiempo;
	}

	public Dificultad getDificultad() {
		return dificultad;
	}

	public void setDificultad(Dificultad dificultad) {
		this.dificultad = dificultad;
	}

	public Category getCategoria() {
		return categoria;
	}

	public void setCategoria(Category categoria) {
		this.categoria = categoria;
	}
	
}
