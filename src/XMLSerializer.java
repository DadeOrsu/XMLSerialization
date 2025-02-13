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
                        Field field = fieldInfo.getField();
                        XMLfield xmlFieldAnnotation = fieldInfo.getAnnotation();

                        try {
                            field.setAccessible(true);
                            Object value = field.get(obj);

                            // obtain the new tagname from the class info
                            String tagName = fieldInfo.getXMLTagName();

                            writer.write("  <" + tagName + " type=\"" + xmlFieldAnnotation.type() + "\">");
                            writer.write(value.toString());
                            writer.write("</" + tagName + ">\n");

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } finally {
                            field.setAccessible(false);
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

    private static class ClassInfo {
        private final boolean isXMLable;
        private final String className;
        private final Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

        public ClassInfo(Class<?> clazz) {
            this.isXMLable = clazz.isAnnotationPresent(XMLable.class);
            this.className = clazz.getSimpleName();

            if (isXMLable) {
                // get all the fields of the class
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
         * Class to maintain the information of an annotated field.
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
