package moc.cg;

import moc.type.TTYPE;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;

/**
 * The TAM machine and its generation functions
 */
public class Mx86 extends AbstractMachine {

    public static final String[] registerNames = {
        "eax", "ebx", "ecx", "edx", "esi", "edi"
    };

    public String getName() {
        return "x86_32";
    }

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

    public class X86VariableLocator implements VariableLocator {
        private int offset;
        private int localOffset;

        public X86VariableLocator(int offset) {
            this.offset = offset;
            localOffset = 0;
        }

        public Location generate(TTYPE param) {
            int res = offset;
            offset -= param.getSize();
            localOffset -= param.getSize();

            return new Location(Location.LocationType.STACKFRAME, res);
        }

        public int getLocalOffset() {
            return localOffset;
        }

        public VariableLocator getChild() {
            return new X86VariableLocator(offset);
        }
    }

    public VariableLocator getVariableLocator() {
        return new X86VariableLocator(0);
    }

    // converts a location to its representation in asm
    public String genLocation(Location loc) {
        if(loc.getType() == Location.LocationType.REGISTER) {
            return registerNames[loc.getOffset()];
        }

        return "" + loc.getOffset();
    }

    public Code genFunction(TFUNCTION function, Code code) {
        code.prependAsm("mov ebp, esp");
        code.prependAsm("push ebp");
        code.prependAsm("f_" + function.getName() + ":");
        code.appendAsm("mov esp, ebp");
        code.appendAsm("pop ebp");
        code.appendAsm("ret");
        return code;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        int num_cond = getLabelNum();
        falseBloc.appendAsm("jmp cond_end_" + num_cond);
        trueBloc.prependAsm("cond_then_" + num_cond + ":");
        trueBloc.appendAsm("cond_end_" + num_cond + ":");

        x86Code c = (x86Code) condition;
        c.appendAsm("test " + c.resultRegisterName() + ", " + c.resultRegisterName());
        c.appendAsm("jeq cond_then_" + num_cond);
        c.appendAsm(falseBloc.getAsm());
        c.appendAsm(trueBloc.getAsm());
        return c;
    }

    public Code genLoop(Code condition, Code Bloc) {
        return new Code("");
    }

    public Code genReturn(Code returnVal_, TFUNCTION fun) {
        x86Code returnVal = (x86Code) returnVal_;

        if(! returnVal.resultRegisterName().equals("eax")) {
            returnVal.appendAsm("mov eax, " + returnVal.resultRegisterName());
        }

        return returnVal;
    }

    public Code genAffectation(Code address_, Code affectedVal_, TTYPE type) {
        x86Code address = (x86Code) address_;
        x86Code affectedVal = (x86Code) affectedVal_;

        if(address.getResultRegister() == affectedVal.getResultRegister()) {
            address.setResultRegister(affectedVal.getResultRegister() + 1);
            address.appendAsm("mov " + address.resultRegisterName() + ", " + affectedVal.resultRegisterName());
        }
        address.appendAsm(affectedVal.getAsm());
        address.appendAsm("mov [" + address.resultRegisterName() + "], " + affectedVal.resultRegisterName());
        address.setResultRegister(-1);
        return address;
    }

    public Code genBinary(Code leftOperand_, Code rightOperand_, String operator) {
        x86Code leftOperand = (x86Code) leftOperand_;
        x86Code rightOperand = (x86Code) rightOperand_;
        if(rightOperand.getResultRegister() == leftOperand.getResultRegister()) {
            rightOperand.setResultRegister(leftOperand.getResultRegister() + 1);
            rightOperand.appendAsm("mov " + rightOperand.resultRegisterName() + ", " + leftOperand.resultRegisterName());
        }
        leftOperand.prependAsm(rightOperand.getAsm());

        switch(operator) {
            case "+":
                leftOperand.appendAsm("add " + leftOperand.resultRegisterName() + ", " + rightOperand.resultRegisterName());
                break;
            case "-":
                leftOperand.appendAsm("sub " + leftOperand.resultRegisterName() + ", " + rightOperand.resultRegisterName());
                break;
            case "*":
                leftOperand.appendAsm("mul " + leftOperand.resultRegisterName() + ", " + rightOperand.resultRegisterName());
                break;
            case "/":
                leftOperand.appendAsm("div " + leftOperand.resultRegisterName() + ", " + rightOperand.resultRegisterName());
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
        return castedCode;
    }

    public Code genCall(String ident, Code arguments_) {
        x86Code arguments = (x86Code) arguments_;

        arguments.appendAsm("call f_" + ident);
        arguments.setResultRegister(0);
        return arguments;
    }

    // declare a variable
    public Code genDecl(INFOVAR info) {
        return new x86Code("sub esp, " + info.getType().getSize());
    }

    // declare a variable with an initial value
    public Code genDecl(INFOVAR info, Code value_) {
        x86Code value = (x86Code) value_;
        value.appendAsm("push " + value.resultRegisterName());
        value.setResultRegister(-1);
        return value;
    }

    public Code genAcces(Code pointerCode, TTYPE pointedType) {
        x86Code c = (x86Code) pointerCode;
        pointerCode.appendAsm("mov " + c.resultRegisterName() + ", [" + c.resultRegisterName() + "]");
        return c;
    }

    public Code genBloc(Code instsCode , VariableLocator vloc) {
        X86VariableLocator vl = (X86VariableLocator) vloc;
        if(vl.getLocalOffset() != 0)
        {
            instsCode.appendAsm("sub esp, " + vl.getLocalOffset());
        }
        return instsCode;
    }

    public Code genVariable(INFOVAR i) {
        assert(i.getLocation().getType() == Location.LocationType.STACKFRAME);
        x86Code c;
        if(i.getLocation().getOffset() < 0)
            c = new x86Code("lea eax, [ebp - " + (-i.getLocation().getOffset()) + "]", 0);
        else
            c = new x86Code("lea eax, [ebp + " + i.getLocation().getOffset() + "]", 0);
        c.setIsAddress(false);
        c.setAddress(i.getLocation().getOffset());
        return c;
    }

    public Code genInt(String cst) {
        return new x86Code("mov eax, " + cst, 0);
    }

    public Code genString(String txt) {
        return new Code("");
    }

    public Code genNull() {
        return new x86Code("mov eax, 0", 0);
    }

    public Code genBool(int b) {
        return new x86Code("mov eax, " + b, 0);
    }

    public Code genChar(String c) {
        return new x86Code("mov eax, " + c, 0);
    }
}
