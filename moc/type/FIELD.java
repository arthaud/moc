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

    public boolean equals(Object other) {
        if (other instanceof FIELD) {
            FIELD of = (FIELD)other;
            return name.equals(of.getName()) && type.equals(of.getType());
        }
        else {
            return false;
        }
    }

    public String toString() {
        return type + " " + name;
    }
}
