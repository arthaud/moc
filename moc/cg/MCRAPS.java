package moc.cg;

import moc.st.INFOVAR;
import moc.type.TBOOL;
import moc.type.TFUNCTION;
import moc.type.TTYPE;
import moc.type.TVOID;

/**
 * The CRAPS machine and its generation functions
 */
public class MCRAPS extends AbstractMachine {

    public MCRAPS() {
    }

    public String getName() {
        return "SPARC";
    }

    public String getSuffix() {
        return "asm";
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
        return 4;
    }

    public class XSPARCParametersLocator implements ParametersLocator {
        public Location generate(TTYPE param) {
            throw new UnsupportedOperationException("SPARC");
        }
    }

    public ParametersLocator getParametersLocator() {
        return new DefaultParametersLocator(4);
    }

    public class XSPARCVariableLocator extends DefaultVariableLocator {
        public XSPARCVariableLocator(int offset) {
            super(offset);
        }

        public Location generate(TTYPE param) {
            throw new UnsupportedOperationException("SPARC");
        }
        public VariableLocator getChild() {
            return new XSPARCVariableLocator(offset);
        }
    }

    public VariableLocator getVariableLocator() {
        return new XSPARCVariableLocator(0);
    }

    public String genLocation(Location l) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genFunction(TFUNCTION function, Code code) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genConditional(Code c, Code trueBloc, Code falseBloc) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genLoop(Code condition, Code c) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genFunctionReturn(Code returnVal, TFUNCTION function) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genFunctionCall(TFUNCTION f, Code arguments) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genDecl(INFOVAR info) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genDecl(INFOVAR info, Code value) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genInst(TTYPE type, Code value) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genArg(Code e, TTYPE type) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genAcces(Code pointerCode, TTYPE pointedType) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genBloc(Code instsCode , VariableLocator vloc) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genVariable(INFOVAR i) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genSelf() {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genInt(String cst) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genString(String txt) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genNull() {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genBool(int b) {
        throw new UnsupportedOperationException("SPARC");
    }

    public Code genChar(String c) {
        throw new UnsupportedOperationException("SPARC");
    }

}
