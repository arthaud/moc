package moc.cg;

import moc.type.TTYPE;
import moc.type.TVOID;
import moc.type.TBOOL;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;

/**
 * The TAM machine and its generation functions
 */
public class MTAM extends AbstractMachine {

    private int initOffset = 0;

    public String getName() {
        return "tam";
    }

    public String getSuffix() {
        return "tam";
    }

    public int getIntSize() {
        return 1;
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

    public class TamParametersLocator implements ParametersLocator {
        private int offset;

        public TamParametersLocator() {
            offset = -1;
        }

        public Location generate(TTYPE param) {
            int res = offset;
            offset -= param.getSize();
            return new Location(Location.LocationType.STACKFRAME, res);
        }
    }

    public ParametersLocator getParametersLocator() {
        return new TamParametersLocator();
    }

    public class TamVariableLocator implements VariableLocator {
        private int offset;
        private int localOffset;

        public TamVariableLocator(int offset) {
            this.offset = offset;
            localOffset = 0;
        }

        public Location generate(TTYPE param) {
            int res = offset;
            offset += param.getSize();
            localOffset += param.getSize();
            
            return new Location(Location.LocationType.STACKFRAME, res);
        }

        public int getLocalOffset() {
            return localOffset;
        }

        public VariableLocator getChild() {
            return new TamVariableLocator(offset);
        }
    }

    public VariableLocator getVariableLocator() {
        return new TamVariableLocator(3);
    }

    // converts a location to its representation in asm
    public String genLocation(Location loc) {
        if(loc.getType() == Location.LocationType.STACKFRAME) {
            return loc.getOffset() + "[LB]";
        }

        return "" + loc.getOffset();
    }

    public Code genFunction(TFUNCTION function, Code code) {
        code.prependAsm("f_" + function.getName() + ":");
        code.prependAsm("\n" + genComment("### " + function + " #############"));

        if (function.getReturnType() instanceof TVOID) {
            code.appendAsm("RETURN (0) " + function.getParameterTypes().getSize());
        }

        return code;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        int num = getLabelNum();
        String st;
        boolean hasElse = !falseBloc.getAsm().equals("");

        Code retCode = genVal(condition, new TBOOL(1));
        retCode.prependAsm(genComment("if condition :"));

        if(hasElse)
            st = "JUMPIF (0) else_" + num;
        else
            st = "JUMPIF (0) end_if_" + num;

        retCode.appendAsm(st);
        retCode.appendAsm(trueBloc.getAsm());
        if(hasElse) {
            retCode.appendAsm("JUMP end_if_" + num);
            retCode.appendAsm("else_" + num + ":");
            retCode.appendAsm(falseBloc.getAsm());
        }
        retCode.appendAsm("end_if_" + num + ":");

        return retCode;
    }

    public Code genLoop(Code condition, Code bloc) {
        int num = getLabelNum();
        Code retCode = genVal(condition, new TBOOL(1));
        retCode.prependAsm(genComment("loop condition :"));
        retCode.prependAsm("loop_" + num + ":");
        retCode.appendAsm("JUMPIF (0) end_loop_" + num);
        retCode.appendAsm(bloc.getAsm());
        retCode.appendAsm("JUMP loop_" + num);
        retCode.appendAsm("end_loop_" + num + ":");
        return retCode;
    }

    public Code genReturn(Code returnVal, TFUNCTION fun) {
        int retsize = fun.getReturnType().getSize();
        int paramsize = fun.getParameterTypes().getSize();
        Code c = genVal(returnVal, fun.getReturnType());
        c.appendAsm("RETURN (" + retsize + ") " + paramsize);
        return c;
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        Code retCode = genVal(affectedVal, type);
        retCode.prependAsm(genComment("affected value :"));

        if (address.getLocation() != null) {
            retCode.appendAsm("STORE (" + type.getSize() + ") " + address.getLocation().getOffset() + "[LB] " + genComment("affectation"));
        } else {
            assert(address.getIsAddress());
            retCode.appendAsm(genComment("affected address :"));
            retCode.appendAsm(address.getAsm());
            retCode.appendAsm("STOREI (" + type.getSize() + ") " + genComment("affectation"));
        }

        return retCode;
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        Code res = genVal(leftOperand, leftType);
        res.prependAsm(genComment("left operand :"));
        res.appendAsm(genComment("right operand :"));
        res.appendAsm(genVal(rightOperand, rightType).getAsm());
        String op;

        switch(operator) {
        case "+":
            op = "SUBR IAdd";
            break;
        case "-":
            op = "SUBR ISub";
            break;
        case "<":
            op = "SUBR ILss";
            break;
        case ">":
            op = "SUBR IGtr";
            break;
        case "<=":
            op = "SUBR ILeq";
            break;
        case ">=":
            op = "SUBR IGeq";
            break;
        case "==":
            op = "SUBR IEq";
            break;
        case "!=":
            op = "SUBR INeq";
            break;
        case "||":
            op = "SUBR BOr";
            break;
        case "&&":
            op = "SUBR BAnd";
            break;
        case "*":
            op = "SUBR IMul";
            break;
        case "/":
            op = "SUBR IDiv";
            break;
        case "%":
            op = "SUBR IMod";
            break;
        default:
            throw new RuntimeException("Unknown operator.");
        }

        res.appendAsm(op);
        return res;
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        Code c = genVal(operand, type);

        switch(operator) {
        case "-":
            c.appendAsm("SUBR INeg");
            break;
        case "!":
            c.appendAsm("SUBR BNeg");
            break;
        default:
            throw new RuntimeException("Unknown operator.");
        }

        return c;
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        return genVal(castedCode, oldType);
    }

    public Code genCall(TFUNCTION f, Code arguments) {
        arguments.appendAsm("CALL (LB) f_" + f.getName());
        return arguments;
    }

    // declare a variable
    public Code genDecl(INFOVAR info) {
        return new Code("PUSH " + info.getType().getSize());
    }

    // declare a variable with an initial value
    public Code genDecl(INFOVAR info, Code value) {
        return genVal(value, info.getType());
    }
    
    public Code genArg(Code argument, TTYPE type) {
        return genVal(argument, type);
    }

    // expression instruction
    public Code genInst(TTYPE type, Code value) {
        if(!(type instanceof TVOID))
            value.appendAsm("POP (0) " + type.getSize());

        return value;
    }

    /** the generated code puts the address of the pointed var on the top of the stack */
    public Code genAcces(Code pointerCode, TTYPE pointedType) {
        if (pointerCode.getIsAddress()) {
            pointerCode.appendAsm("LOADI (" + pointedType.getSize() + ")");
        }

        pointerCode.setIsAddress(true);
        pointerCode.setLocation(null);
        return pointerCode;
    }

    public Code genBloc(Code c, VariableLocator vloc) {
        TamVariableLocator vl = (TamVariableLocator) vloc;

        if (vl.getLocalOffset() > 0) {
            c.appendAsm("POP (0) " + vl.getLocalOffset() + " " + genComment("removing local variables"));
        }

        return c;
    }

    public Code genVariable(INFOVAR i) {
        Code retCode = new Code("LOAD (" + i.getSize() + ") " + i.getLocation().getOffset() + "[LB]");
        retCode.setIsAddress(false);
        retCode.setLocation(i.getLocation());
        return retCode;
    }

    public Code genInt(String cst) {
        return new Code("LOADL " + cst);
    }

    public Code genString(String txt) {
        Code retCode = new Code("LOADL " + initOffset + " " + genComment("string " + txt));
        retCode.setIsAddress(false);

        txt = txt.substring(1, txt.length() - 1); // remove the ""
        initCode += genComment(txt) + "\n";
        for (int i = 0; i < txt.length(); i++) {
            if(txt.charAt(i) == '\\' && i + 1 < txt.length()) { // special characters
                i++;
                if(txt.charAt(i) == '0')
                    initCode += "LOADL 0\n";
                else
                    initCode += "LOADL '\\" + txt.charAt(i) + "'\n";
            }
            else
                initCode += "LOADL '" + txt.charAt(i) + "'\n";

            initOffset++;
        }

        initCode += "LOADL 0\n";
        initOffset++;

        return retCode;
    }

    public Code genNull() {
        return new Code("LOADL 0");
    }

    public Code genBool(int b) {
        return new Code("LOADL " + b);
    }

    public Code genChar(String c) {
        if(c.equals("'\\0'"))
            return new Code("LOADL 0");
        else
            return new Code("LOADL " + c);
    }

    /**
     * Ensures the Code gives a value
     */
    private Code genVal(Code operand, TTYPE type) {
        if(operand.getLocation() != null) {
            return new Code("LOAD (" + type.getSize() + ") " + operand.getLocation().getOffset() + "[LB]");
        }
        else if(operand.getIsAddress()) {
            operand.appendAsm("LOADI (" + type.getSize() + ")");
            operand.setIsAddress(false);
        }

        return operand;
    }
}
