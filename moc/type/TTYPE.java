package moc.type;

public interface TTYPE {
    /**
     * The size of the data type: depends on the machine
     */
    public int getSize();

    /**
     * Returns true if we can do "a = b" with b of type other
     */
    public boolean constructFrom(TTYPE other);

    /**
     * Returns true if we can compare the two types
     */
    public boolean comparableWith(TTYPE other, String op);

    /**
     * Returns true if we can use the binary op
     */
    public boolean binaryUsable(TTYPE other, String op);

    /**
     * Returns true if we can use the unary op
     */
    public boolean unaryUsable(String op);

    /**
     * Returns true if we can cast to the type other
     */
    public boolean isCastableTo(TTYPE other);

    /**
     * Returns true if we can test the type, with if(..)
     */
    public boolean testable();

    public boolean equals(TTYPE other);

    public String toString();
}
