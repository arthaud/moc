package moc.type;

public class TCLASS implements TTYPE {
    private String name;
    private TCLASS superClass;
    private LFIELDS attributes;
    private LMETHODS methods;

    public TCLASS(String name, TCLASS superClass, LFIELDS attributes, LMETHODS methods) {
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
        if (superClass != null) {
            return superClass.getSize() + attributes.getSize();
        } else {
            return attributes.getSize();
        }
    }

    /**
     * Find an attribute of the class
     */
    public FIELD findAttribute(String name) {
        return findAttribute(name, true);
    }

    public FIELD findAttribute(String name, boolean lookSuperClass) {
        FIELD attribute = attributes.find(name);

        if (attribute == null && lookSuperClass && superClass != null)
            return superClass.findAttribute(name, true);

        return attribute;
    }

    /**
     * Get the offset of an attribute
     */
    public int getAttributeOffset(String name) {
        assert(findAttribute(name) != null);

        if (attributes.find(name) != null) {
            if (superClass != null) {
                return superClass.getSize() + attributes.getOffset(name);
            }
            else {
                return attributes.getOffset(name);
            }
        }
        else {
            return superClass.getAttributeOffset(name);
        }
    }

    /**
     * Find a method by name
     */
    public METHOD findMethodByName(LFIELDS parameters) {
        return findMethodByName(parameters, true);
    }

    public METHOD findMethodByName(LFIELDS parameters, boolean lookSuperClass) {
        METHOD method = methods.findByName(parameters);

        if (method == null && lookSuperClass && superClass != null)
            return superClass.findMethodByName(parameters, true);

        return method;
    }

    /**
     * Find a callable method with the corresponding parameters
     */
    public METHOD findCallableMethod(LFIELDS parameters) {
        return findCallableMethod(parameters, true);
    }

    public METHOD findCallableMethod(LFIELDS parameters, boolean lookSuperClass) {
        METHOD method = methods.findCallable(parameters);

        if (method == null && lookSuperClass && superClass != null)
            return superClass.findCallableMethod(parameters, true);

        return method;
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
        if(other instanceof TCLASS) {
            TCLASS cl = (TCLASS) other;
            return name.equals(cl.getName());
        }
        else
            return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("class ");
        sb.append(name);

        if(superClass != null)
            sb.append(" : " + superClass.getName());

        if(attributes.size() > 0) {
            sb.append("\n");
            sb.append(attributes);
        }

        if(methods.size() > 0) {
            sb.append("\n");
            sb.append(methods);
        }

        return sb.toString();
    }
}
