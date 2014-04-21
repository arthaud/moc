package moc.cg;

import moc.compiler.MOCException;

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
    
    Code genAffectation(Code Adress, Code affectedVal);

    Code genBinary(Code leftOperand, Code rightOperand, String Operator);

    Code genUnary(Code Operand, String Operator);

    Code genCast(DType type, Code CastedCode);

    Code genCall(String ident, Code arguments);
     
   }
