package moc.type;

public class TPOINTER implements TTYPE {
    private TTYPE type;
    private int size;

    public TPOINTER(TTYPE type, int size) {
        this.type = type;
        this.size = size;
    }

    public TTYPE getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public boolean constructFrom(TTYPE other) {
        if(other instanceof TPOINTER)
        {
            TPOINTER p = (TPOINTER) other;
            return type.equals(p.getType());
        }
        else
            return other instanceof TNULL;
    }

    public boolean comparableWith(TTYPE other, String op) {
        if(!op.equals("==") && !op.equals("!="))
            return false;

        if(other instanceof TPOINTER)
        {
            TPOINTER p = (TPOINTER) other;
            return type.equals(p.getType());
        }
        else
            return other instanceof TNULL;
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
        if(other instanceof TPOINTER) {
            TPOINTER p = (TPOINTER) other;
            return type.equals(p.getType());
        }
        else
            return false;
    }

    public String toString() {
        return "pointer on " + type.toString();
    }
}
