import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMLSerializer {

    public static void serialize(Object[] arr, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Add the XML declaration
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            // map to store the class information
            Map<Class<?>, ClassInfo> classInfoMap = new HashMap<>();

            for (Object obj : arr) {
                // verify if the class is already in the map
                Class<?> objClass = obj.getClass();
                ClassInfo classInfo = classInfoMap.computeIfAbsent(objClass, c -> new ClassInfo(c));

                if (classInfo.isXMLable()) {
                    // write the opening tag of the class
                    writer.write("<" + classInfo.getClassName() + ">\n");

                    for (ClassInfo.FieldInfo fieldInfo : classInfo.getFieldInfoList()) {
                        Field field = fieldInfo.getField();
                        Annotation annotation = fieldInfo.getAnnotation();

                        if (annotation instanceof XMLfield) {
                            XMLfield xmlFieldAnnotation = (XMLfield) annotation;

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

    // auxiliary class to store the class information
    private static class ClassInfo {
        private boolean isXMLable;
        private String className;
        private Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

        public ClassInfo(Class<?> clazz) {
            this.isXMLable = clazz.isAnnotationPresent(XMLable.class);
            this.className = clazz.getSimpleName();

            if (isXMLable) {
                // Memorizza le informazioni sui campi
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        fieldInfoMap.put(field.getName(), new FieldInfo(field, annotation));
                    }
                }
            }
        }

        public boolean isXMLable() {
            return isXMLable;
        }

        public String getClassName() {
            return className;
        }

        public Iterable<FieldInfo> getFieldInfoList() {
            return fieldInfoMap.values();
        }

        // auxiliary class to store the field information
        public static class FieldInfo {
            private final Field field;
            private final Annotation annotation;

            public FieldInfo(Field field, Annotation annotation) {
                this.field = field;
                this.annotation = annotation;
            }

            public Field getField() {
                return field;
            }

            public Annotation getAnnotation() {
                return annotation;
            }
        }
    }
}
