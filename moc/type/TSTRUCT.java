package moc.type;

public class TSTRUCT implements TTYPE {
    private LFIELDS fields;

    public TSTRUCT() {
        fields = new LFIELDS();
    }

    public LFIELDS getFields() {
        return fields;
    }

    public int getSize() {
        return fields.getSize();
    }

    public boolean hasField(String name) {
        return fields.hasField(name);
    }

    public FIELD getField(String name) {
        return fields.getField(name);
    }

    public int getFieldOffset(String name) {
        return fields.getFieldOffset(name);
    }

    public void addField(String name, TTYPE type) {
        fields.add(new FIELD(name, type));
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
        if(other instanceof TSTRUCT) {
            TSTRUCT p = (TSTRUCT) other;
            return fields.equals(p.getFields());
        }
        else return false;
    }

    public String toString() {
        return "struct{" + fields.toString() + "}";
    }
}
