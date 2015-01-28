package moc.cg;

import java.util.ArrayDeque;

import moc.st.INFOVAR;
import moc.type.TBOOL;
import moc.type.TFUNCTION;
import moc.type.TTYPE;
import moc.type.TVOID;

/**
 * The CRAPS machine and its generation functions
 */
public class MCRAPS extends AbstractMachine {

    private int stringOffset = 0;

    public static final String[] registerNames = {
        "%r1", "%r2", "%r3", "%r4", "%r5", "%r6", "%r7", "%r8", "%r9"
    };

    private Allocator allocator = new Allocator();

    private class Allocator extends ArrayDeque<Location> {
        private static final long serialVersionUID = 1L;

        /* returns a new unused location */
        public Location get() {
            for(int i = 0; i < registerNames.length; i++) {
                Location l = new Location(Location.LocationType.REGISTER, i);
                if(! this.contains(l))
                    return l;
            }

            throw new RuntimeException("No more unused registers !");
        }

        public boolean containsRegister(int i) {
            return contains(new Location(Location.LocationType.REGISTER, i));
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("[");

            for(Location l : this) {
                if(sb.length() > 1)
                    sb.append(", ");

                sb.append(registerNames[l.getOffset()]);
            }

            sb.append("]");
            return sb.toString();
        }
    }

