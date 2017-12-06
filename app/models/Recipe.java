package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private Category category;

	
	
	/**
	 * Constructor de la clase Recipe
	 * @param nombre Nombre de la receta
	 * @param pasos Pasos para elaborar la receta
	 * @param tiempo Unidad de tiempo para elaborar la receta
	 * @param dificultad Dificultad de la receta
	 * @param category Categoría de la receta
	 */
	public Recipe(@Required String nombre, @Required String pasos, @Required String tiempo, Dificultad dificultad,
			Category category) {
		super();
		this.nombre = nombre;
		this.pasos = pasos;
		this.tiempo = tiempo;
		this.dificultad = dificultad;
		this.category = category;
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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}
