package moc.type;

public interface TTYPE {
    /**
     * The size of the data type: depends on the machine
     */
    public int getSize();

    /**
     * Compatibility function with another type
     */
    public boolean compareTo(TTYPE other);

    public String toString();
}
