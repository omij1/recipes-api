package validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.FIELD})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FirstCapitalLetterValidator.class)
public @interface FirstCapitalLetter {

    String message() default "invalid_string";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}