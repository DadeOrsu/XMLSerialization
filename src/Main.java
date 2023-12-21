/**
 * Main class for the XMLSerializer example.
 */
public class Main {
    public static void main(String[] args) {
        Object[] persons = {
                new Librarian("John", "Doe", 35, "Library Science"),
                new Professor("Prof", "X", 40, "Computer Science"),
                new Student("Jane", "Doe", 25),
                new Student("John", "Smith", 30),
                new Professor("Prof", "Y", 45, "Physics"),
                new Librarian("Jane", "Smith", 35, "Library Science")
        };
        XMLSerializer.serialize(persons, "output.xml");
    }
}
