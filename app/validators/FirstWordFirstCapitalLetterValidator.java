package validators;

import play.data.validation.Constraints;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Http;

import javax.validation.ConstraintValidator;

/**
 * Clase validadora que comprueba que la palabra introducida tiene la primera letra mayúscula y el resto minúsculas.
 * Sólo comprueba la primera palabra
 */
public class FirstWordFirstCapitalLetterValidator
        extends Constraints.Validator<String>
        implements ConstraintValidator<FirstWordFirstCapitalLetter, String> {


    @Override
    public boolean isValid(String string) {
        if (string == null || string == "") { //Si se pasa una cadena vacía, devuelve true para que el validador @NotBlanck de el error correspondiente
            return true;
        }
        if (Character.isUpperCase(string.charAt(0))) {
            for (int i = 1; i < string.length(); i++) {
                if (Character.isUpperCase(string.charAt(i)) && Character.isLetter(string.charAt(i - 1))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public F.Tuple<String, Object[]> getErrorMessageKey() {
        Messages messages = Http.Context.current().messages();
        return new F.Tuple<String, Object[]>(messages.at("validation.capitalLetter"),
                new Object[]{""});
    }

    @Override
    public void initialize(FirstWordFirstCapitalLetter constraintAnnotation) {

    }

}
