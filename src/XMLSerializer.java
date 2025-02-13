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
            // Add the XML declaration at the beginning of the xml file
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            // Map that stores class metadata to ensure introspection is performed only once per class.
            Map<Class<?>, ClassInfo> classInfoMap = new HashMap<>();

            for (Object obj : arr) {
                // for each object we get its class
                Class<?> objClass = obj.getClass();
                // if the class metadata are already present in the map we use them, otherwise we add them
                ClassInfo classInfo = classInfoMap.computeIfAbsent(objClass, ClassInfo::new);
                // check if the class has the XMLable annotation
                if (classInfo.isXMLable()) {
                    writer.write("<" + classInfo.getClassName() + ">\n");
                    // iterate over the XMLfields of the class
                    for (ClassInfo.FieldInfo fieldInfo : classInfo.getFieldInfoList()) {
                        Field field = fieldInfo.getField();
                        XMLfield xmlFieldAnnotation = fieldInfo.getAnnotation();
                        // set the field as accessible and restore its accessibility at the end
                        boolean wasAccessible = field.canAccess(obj);
                        field.setAccessible(true);
                        try {
                            Object value = field.get(obj);

                            // obtain the tag from the class info and write it
                            String tagName = fieldInfo.getXMLTagName();
                            writer.write("  <" + tagName + " type=\"" + xmlFieldAnnotation.type() + "\">");
                            writer.write(value.toString());
                            writer.write("</" + tagName + ">\n");

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } finally {
                            field.setAccessible(wasAccessible);
                        }
                    }

                    // Write the closing etiquette
                    writer.write("</" + classInfo.getClassName() + ">\n");
                } else {
                    // Write only <notXMLable /> if the class has not the annotation XMLable
                    writer.write("<notXMLable />\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The ClassInfo class serves as a support structure to efficiently retrieve metadata about a specific class,
     * avoiding redundant introspection. When an instance of a previously unseen class is encountered for the first time,
     * its metadata is extracted and stored, ensuring that future accesses do not require repeated reflection.
     */
    private static class ClassInfo {
        private final boolean isXMLable;
        private final String className;
        private final Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

        public ClassInfo(Class<?> clazz) {
            this.isXMLable = clazz.isAnnotationPresent(XMLable.class);
            this.className = clazz.getSimpleName();
            //checks if the @XMLable annotation is present, if so it stores other metadata
            if (isXMLable) {
                // Retrieve all declared fields of the class and store their metadata if annotated with @XMLfield.
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof XMLfield xmlFieldAnnotation) {
                            fieldInfoMap.put(field.getName(), new FieldInfo(field, xmlFieldAnnotation));
                        }
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

        /**
         * A utility class that stores metadata about a field annotated with @XMLfield.
         * It provides access to the field’s reflection object, its annotation details,
         * and utility methods for retrieving XML-related attributes.
         */
        public static class FieldInfo {
            private final Field field;
            private final XMLfield xmlFieldAnnotation;

            public FieldInfo(Field field, XMLfield xmlFieldAnnotation) {
                this.field = field;
                this.xmlFieldAnnotation = xmlFieldAnnotation;
            }

            public Field getField() {
                return field;
            }

            public XMLfield getAnnotation() {
                return xmlFieldAnnotation;
            }

            public String getXMLTagName() {
                return xmlFieldAnnotation.name().isEmpty() ? field.getName() : xmlFieldAnnotation.name();
            }
        }
    }

}
