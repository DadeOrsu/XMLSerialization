import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMLSerializer {

    /**
     * Serialize an array of objects to an XML file.
     *
     * @param arr      the array of objects to serialize
     * @param fileName the name of the file to write
     */
    public static void serialize(Object[] arr, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Add the XML declaration
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            // map to store the class information
            Map<Class<?>, ClassInfo> classInfoMap = new HashMap<>();

            for (Object obj : arr) {
                // verify if the class is already in the map
                Class<?> objClass = obj.getClass();
                ClassInfo classInfo = classInfoMap.computeIfAbsent(objClass, ClassInfo::new);

                if (classInfo.isXMLable()) {
                    // write the opening tag of the class
                    writer.write("<" + classInfo.getClassName() + ">\n");

                    for (ClassInfo.FieldInfo fieldInfo : classInfo.getFieldInfoList()) {
                        Field field = fieldInfo.field();
                        Annotation annotation = fieldInfo.annotation();

                        if (annotation instanceof XMLfield xmlFieldAnnotation) {

                            try {
                                // set the field accessible
                                field.setAccessible(true);

                                // obtain the value of the field
                                Object value = field.get(obj);

                                // write the opening tag of the field
                                writer.write("  <" + field.getName() + " type=\"" + xmlFieldAnnotation.type() + "\">");
                                writer.write(value.toString());
                                writer.write("</" + field.getName() + ">\n");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } finally {
                                // set the field not accessible
                                field.setAccessible(false);
                            }
                        }
                    }

                    // Scrivi l'etichetta di chiusura della classe
                    writer.write("</" + classInfo.getClassName() + ">\n");
                } else {
                    // Scrivi solo <notXMLable /> se la classe non ha l'annotazione XMLable
                    writer.write("<notXMLable />\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Auxiliary class to store the class information.
     */
    private static class ClassInfo {
        private final boolean isXMLable;
        private final String className;
        private final Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

        /**
         * Constructor.
         *
         * @param clazz the class to store the information
         */
        public ClassInfo(Class<?> clazz) {
            this.isXMLable = clazz.isAnnotationPresent(XMLable.class);
            this.className = clazz.getSimpleName();

            if (isXMLable) {
                // get all the fields of the class
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        fieldInfoMap.put(field.getName(), new FieldInfo(field, annotation));
                    }
                }
            }
        }

        /**
         * Check if the class is XMLable.
         * @return true if the class is XMLable, false otherwise
         */
        public boolean isXMLable() {
            return isXMLable;
        }

        /**
         * Get the name of the class.
         * @return the name of the class
         */
        public String getClassName() {
            return className;
        }

        /**
         * Get the list of the fields of the class.
         * @return the list of the fields of the class
         */
        public Iterable<FieldInfo> getFieldInfoList() {
            return fieldInfoMap.values();
        }

        /**
         * Auxiliary class to store the field information.
         *
         * @param field the field
         * @param annotation the annotation of the field
         */
            public record FieldInfo(Field field, Annotation annotation) {
        }
    }
}
