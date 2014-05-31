package moc.type;

public class METHOD {
    private TCLASS defClass;
    private TTYPE returnType;
    private LFIELDS parameters;
    private boolean isStatic;

    public METHOD(TCLASS defClass, TTYPE returnType, LFIELDS parameters, boolean isStatic) {
        this.defClass = defClass;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isStatic = isStatic;
    }

    public TCLASS getDefClass() {
        return defClass;
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

    public boolean compareName(LFIELDS parameters) {
        if (this.parameters.size() != parameters.size())
            return false;

        for(int i=0; i < this.parameters.size(); i++) {
            if (!this.parameters.get(i).getName().equals(parameters.get(i).getName()))
                return false;
        }

        return true;
    }

    public boolean callable(LFIELDS parameters) {
        if (this.parameters.size() != parameters.size())
            return false;

        for(int i=0; i < this.parameters.size(); i++) {
            if (!this.parameters.get(i).getName().equals(parameters.get(i).getName())
                || (this.parameters.get(i).getType() == null && parameters.get(i).getType() != null)
                || (this.parameters.get(i).getType() != null && !this.parameters.get(i).getType().constructFrom(parameters.get(i).getType())))
                return false;
        }

        return true;
    }

    public String getLabel() {
        return parameters.getLabel();
    }

    /**
     * Get the offset of the method in the vtable
     */
    public int getVtableOffset() {
        return defClass.getVtable().indexOf(this);
    }

    public String toString() {
        return (isStatic ? "+" : "-") + " " + returnType.toString() + " (" + parameters.toString() + ")";
    }
}
