package moc.cg;

import moc.compiler.MOCException;
import moc.type.TTYPE;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;
import moc.st.ST;

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

    Code genLoop(Code condition, Code Bloc);

    Code genFunctionReturn(Code returnVal, TFUNCTION fun);

    Code includeAsm(String asmCode, ST symbolsTable);

    Code genAffectation(Code address, Code affectedVal, TTYPE type);

    Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator);

    Code genUnary(Code operand, TTYPE type, String operator);

    Code genCast(TTYPE newType, TTYPE oldType, Code castedCode);

    Code genFunctionCall(TFUNCTION func, Code arguments);

    Code genArg(Code argument, TTYPE type);

    // declare a variable
    Code genDecl(INFOVAR info);

    // declare a variable with an initial value
    Code genDecl(INFOVAR info, Code value);

    // declare a global variable
    Code genDeclGlobal(INFOVAR info);

    // expression instruction
    Code genInst(TTYPE type, Code value);

    String genComment(String comm);

    Code genAcces(Code pointerCode, TTYPE pointedType);

    Code genArrayAcces(Code pointerCode, TTYPE pointerType, Code posCode, TTYPE posType);

    // removes local variables after instCode
    Code genBloc(Code instsCode, VariableLocator vloc);

    Location genGlobalLocation();

    ParametersLocator getParametersLocator();

    VariableLocator getVariableLocator();

    // converts a location to its representation in asm
    String genLocation(Location loc);

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
