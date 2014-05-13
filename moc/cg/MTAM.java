package moc.cg;

import moc.type.TTYPE;
import moc.type.TVOID;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;

/**
 * The TAM machine and its generation functions
 */
public class MTAM extends AbstractMachine {
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

    public Code genFunction(TFUNCTION function, Code code) {
        code.prependAsm("_" + function.getName() + ":");
        if (function.getReturnType() instanceof TVOID) {
            code.appendAsm("RETURN (" + function.getParameterTypes().getSize() + ") 0");
        }
        return code;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        int num = getLabelNum();
        String st;
        boolean hasElse = !falseBloc.getAsm().equals("");

        Code retCode = new Code(condition.getAsm());
        if(hasElse)
            st = "JUMPIF (0) ELSEBLOC_" + num;
        else
            st = "JUMPIF (0) END_COND_" + num;

        retCode.appendAsm(st);
        retCode.appendAsm(trueBloc.getAsm());
        if(hasElse) {
            retCode.appendAsm("JUMP END_COND_" + num ); 
            retCode.appendAsm("ELSEBLOC_" + num + ":");
            retCode.appendAsm(falseBloc.getAsm());
        }
        retCode.appendAsm("END_COND_" + num + ":"); 
        
        return retCode;
    }

    public Code genReturn(Code returnVal, TFUNCTION fun) {
        int retsize = fun.getReturnType().getSize();
        int paramsize = fun.getParameterTypes().getSize();
        Code retCode = new Code("RETURN (" + paramsize + ") " + retsize );
        retCode.prependAsm(returnVal.getAsm());
        return retCode;
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        if (!address.getIsAddress() && address.getAddress() == 0)
            return new Code(";Affectation error: left operand has no address");

        Code retCode= genVal(affectedVal);
        if (address.getAddress() != 0) {
            retCode.appendAsm("STORE (" + type.getSize() + ") " + address.getAddress() + "[LB]");
        } else {
            retCode.appendAsm(address.getAsm());
            retCode.appendAsm("STOREI (" +type.getSize() + ")");
        }

        return retCode;
    }

    public Code genBinary(Code leftOperand, Code rightOperand, String operator) {
        Code res = genVal(leftOperand);
        res.appendAsm(genVal(rightOperand).getAsm());
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

    public Code genUnary(Code operand, String operator) {
        switch(operator) {
        case "+":
            break;
        case "-":
            operand.appendAsm("SUBR INeg");
            break;
        case "!":
            operand.appendAsm("SUBR BNeg");
            break;
        default:
            throw new RuntimeException("Unknown operator.");
        }

        return operand;
    }

    public Code genCast(TTYPE type, Code castedCode) {
        return null;
    }

    public Code genCall(String ident, Code arguments) {
        Code c = arguments;
        c.appendAsm("CALL (LB) _" + ident);
        return c;
    }

    public Code genDecl(TTYPE type) {
        return new Code("PUSH " + type.getSize());
    }

    /** the generated code puts the address of the pointed var on the top of the stack */
    public Code genAcces(Code pointerCode, TTYPE pointed_type) {
        if (pointerCode.getIsAddress()) {
            pointerCode.appendAsm("LOADI " + pointed_type.getSize());
        }
        pointerCode.setIsAddress(true);
        pointerCode.setAddress(0);
        pointerCode.setTypeSize(pointed_type.getSize());
        return pointerCode;
    }

    public Code genBloc(Code c, VariableLocator vloc) {
        TamVariableLocator vl = (TamVariableLocator) vloc;
        String st;

        if (vl.getLocalOffset() == 0) {
            st = ";no locals to POP";
        } else {
            st= "POP (0) " + vl.getLocalOffset() + ";removing local variables";
        }

        Code ret = c;
        ret.appendAsm(st);
        return ret;
    }

    public Code genVariable(INFOVAR i) {
        Code retCode = new Code("LOAD (" + i.getSize() + ") " + i.getLocation().getOffset() + "[LB]");
        retCode.setIsAddress(false);
        retCode.setAddress(i.getLocation().getOffset());
        retCode.setTypeSize(i.getSize());
        return retCode;
    }

    public Code genInt(String cst) {
        Code retCode = new Code("LOADL " + cst);
        retCode.setIsAddress(false);
        retCode.setAddress(0);
        return retCode;
    }

    public Code genString(String txt) {
        return null;
    }

    public Code genNull() {
        return new Code("LOADL 0");
    }

    public Code genBool(int b) {
        Code retCode = new Code("LOADL " + b);
        retCode.setIsAddress(false);
        retCode.setAddress(0);
        return retCode;
    }

    public Code genChar(String c) {
        return null;
    }


    private int labelNum = 0;

    private int getLabelNum() {
        labelNum++;
        return labelNum -1;
    }

    /**
     * Ensures the Code gives a value
     */
    private Code genVal(Code operand) {
        if(!operand.getIsAddress())
            return operand;

        if(operand.getAddress() == 0) {
            operand.appendAsm("LOADI (" + operand.getTypeSize() + ")");
        } else {
            operand.appendAsm("LOAD (" + operand.getTypeSize() + ") " + operand.getAddress() + "[LB]");
        }

        return operand;
    }
}
