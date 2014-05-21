package moc.type;

public class FIELD {
    private String name;
    private TTYPE type;

    public FIELD(String name, TTYPE type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TTYPE getType() {
        return type;
    }

    public String toString() {
        return type + " " + name;
    }
}
