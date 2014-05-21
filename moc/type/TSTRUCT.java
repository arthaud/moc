package moc.type;

public class TSTRUCT implements TTYPE {
    private LFIELDS fields;

    public TSTRUCT(LFIELDS fields) {
        this.fields = fields;
    }

    public int getSize() {
        return fields.getSize();
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
        return false;
    }

    public boolean testable() {
        return false;
    }

    public boolean equals(TTYPE other) {
        return false;
    }

    public String toString() {
        return "struct {" + fields + "}";
    }
}
