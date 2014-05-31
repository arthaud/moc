package moc.type;

import java.util.ArrayList;

public class LFIELDS extends ArrayList<FIELD> {
    private static final long serialVersionUID = 1L;

    public int getSize() {
        int size = 0;

        for (FIELD f : this) {
            if (f.getType() != null)
                size += f.getType().getSize();
        }

        return size;
    }

    /**
     * Get the offset of a field
     */
    public int getOffset(String name) {
        assert(find(name) != null);
        int offset = 0;

        for (FIELD f : this) {
            if (f.getName().equals(name))
                return offset;

            offset += f.getType().getSize();
        }

        return -1; // should never happens
    }

    public FIELD find(String name) {
        for (FIELD f : this) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
    }

    public String getLabel() {
        StringBuffer sb = new StringBuffer();

        for (FIELD f : this) {
            if(sb.length() > 0)
                sb.append("_");

            sb.append(f.getName());
        }

        return sb.toString();
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
