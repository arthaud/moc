package moc.cg;

abstract public class DefaultVariableLocator implements VariableLocator {
    protected int offset;
    protected int localOffset;

    public DefaultVariableLocator(int offset) {
        this.offset = offset;
        localOffset = 0;
    }

    public int getLocalOffset() {
        return localOffset;
    }
}

