package moc.type;

public class TCHAR implements TTYPE {
    private int size;

    public TCHAR(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean compareTo(TTYPE other) {
        return false; /* TODO */
    }

    public String toString() {
        return "char";
    }
}
