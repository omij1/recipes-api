package models;

import io.ebean.annotation.EnumValue;

/**
 * Enumeración que representa los niveles de dificultad de una receta
 * @author MIMO
 *
 */
public enum Difficulty {
	@EnumValue("Muy fácil")
	MUY_FACIL,
	
	@EnumValue("Fácil")
	FACIL,
	
	@EnumValue("Intermedia")
	INTERMEDIA, 
	
	@EnumValue("Difícil")
	DIFICIL,
	
	@EnumValue("Muy difícil")
	MUY_DIFICIL
}
