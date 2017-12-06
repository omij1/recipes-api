package models;

import io.ebean.annotation.EnumValue;

public enum Dificultad {
	@EnumValue("Muy fácil")
	MUY_FACIL,
	
	@EnumValue("Fácil")
	FACIL,
	
	@EnumValue("Normal")
	NORMAL, 
	
	@EnumValue("Difícil")
	DIFICIL,
	
	@EnumValue("Muy difícil")
	MUY_DIFICIL
}
