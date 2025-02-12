import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to indicate that a field should be serialized in XML.
 * Fields marked with this annotation will be included in the XML output.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLfield {
    /**
     * The Java type of the field.
     */
    String type();
    /**
     * The name of the field in the XML output.
     */
    String name() default "";
}