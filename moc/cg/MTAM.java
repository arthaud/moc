package moc.cg;

/**
 * The TAM machine and its generation functions
 */
public class MTAM extends AbstractMachine {

    @Override
    public String getSuffix() {
        return "tam";
    }

    int getIntSize() {
        return 1;
    }

    int getCharSize() {
        return 1;
    }

    int getBoolSize() {
        return 1;
    }

    int getPointerSize() {
        return 1;
    }


}
