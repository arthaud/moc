package moc.cg;

import moc.type.TTYPE;
import moc.type.TFUNCTION;
import moc.st.INFOVAR;
import java.util.ArrayDeque;

/**
 * The TAM machine and its generation functions
 */
public class Mx86 extends AbstractMachine {

    public static final String[] registerNames = {
        "eax", "ebx", "ecx", "edx", "esi", "edi"
    };
    
    /* Convert a Location to a string representing it in x86 */
    private static String x86Location(Location l)
    {
        if(l.getType() == Location.LocationType.REGISTER)
        {
            return registerNames[l.getOffset()];
        }
        else if(l.getType() == Location.LocationType.STACKFRAME)
        {
            if(l.getOffset() >= 0)
                return "[ebp + " + l.getOffset() + "]";
            else
                return "[ebp - " + (-l.getOffset()) + "]";
        }
        else
        {
            return "" + l.getOffset();
        }
    }
    
    private class Allocator extends ArrayDeque<Location>
    {
        /* returns a new unused location */
        public Location get()
        {
            for(int i = 0; i < registerNames.length; i++)
            {
                Location l = new Location(Location.LocationType.REGISTER, i);
                if(! this.contains(l))
                    return l;
            }
            return null;
        }
    }
    
    Allocator allocator = new Allocator();
    
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

    public Code genConditional(Code c, Code trueBloc, Code falseBloc) {
        int num_cond = getLabelNum();
        falseBloc.appendAsm("jmp cond_end_" + num_cond);
        trueBloc.prependAsm("cond_then_" + num_cond + ":");
        trueBloc.appendAsm("cond_end_" + num_cond + ":");
        
        Location l = allocator.pop();

        c.appendAsm("test " + x86Location(l) + ", " + x86Location(l));
        c.appendAsm("jeq cond_then_" + num_cond);
        c.appendAsm(falseBloc.getAsm());
        c.appendAsm(trueBloc.getAsm());
        return c;
    }

    public Code genLoop(Code condition, Code Bloc) {
        return new Code("");
    }

    public Code genReturn(Code returnVal, TFUNCTION fun) {
        Location l = allocator.pop();

        if(! x86Location(l).equals("eax")) {
            returnVal.appendAsm("mov eax, " + x86Location(l));
        }

        return returnVal;
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        Location a = allocator.pop();
        Location v = allocator.pop();
        
        address.appendAsm(affectedVal.getAsm());
        address.appendAsm("mov [" + x86Location(a) + "], " + x86Location(v));
        return address;
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        Location leftLocation = allocator.pop();
        Location rightLocation = allocator.pop();
        
        leftOperand.appendAsm(rightOperand.getAsm());

        switch(operator) {
            case "+":
                leftOperand.appendAsm("add " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "-":
                leftOperand.appendAsm("sub " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "*":
                leftOperand.appendAsm("mul " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "/":
                leftOperand.appendAsm("div " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "&&":
                leftOperand.appendAsm("and " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "||":
                leftOperand.appendAsm("or " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "%":
                // TODO
                leftOperand.appendAsm("TODO:modulo " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                break;
            case "==":
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", zf");
                break;
            case "!=":
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", zf");
                leftOperand.appendAsm("neg " + x86Location(leftLocation));
                break;
            case ">":
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", sf");
                break;
            case "<=":
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", sf");
                leftOperand.appendAsm("neg " + x86Location(leftLocation));
                break;
            case ">=":
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", sf");
                leftOperand.appendAsm("or " + x86Location(leftLocation) + ", zf");
                break;
            case "<":
                // TODO
                leftOperand.appendAsm("cmp " + x86Location(leftLocation) + ", " + x86Location(rightLocation));
                leftOperand.appendAsm("mv " + x86Location(leftLocation) + ", sf");
                break;
            default:
                throw new RuntimeException("Unknown operator.");
        }

        allocator.push(leftLocation);
        return leftOperand;
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        Location l = allocator.pop();

        switch(operator) {
            case "-":
                operand.appendAsm("neg " + x86Location(l));
                break;
            default:
                throw new RuntimeException("Unknown operator.");
        }
        
        allocator.push(l);
        return operand;
    }

    public Code genCast(TTYPE type, Code castedCode) {
        return castedCode;
    }

    public Code genCall(String ident, Code arguments) {
        arguments.appendAsm("call f_" + ident);
        allocator.push(new Location(Location.LocationType.REGISTER, 0));
        return arguments;
    }

    // declare a variable
    public Code genDecl(INFOVAR info) {
        return new Code("sub esp, " + info.getType().getSize());
    }

    // declare a variable with an initial value
    public Code genDecl(INFOVAR info, Code value) {
        Location l = allocator.pop();
        value.appendAsm("push " + x86Location(l));
        return value;
    }

    // expression instruction
    public Code genInst(TTYPE type, Code value) {
        value.prependAsm("");
        return value;
    }
    
    public Code genArg(Code e, TTYPE type)
    {
        Location l = allocator.pop();
        e.appendAsm("push " + x86Location(l));
        return e;
    }

    public Code genAcces(Code pointerCode, TTYPE pointedType) {
        Location l = allocator.pop();
        Location d = allocator.get();
        pointerCode.appendAsm("mov " + x86Location(d) + ", [" + x86Location(l) + "]");
        allocator.push(d);
        return pointerCode;
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
        Location l = allocator.get();
        Code c = new Code("mov " + x86Location(l) + ", " + x86Location(i.getLocation()));
        allocator.push(l);
        c.setIsAddress(false);
        c.setAddress(i.getLocation().getOffset());
        return c;
    }

    public Code genInt(String cst) {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("mov " + x86Location(l) + ", " + cst);
    }

    public Code genString(String txt) {
        return new Code("");
    }

    public Code genNull() {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("mov " + x86Location(l) + ", 0");
    }

    public Code genBool(int b) {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("mov " + x86Location(l) + ", " + b);
    }

    public Code genChar(String c) {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("mov " + x86Location(l) + ", " + c);
    }
}
