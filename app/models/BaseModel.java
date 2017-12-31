package models;

import java.sql.Timestamp;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;

/**
 * Modelo base utilizado en la base de datos
 * @author MIMO
 *
 */

@MappedSuperclass
public class BaseModel extends Model{
	
	/**
	 * Identificador único del elemento
	 */
	@Id
	public Long id;
	
	/**
	 * Versión del elemento
	 */
	@Version
	@JsonIgnore
	Long version;
	
	/**
	 * Fecha de creación del elemento
	 */
	@CreatedTimestamp
	@JsonIgnore
	Timestamp created;
	
	/**
	 * Fecha de la última actualización del elemento
	 */
	@UpdatedTimestamp
	@JsonIgnore
	Timestamp updated;

	/**
	 * Getter de id
	 * @return Identificador único del elemento
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Setter de id
	 * @param id Identificador del elemento
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Getter de version
	 * @return La versión actual del elemento
	 */
	public Long getVersion() {
		return version;
	}

	/**
	 * Setter de version
	 * @param version La versión del elemento
	 */ 
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * Getter de created
	 * @return Fecha de creación del elemento
	 */
	public Timestamp getCreated() {
		return created;
	}

	/**
	 * Setter de created
	 * @param created Fecha de creación del elemento
	 */
	public void setCreated(Timestamp created) {
		this.created = created;
	}

	/**
	 * Getter de updated
	 * @return Fecha de la última actualización del elemento
	 */
	public Timestamp getUpdated() {
		return updated;
	}

	/**
	 * Setter de updated
	 * @param updated Fecha de la última actualización del elemento
	 */
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}
	
}
