package moc.type;

public class METHOD {
    private TTYPE returnType;
    private LFIELDS parameters;
    private boolean isStatic;

    public METHOD(TTYPE returnType, LFIELDS parameters, boolean isStatic) {
        this.returnType = returnType;
        this.parameters = parameters;
        this.isStatic = isStatic;
    }

    public TTYPE getReturnType() {
        return returnType;
    }

    public LFIELDS getParameters() {
        return parameters;
    }

    public boolean isStatic() {
        return isStatic;
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
        return false; // no need
    }

    public String toString() {
        return returnType.toString() + " " + " (" + parameters.toString() + ")";
    }
}
