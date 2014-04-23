package moc.type;

import java.util.ArrayList;

public class LTYPES extends ArrayList<TTYPE> {

    public int getSize() {
        int t = 0;

        for (TTYPE c : this) {
            t += c.getSize();
        }

        return t;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (TTYPE c : this) {
            if(sb.length() > 0)
                sb.append(", ");

            sb.append(c);
        }

        return sb.toString();
    }
}
