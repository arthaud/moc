package moc.type;

import java.util.ArrayList;

public class LFIELDS extends ArrayList<FIELD> {

    public int getSize() {
        int t = 0;

        for (FIELD c : this) {
            t += c.getType().getSize();
        }

        return t;
    }

    public boolean hasField(String name) {
        for (FIELD c : this) {
            if(c.getName().equals(name))
                return true;
        }

        return false;
    }

    public FIELD getField(String name) {
        for (FIELD c : this) {
            if(c.getName().equals(name))
                return c;
        }

        return null;
    }

    public int getFieldOffset(String name) {
        assert(hasField(name));
        int offset = 0;

        for (FIELD c : this) {
            if(c.getName().equals(name))
                return offset;

            offset += c.getType().getSize();
        }

        return -1;
    }

    public boolean equals(LFIELDS other) {
        if(size() != other.size())
            return false;

        // the order is important here!
        for(int i=0; i < size(); i++) {
            if(!get(i).equals(other.get(i)))
                return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (FIELD c : this) {
            if(sb.length() > 0)
                sb.append(", ");

            sb.append(c);
        }

        return sb.toString();
    }
}
