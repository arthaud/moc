package moc.cg;

import moc.type.TTYPE;
import moc.type.TFUNCTION;

/**
 * The TAM machine and its generation functions
 */
public class Mx86 extends AbstractMachine {
    
    public static final String[] registerNames = {
        "eax", "ebx", "ecx", "edx", "esi", "edi"
    };

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
        return 4;
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
        code.prependAsm(function.getName() + ":");
        return code;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        return null;
    }

    public Code genReturn(Code returnVal_) {
        x86Code returnVal = (x86Code) returnVal_;

        if(! returnVal.resultRegisterName().equals("eax")) {
            returnVal.appendAsm("mov eax " + returnVal.resultRegisterName());
        }

        return returnVal;
    }

    public Code includeAsm(String asmCode) {
        return new Code(asmCode);
    }

    public Code genAffectation(Code address_, Code affectedVal_) {
        x86Code address = (x86Code) address_;
        x86Code affectedVal = (x86Code) affectedVal_;

        address.appendAsm("mov [" + address.resultRegisterName() + "] " + affectedVal.resultRegisterName());
        return address;
    }

    public Code genBinary(Code leftOperand_, Code rightOperand_, String operator) {
        x86Code leftOperand = (x86Code) leftOperand_;
        x86Code rightOperand = (x86Code) rightOperand_;

        switch(operator) {
            case "+":
                leftOperand.appendAsm("add " + leftOperand.resultRegisterName() + " " + rightOperand.resultRegisterName());
                break;
            case "-":
                leftOperand.appendAsm("sub " + leftOperand.resultRegisterName() + " " + rightOperand.resultRegisterName());
                break;
            default:
                throw new RuntimeException("Unknown operator.");
        }

        return leftOperand;
    }

    public Code genUnary(Code operand_, String operator) {
        x86Code operand = (x86Code) operand_;

        switch(operator) {
            case "-":
                operand.appendAsm("neg " + operand.resultRegisterName());
                break;
            default:
                throw new RuntimeException("Unknown operator.");
        }

        return operand;
    }

    public Code genCast(TTYPE type, Code castedCode) {
        return null;
    }

    public Code genCall(String ident, Code arguments_) {
        x86Code arguments = (x86Code) arguments_;

        arguments.appendAsm("call " + ident);
        arguments.setResultRegister(0);
        return arguments;
    }
}
