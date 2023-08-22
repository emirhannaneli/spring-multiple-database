package dev.emirman.util.spring.multiple.database.annotation.jpa;

import dev.emirman.util.spring.multiple.database.MultiplePostgresFactory;
import dev.emirman.util.spring.multiple.database.annotation.jpa.event.EnableMultipleJPAListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({MultiplePostgresFactory.class})
public @interface EnableMultipleJPA {
}
