package validators;

import play.data.validation.Constraints;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Http;

import javax.validation.ConstraintValidator;

public class FirstCapitalLetterValidator
        extends Constraints.Validator<String>
        implements ConstraintValidator<FirstCapitalLetter, String> {

    Messages messages = Http.Context.current().messages();

    @Override
    public boolean isValid(String string) {
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
        return new F.Tuple<String, Object[]>(messages.at("validation.capitalLetter"),
                new Object[]{""});
    }

    @Override
    public void initialize(FirstCapitalLetter constraintAnnotation) {

    }
}
