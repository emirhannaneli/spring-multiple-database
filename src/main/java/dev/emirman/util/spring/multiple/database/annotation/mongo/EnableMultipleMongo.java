package dev.emirman.util.spring.multiple.database.annotation.mongo;

import dev.emirman.util.spring.multiple.database.MultipleMongoFactory;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MultipleMongoFactory.class)
public @interface EnableMultipleMongo {
}
