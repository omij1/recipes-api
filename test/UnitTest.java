
import models.User;
import validators.FirstCapitalLetterValidator;

import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;


/**
 * Clase que contiene los test unitarios del API de recetas de cocina
 */
public class UnitTest {

    //Test para comprobar que se genera correctamente el apiKey con una longitud de 30 caracteres
    @Test
    public void testApiKeyLength() {
    	
        User user = new User("nick", "name", "surname", "city");
        user.generateApiKey();
        assertThat(user.getApiKey().getKey().length()).isEqualTo(30);
    }
    
    @Test
    public void testFirstCapitalLetterValidator() {
    	
    		String cadena = "Hello";
    		FirstCapitalLetterValidator fclv = new FirstCapitalLetterValidator();
    		assertThat(fclv.isValid(cadena)).isEqualTo(true);
    }

}
