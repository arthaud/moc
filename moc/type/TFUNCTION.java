package moc.type;

public class TFUNCTION implements TTYPE {
    private String name;
    private TTYPE returnType;
    private LTYPES parameterTypes;

    public TFUNCTION(String name, TTYPE returnType, LTYPES parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public int getSize() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public TTYPE getReturnType() {
        return returnType;
    }

    public LTYPES getParameterTypes() {
        return parameterTypes;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return returnType.toString() + " " + name + " (" + parameterTypes.toString() + ")";
    }
}
