package moc.type;

import java.util.ArrayList;

public class LFIELDS extends ArrayList<FIELD> {
    private static final long serialVersionUID = 1L;

    public int getSize() {
        int size = 0;

        for (FIELD f : this) {
            size += f.getType().getSize();
        }

        return size;
    }

    public FIELD search(String name) {
        for (FIELD f : this) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (FIELD f : this) {
            if(sb.length() > 0)
                sb.append(", ");

            sb.append(f);
        }

        return sb.toString();
    }
}