    public MCRAPS() {
        initCode = "section .data\n";
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

    public ParametersLocator getParametersLocator() {
        return new DefaultParametersLocator(4);
    }

    public class SPARCVariableLocator extends DefaultVariableLocator {
        public SPARCVariableLocator(int offset) {
            super(offset);
        }

        public Location generate(TTYPE param) {
            offset -= param.getSize();
            localOffset -= param.getSize();

            return new Location(Location.LocationType.STACKFRAME, offset);
        }

        public VariableLocator getChild() {
            return new SPARCVariableLocator(offset);
        }
    }

    public VariableLocator getVariableLocator() {
        return new SPARCVariableLocator(0);
    }

    public String genLocation(Location l) {
        if(l.getType() == Location.LocationType.REGISTER) {
            return registerNames[l.getOffset()];
        }
        else if(l.getType() == Location.LocationType.STACKFRAME) {
            if(l.getOffset() >= 0)
                return "[%r29 + " + l.getOffset() + "]";
            else
                return "[%r29 - " + (-l.getOffset()) + "]";
        }
        else {
            return "" + l.getOffset();
        }
    }

    private Code genFunction(String label, String comment, Code code) {
        Code c = new Code(code.getAsm());
        c.prependAsm(label + ":");
        c.prependAsm("\n" + genComment("### " + comment + " #############"));

        c.appendAsm(label + "_end:");
        c.appendAsm("ret");
        return c;
    }

    public Code genFunction(TFUNCTION function, Code code) {
        return genFunction("f_" + function.getName(), function.toString(), code);
    }

    public Code genConditional(Code c, Code trueBloc, Code falseBloc) {
        Location l = allocator.pop();
        c = genVal(c, l, new TBOOL(1));

        int num_cond = getLabelNum();
        falseBloc.appendAsm("ba cond_end_" + num_cond);
        trueBloc.prependAsm("cond_then_" + num_cond + ":");
        trueBloc.appendAsm("cond_end_" + num_cond + ":");

        c.prependAsm(genComment("if condition :"));
        c.appendAsm("cmp " + genLocation(l) + ", %r0");
        c.appendAsm("bne cond_then_" + num_cond);
        c.appendAsm(falseBloc.getAsm());
        c.appendAsm(trueBloc.getAsm());
        return c;
    }

    public Code genLoop(Code condition, Code c) {
        Location l = allocator.pop();
        condition = genVal(condition, l, new TBOOL(1));

        int num = getLabelNum();
        condition.appendAsm("cmp " + genLocation(l) + ", %r0");
        condition.appendAsm("be end_loop_" + num);
        c.prependAsm(condition.getAsm());
        c.prependAsm(genComment("loop condition :"));
        c.prependAsm("loop_" + num + ":");
        c.appendAsm("ba loop_" + num);
        c.appendAsm("end_loop_" + num + ":");
        return c;
    }

    private Code genReturn(String label, TTYPE returnType, Code returnVal) {
        Location l = allocator.pop();
        returnVal = genVal(returnVal, l, returnType);

        if(! genLocation(l).equals("%r1")) {
            returnVal.appendAsm("mov " + genLocation(l) + ", %r1");
        }

        returnVal.appendAsm("ba " + label + "_end");
        return returnVal;
    }

    public Code genFunctionReturn(Code returnVal, TFUNCTION function) {
        return genReturn("f_" + function.getName(), function.getReturnType(), returnVal);
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        Location v = allocator.pop();
        Location a = allocator.pop();

        affectedVal = genVal(affectedVal, v, type);
        affectedVal.prependAsm(genComment("affected value :"));

        if (address.getLocation() != null) {
            affectedVal.appendAsm(genMovRegToMem(genLocation(address.getLocation()), v, type.getSize()));
            return affectedVal;
        } else {
            address.prependAsm(genComment("affected address :"));
            address.appendAsm(affectedVal.getAsm());
            address.appendAsm(genMovRegToMem("[" + genLocation(a) + "]", v, type.getSize()));
            return address;
        }
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        // Warning : Don't forget the order is important here...
        Location rightLocation = allocator.pop();
        Location leftLocation = allocator.pop();

        leftOperand = genVal(leftOperand, leftLocation, leftType);
        rightOperand = genVal(rightOperand, rightLocation, rightType);

        leftOperand.prependAsm(genComment("left operand :"));
        rightOperand.prependAsm(genComment("right operand :"));

        leftOperand.appendAsm(rightOperand.getAsm());

        switch(operator) {
            case "+":
                leftOperand.appendAsm("add " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                break;
            case "-":
                leftOperand.appendAsm("sub " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                break;
            case "*":
            case "/":
            case "%":
                // mul and div work only with one parameter...
                leftOperand.appendAsm(genComment("operator " + operator));

                // save registers
                if (allocator.containsRegister(0)) leftOperand.appendAsm("push eax");
                if (allocator.containsRegister(3)) leftOperand.appendAsm("push edx");

                if (rightLocation.getOffset() == 3) {
                    // get an unused register
                    allocator.push(rightLocation);
                    allocator.push(leftLocation);
                    Location newLocation = allocator.get();
                    allocator.pop();
                    allocator.pop();

                    // use newLocation as right operand
                    leftOperand.appendAsm("mov " + genLocation(newLocation) + ", edx");
                    rightLocation = newLocation;
                }

                // prepare
                if (leftLocation.getOffset() != 0) leftOperand.appendAsm("mov eax, " + genLocation(leftLocation));
                leftOperand.appendAsm("mov edx, 0"); // high part of the operand

                if (operator.equals("*")) leftOperand.appendAsm("mul " + genLocation(rightLocation));
                else leftOperand.appendAsm("div " + genLocation(rightLocation));

                if (!operator.equals("%") && leftLocation.getOffset() != 0) leftOperand.appendAsm("mov " + genLocation(leftLocation) + ", eax");
                if (operator.equals("%") && leftLocation.getOffset() != 3) leftOperand.appendAsm("mov " + genLocation(leftLocation) + ", edx");

                // restore registers
                if (allocator.containsRegister(3)) leftOperand.appendAsm("pop edx");
                if (allocator.containsRegister(0)) leftOperand.appendAsm("pop eax");
                break;
            case "&&":
                // "and" in x86 is a bitwise operator.
                // 0b10 and 0b100 = 0, too bad..
                leftOperand.appendAsm(genComment("operator " + operator));

                // leftLocation > 0
                leftOperand.appendAsm("cmp " + genLocation(leftLocation) + ", 0");
                leftOperand.appendAsm("pushfd");
                leftOperand.appendAsm("pop " + genLocation(leftLocation));
                leftOperand.appendAsm("not " + genLocation(leftLocation));
                leftOperand.appendAsm("and " + genLocation(leftLocation) + ", 0x40");

                // rightLocation > 0
                leftOperand.appendAsm("cmp " + genLocation(rightLocation) + ", 0");
                leftOperand.appendAsm("pushfd");
                leftOperand.appendAsm("pop " + genLocation(rightLocation));
                leftOperand.appendAsm("not " + genLocation(rightLocation));
                leftOperand.appendAsm("and " + genLocation(rightLocation) + ", 0x40");

                leftOperand.appendAsm("and " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                break;
            case "||":
                leftOperand.appendAsm("or " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                break;
            case "==":
            case "!=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("cmp " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                leftOperand.appendAsm("pushfd");
                leftOperand.appendAsm("pop " + genLocation(leftLocation));

                if (operator.equals("!=")) leftOperand.appendAsm("not " + genLocation(leftLocation));
                leftOperand.appendAsm("and " + genLocation(leftLocation) + ", 0x40");
                break;
            case "<":
            case ">=":
            case ">":
            case "<=":
                leftOperand.appendAsm(genComment("operator " + operator));

                if (operator.equals("<") || operator.equals(">="))
                    leftOperand.appendAsm("cmp " + genLocation(leftLocation) + ", " + genLocation(rightLocation));
                else
                    leftOperand.appendAsm("cmp " + genLocation(rightLocation) + ", " + genLocation(leftLocation));

                leftOperand.appendAsm("pushfd");
                leftOperand.appendAsm("pop " + genLocation(leftLocation));

                if (operator.equals(">=") || operator.equals("<=")) leftOperand.appendAsm("not " + genLocation(leftLocation));
                leftOperand.appendAsm("and " + genLocation(leftLocation) + ", 0x80");
                break;
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }

        allocator.push(leftLocation);
        return leftOperand;
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        Location l = allocator.pop();
        operand = genVal(operand, l, type);

        switch(operator) {
            case "-":
                operand.appendAsm("neg " + genLocation(l));
                break;
            case "!":
                operand.appendAsm(genComment("operator !"));
                operand.appendAsm("cmp " + genLocation(l) + ", 0");
                operand.appendAsm("pushfd");
                operand.appendAsm("pop " + genLocation(l));
                operand.appendAsm("and " + genLocation(l) + ", 0x40");
                break;
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }

        allocator.push(l);
        return operand;
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        Location l = allocator.pop();
        castedCode = genVal(castedCode, l, oldType);
        allocator.push(l);
        return castedCode;
    }

    private Code genCall(String label, TTYPE returnType, int parametersSize, Code arguments) {
        Location l = allocator.get();

        arguments.prependAsm(genComment("push parameters :"));
        arguments.appendAsm("call " + label);

        if (!(returnType instanceof TVOID) && l.getOffset() != 0) {
            arguments.appendAsm("mov %r1, " + genLocation(l));
        }

        if (parametersSize > 0) {
            arguments.appendAsm("add %r29, " + parametersSize + ", %r29 " + genComment("removing parameters"));
        }

        // push registers
        for (Location loc : allocator) {
            arguments.prependAsm("push " + genLocation(loc));
            arguments.appendAsm("pop " + genLocation(loc));
        }

        // need to be done here and not before (not to disturb the backup registers)
        if(!(returnType instanceof TVOID))
            allocator.push(l);

        return arguments;
    }

    public Code genFunctionCall(TFUNCTION f, Code arguments) {
        return genCall("f_" + f.getName(),
                        f.getReturnType(),
                        f.getParameterTypes().getSize(),
                        arguments);
    }

    // declare a variable
    public Code genDecl(INFOVAR info) {
        return new Code("sub %r29, " + info.getType().getSize() + ", %r29");
    }

    // declare a variable with an initial value
    public Code genDecl(INFOVAR info, Code value) {
        Location l = allocator.pop();
        value = genVal(value, l, info.getType());
        value.appendAsm(genPush(l, info.getType().getSize()));
        return value;
    }

    // expression instruction
    public Code genInst(TTYPE type, Code value) {
        if(!(type instanceof TVOID))
            allocator.pop();

        return value;
    }

    public Code genArg(Code e, TTYPE type) {
        Location l = allocator.pop();
        e = genVal(e, l, type);
        e.appendAsm(genPush(l, type.getSize()));
        return e;
    }

    public Code genAcces(Code pointerCode, TTYPE pointedType) {
        Location l = allocator.pop();
        Location d = allocator.get();

        if(pointerCode.getIsAddress()) {
            pointerCode.appendAsm(genMovMemToReg(genLocation(d), "[" + genLocation(l) + "]", pointedType.getSize()));
        }

        pointerCode.setIsAddress(true);
        pointerCode.setLocation(null);
        allocator.push(d);
        return pointerCode;
    }

    public Code genBloc(Code instsCode , VariableLocator vloc) {
        SPARCVariableLocator vl = (SPARCVariableLocator) vloc;

        if(vl.getLocalOffset() != 0) {
            instsCode.appendAsm("add %r29, " + (-vl.getLocalOffset()) + ", %r29 " + genComment("removing local variables"));
        }

        return instsCode;
    }

    public Code genVariable(INFOVAR i) {
        assert(i.getLocation().getType() == Location.LocationType.STACKFRAME);
        Location l = allocator.get();
        allocator.push(l);

        Code c = new Code(genMovMemToReg(genLocation(l), genLocation(i.getLocation()), i.getType().getSize()));
        c.setIsAddress(false);
        c.setLocation(i.getLocation());

        return c;
    }

    public Code genInt(String cst) {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("set " + cst + ", " + genLocation(l));
    }

    public Code genString(String txt) {
        int offset = stringOffset;
        stringOffset++;

        txt = txt.replace("\\n", "\",10,\"").replace("\\r", "\",13,\"").replace("\\t", "\",9,\""); // fix for nasm..
        initCode += "\tstr_" + offset + ": db " + txt + ", 0\n";
        Location l = allocator.get();
        allocator.push(l);
        return new Code("mov str_" + offset + ", " + genLocation(l));
    }

    public Code genNull() {
        return genBool(0);
    }

    public Code genBool(int b) {
        Location l = allocator.get();
        allocator.push(l);
        return new Code("set " + b + ", " + genLocation(l));
    }

    public Code genChar(String c) {
        Location l = allocator.get();
        allocator.push(l);

        if(c.equals("'\\0'"))
            return new Code("set 0, " + genLocation(l));
        else if(c.equals("'\\n'"))
            return new Code("set 10, " + genLocation(l));
        else if(c.equals("'\\r'"))
            return new Code("set 13, " + genLocation(l));
        else if(c.equals("'\\t'"))
            return new Code("set 9, " + genLocation(l));
        else
            return new Code("set " + c + ", " + genLocation(l));
    }

    /**
     * Ensures the Code gives a value
     */
    private Code genVal(Code operand, Location l, TTYPE type) {
        if(operand.getLocation() != null) {
            return new Code(genMovMemToReg(genLocation(l), genLocation(operand.getLocation()), type.getSize()));
        }
        else if(operand.getIsAddress()) {
            operand.appendAsm(genMovMemToReg(genLocation(l), "[" + genLocation(l) + "]", type.getSize()));
            operand.setIsAddress(false);
        }

        return operand;
    }

    /**
     * Generate a push
     */
    private String genPush(Location operand, int size) {
        if (size == 4) {
            return "push " + genLocation(operand);
        }
        else {
            String c = "sub %r29, " + size + ", %r29\n";
            c += genMovRegToMem("[%r29]", operand, size);
            return c;
        }
    }

    /**
     * Generate a mov from registers to memory
     */
    private String genMovRegToMem(String left, Location right, int size) {
        assert(right.getType() == Location.LocationType.REGISTER);

        return "st " + genLocation(right) + ", " + left;
        /*
        switch(size) {
            case 4: {
                return "mov " + left + ", " + genLocation(right);
            }
            case 2: {
                String[] registerNames = {"ax", "bx", "cx", "dx", "si", "di"};
                return "mov " + left + ", " + registerNames[right.getOffset()];
            }
            case 1: {
                String[] registerNames = {"al", "bl", "cl", "dl", "sil", "dil"};
                return "mov " + left + ", " + registerNames[right.getOffset()];
            }
            default:
                throw new RuntimeException("Invalid size: " + size);
        }
        */
    }

    /**
     * Generate a mov from memory to a register
     */
    private String genMovMemToReg(String left, String right, int size) {
        return "ld " + right + ", " + left;
        /*
        switch(size) {
            case 4:
                return "mov " + left + ", " + right;
            case 2:
                return "movzx " + left + ", WORD " + right;
            case 1:
                return "movzx " + left + ", BYTE " + right;
            default:
                throw new RuntimeException("Invalid size: " + size);
        }
        */
    }
}
