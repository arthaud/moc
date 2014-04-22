package moc.cg;

import moc.compiler.MOCException;
import moc.type.TTYPE;

/**
 * This interface describes a target machine
 */
public interface IMachine {
    /**
     * Target file suffix (.tam for example)
     */
    String getSuffix();

    /**
     * Writes the code in a file from the source file name and the suffix
     */
    void writeCode(String fileName, String code) throws MOCException;

    Code genFunction(String label, int paramSize, int retSize, Code code);

    Code genConditional(Code condition, Code trueBloc, Code falseBloc);

    Code genReturn(Code returnVal);

    Code includeAsm(String asmCode);

    Code genAffectation(Code address, Code affectedVal);

    Code genBinary(Code leftOperand, Code rightOperand, String operator);

    Code genUnary(Code operand, String operator);

    Code genCast(TTYPE type, Code castedCode);

    Code genCall(String ident, Code arguments);

    String genComment(String comm);

    int getIntSize();

    int getCharSize();

    int getBoolSize();

    int getPointerSize();
}
