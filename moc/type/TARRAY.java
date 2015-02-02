package moc.type;

public class TARRAY extends TPOINTER {
    private String nbElts;

    public TARRAY(TTYPE type, int size, String nbElts) {
        super(type, size);
        this.nbElts = nbElts;
    }

    public int getStackSize() {
        if (nbElts == null)
            throw new RuntimeException("Please give a size when declaring an array");

        return type.getSize() * Integer.valueOf(nbElts);
    }

    public String toString() {
        return "array of " + type.toString();
    }
}
