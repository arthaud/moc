package moc.type;

import java.util.ArrayList;

public class LMETHODS extends ArrayList<METHOD> {
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (METHOD f: this) {
            if(sb.length() > 0)
                sb.append(", ");

            sb.append(f);
        }

        return sb.toString();
    }
}
