package moc.gc;

/**
 * This class describes a registry of a machine
 */
public class Register {
    private String name;
    private int num;

    public Register(String name, int num) {
        this.name = name;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "Register [name=" + name + ", num=" + num + "]";
    }
}
