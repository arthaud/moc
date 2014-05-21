package moc.type;

public class TCLASS implements TTYPE {
    private String name;
    private TCLASS superClass;
    private TSTRUCT attributes;
    private LMETHODS methods;

    public TCLASS(String name, TCLASS superClass, TSTRUCT attributes, LMETHODS methods) {
        this.name = name;
        this.superClass = superClass;
        this.attributes = attributes;
        this.methods = methods;
    }

    public String getName() {
        return name;
    }

    public TCLASS getSuperType() {
        return superClass;
    }

    public int getSize() {
        return superClass.getSize() + attributes.getSize();
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
        return "class " + name + " : " + superClass.getName() + "\n" + attributes + "\n" + methods;
    }
}
