package moc.type;

public class TARRAY implements TTYPE {
    private TTYPE elementsType;
    private int nbElements;

    public TARRAY(TTYPE elementsType, int nbElements) {
        this.elementsType = elementsType;
        this.nbElements = nbElements;
    }

    public TTYPE getElementsType() {
        return elementsType;
    }

    public int getSize() {
        return nbElements * elementsType.getSize();
    }

    public int getNbElements() {
        return nbElements;
    }

    public boolean constructFrom(TTYPE other) {
        return false;
    }

    public boolean comparableWith(TTYPE other, String op) {
        return false;
    }

    public boolean binaryUsable(TTYPE other, String op) {
        return false;
    }

    public boolean unaryUsable(String op) {
        return false;
    }

    public boolean isCastableTo(TTYPE other) {
        return other instanceof TINTEGER
            || other instanceof TPOINTER;
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        if(other instanceof TARRAY) {
            TARRAY p = (TARRAY) other;
            return nbElements == p.getNbElements()
                && elementsType.equals(p.getElementsType());
        }
        else
            return false;
    }

    public String toString() {
        return "array of " + nbElements + " " + elementsType.toString();
    }
}
