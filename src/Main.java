
public class Main {

    public static void main(String[] args) {
        Object[] persons = {
                new Professor("Prof", "X", 40, "Computer Science"),
                new Student("Jane", "Doe", 25),
                new Student("John", "Smith", 30),
                new Professor("Prof", "Y", 45, "Physics")
        };
        XMLSerializer.serialize(persons, "output.xml");
    }
}
