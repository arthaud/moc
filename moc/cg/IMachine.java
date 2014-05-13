package moc.cg;

import moc.compiler.MOCException;
import moc.type.TTYPE;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;

/**
 * This interface describes a target machine
 */
public interface IMachine {
    /**
     * Machine name
     */
    String getName();

    /**
     * Target file suffix (.tam for example)
     */
    String getSuffix();

    /**
     * Writes the code in a file from the source file name and the suffix
     */
    void writeCode(String fileName, String code) throws MOCException;

    Code genFunction(TFUNCTION function, Code code);

    Code genConditional(Code condition, Code trueBloc, Code falseBloc);

    Code genReturn(Code returnVal, TFUNCTION fun);

    Code includeAsm(String asmCode);

    Code genAffectation(Code address, Code affectedVal, TTYPE type);

    Code genBinary(Code leftOperand, Code rightOperand, String operator);

    Code genUnary(Code operand, String operator);

    Code genCast(TTYPE type, Code castedCode);

    Code genCall(String ident, Code arguments);

    //declare a null variable  
    Code genDecl(TTYPE type);

    String genComment(String comm);

    Code genAcces(Code pointerCode, TTYPE pointedType);

    //removes local variables after instCode 
    Code genBloc(Code instsCode, VariableLocator vloc);

    ParametersLocator getParametersLocator();

    VariableLocator getVariableLocator();
    
    /**
     * Terminal cases, to load variable, constants...
    */
    Code genVariable(INFOVAR i);

    Code genInt(String cst);

    Code genString(String txt);

    Code genNull();

    Code genBool(int b);

    Code genChar(String c);

    /**
     * Size of basic types
     */

    int getIntSize();

    int getCharSize();

    int getBoolSize();

    int getPointerSize();
}
