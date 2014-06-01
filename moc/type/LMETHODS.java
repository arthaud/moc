package moc.type;

import java.util.ArrayList;

public class LMETHODS extends ArrayList<METHOD> {
    private static final long serialVersionUID = 1L;

    public METHOD findByName(LFIELDS parameters) {
        for (METHOD f: this) {
            if(f.compareName(parameters))
                return f;
        }

        return null;
    }

    public METHOD findCallable(LFIELDS parameters) {
        for (METHOD f: this) {
            if(f.callable(parameters))
                return f;
        }

        return null;
    }

    public boolean hasConstructor() {
        for (METHOD f: this) {
            if(f.isConstructor())
                return true;
        }

        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (METHOD f: this) {
            if(sb.length() > 0)
                sb.append("\n");

            sb.append(f);
        }

        return sb.toString();
    }
}
