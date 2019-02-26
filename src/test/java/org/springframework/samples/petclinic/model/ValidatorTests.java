package org.springframework.samples.petclinic.model;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michael Isvy Simple test to make sure that Bean Validation is working (useful
 * when upgrading to a new version of Hibernate Validator/ Bean Validation)
 */
public class ValidatorTests {

    private Validator createValidator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        return localValidatorFactoryBean;
    }

    @Test
    public void shouldNotValidateWhenFirstOrLastNameEmpty() {

        LocaleContextHolder.setLocale(Locale.ENGLISH);

        Person person = new Person("", "smith");

        Validator validator = createValidator();
        Set<ConstraintViolation<Person>> constraintViolations = validator
                .validate(person);

        assertThat(constraintViolations.size()).isEqualTo(2);
        Iterator<ConstraintViolation<Person> > validations =  constraintViolations.iterator();

        ConstraintViolation<Person> violation = validations.next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("lastName");
        assertThat(violation.getMessage()).isEqualTo("must not be empty");

        ConstraintViolation<Person> violation2 = validations.next();
        assertThat(violation2.getPropertyPath().toString()).isEqualTo("firstName");
        assertThat(violation2.getMessage()).isEqualTo("must not be empty");
    }

}
