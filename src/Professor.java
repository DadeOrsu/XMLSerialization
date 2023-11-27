@XMLable
public class Professor {
    @XMLfield(type = "String")
    private String firstName;
    @XMLfield(type = "String", name = "surname")
    private String lastName;
    @XMLfield(type = "int")
    private int age;
    @XMLfield(type = "String")
    private String department;

    public Professor() {
    }

    public Professor(String firstName, String lastName, int age, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.department = department;
    }
}
