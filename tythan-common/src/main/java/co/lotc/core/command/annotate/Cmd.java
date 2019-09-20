package co.lotc.core.command.annotate;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Cmd {
	String value();
	String permission() default "";
	String alias() default "";
	String[] aliases() default {};
	boolean flags() default true;
}
