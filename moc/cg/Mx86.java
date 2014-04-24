package moc.cg;

import moc.type.TTYPE;
import moc.type.TFUNCTION;

/**
 * The TAM machine and its generation functions
 */
public class Mx86 extends AbstractMachine {

    @Override
    public String getSuffix() {
        return "x86";
    }

    public int getIntSize() {
        return 4;
    }

    public int getCharSize() {
        return 1;
    }

    public int getBoolSize() {
        return 1;
    }

    public int getPointerSize() {
        return 1;
    }

    public class X86ParametersLocator implements ParametersLocator {
        private int offset;

        public X86ParametersLocator() {
            offset = +8;
        }

        public Location generate(TTYPE param) {
            int res = offset;
            offset += param.getSize();
            return new Location(Location.LocationType.STACKFRAME, res);
        }
    }

    public ParametersLocator getParametersLocator() {
        return new X86ParametersLocator();
    }

    public Code genFunction(TFUNCTION function, Code code) {
        return null;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        return null;
    }

    public Code genReturn(Code returnVal) {
        return null;
    }

    public Code includeAsm(String asmCode) {
        return null;
    }

    public Code genAffectation(Code address, Code affectedVal) {
        return null;
    }

    public Code genBinary(Code leftOperand, Code rightOperand, String operator) {
        return null;
    }

    public Code genUnary(Code operand, String operator) {
        return null;
    }

    public Code genCast(TTYPE type, Code castedCode) {
        return null;
    }

    public Code genCall(String ident, Code arguments) {
        return null;
    }
}