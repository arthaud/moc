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
        initCode = "";
    }

    public String getName() {
        return "SPARC";
    }

    public String getSuffix() {
        return "asm";
    }

    @Override
    public String asmVariablePattern() {
        return "\\$([a-z][_0-9A-Za-z]*)";
    }

    /* warning: the memory is word addressed! */
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

    @Override
    public String genComment(String comm) {
        return "// " + comm;
    }

    public ParametersLocator getParametersLocator() {
        return new DefaultParametersLocator(2);
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
                return "[%fp + " + l.getOffset() + "]";
            else
                return "[%fp - " + (-l.getOffset()) + "]";
        }
        else {
            return "" + l.getOffset();
        }
    }

    private Code genFunction(String label, String comment, Code code) {
        Code c = new Code(code.getAsm());
        c.prependAsm("mov %sp, %fp");
        c.prependAsm("push %fp");
        c.prependAsm(label + ":");
        c.prependAsm("\n" + genComment("### " + comment + " #############"));

        c.appendAsm(label + "_end:");
        c.appendAsm("mov %fp, %sp");
        c.appendAsm("pop %fp");
        c.appendAsm("ret");
        return c;
    }

    public Code genFunction(TFUNCTION function, Code code) {
        return genFunction("f_" + function.getName(), function.toString(), code);
    }

    public Code genConditional(Code c, Code trueBloc, Code falseBloc) {
        Location l = allocator.pop();
        c = genVal(c, l, new TBOOL(getBoolSize()));

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
                leftOperand.appendAsm("add " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", " + genLocation(leftLocation));
                break;
            case "-":
                leftOperand.appendAsm("sub " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", " + genLocation(leftLocation));
                break;
            case "*":
                leftOperand.appendAsm("umulcc " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", " + genLocation(leftLocation));
                break;
            case "/":
            case "%":
                throw new UnsupportedOperationException("CRAPS");
            case "&&":
                // "and" in craps is a bitwise operator.
                // 0b10 and 0b100 = 0, too bad..
                leftOperand.appendAsm(genComment("operator " + operator));

                // leftLocation > 0
                leftOperand.appendAsm("subcc %r0, " + genLocation(leftLocation) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftLocation)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftLocation) + ", 7, " + genLocation(leftLocation)); // normalization

                // rightLocation > 0
                leftOperand.appendAsm("subcc %r0, " + genLocation(rightLocation) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(rightLocation)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(rightLocation) + ", 7, " + genLocation(rightLocation)); // normalization

                leftOperand.appendAsm("and " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", " + genLocation(leftLocation));
                break;
            case "||":
                leftOperand.appendAsm("or " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", " + genLocation(leftLocation));
                break;
            case "==":
            case "!=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", %r0");
                leftOperand.appendAsm("and %r25, 64, " + genLocation(leftLocation)); // 64 -> mask for Z
                leftOperand.appendAsm("srl " + genLocation(leftLocation) + ", 6, " + genLocation(leftLocation)); // normalization
                if (operator.equals("!="))
                    leftOperand.appendAsm("xor " + genLocation(leftLocation) + ", 1, " + genLocation(leftLocation));
                break;
            case "<":
            case ">=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(leftLocation) + ", " + genLocation(rightLocation) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftLocation)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftLocation) + ", 7, " + genLocation(leftLocation)); // normalization
                if(operator.equals(">="))
                    leftOperand.appendAsm("xor " + genLocation(leftLocation) + ", 1, " + genLocation(leftLocation));
                break;
            case ">":
            case "<=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(rightLocation) + ", " + genLocation(leftLocation) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftLocation)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftLocation) + ", 7, " + genLocation(leftLocation)); // normalization
                if(operator.equals("<="))
                    leftOperand.appendAsm("xor " + genLocation(leftLocation) + ", 1, " + genLocation(leftLocation));
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
                operand.appendAsm("negcc " + genLocation(l));
                break;
            case "!":
                operand.appendAsm(genComment("operator " + operator));
                operand.appendAsm("subcc " + genLocation(l) + ", %r0, %r0");
                operand.appendAsm("and %r25, 64, " + genLocation(l)); // 64 -> mask for Z
                operand.appendAsm("srl " + genLocation(l) + ", 6, " + genLocation(l)); // normalization
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
        arguments.appendAsm("push %r28");
        arguments.appendAsm("call " + label);
        arguments.appendAsm("pop %r28");

        if (!(returnType instanceof TVOID) && l.getOffset() != 0) {
            arguments.appendAsm("mov %r1, " + genLocation(l));
        }

        if (parametersSize > 0) {
            arguments.appendAsm("add %sp, " + parametersSize + ", %sp " + genComment("removing parameters"));
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
        return new Code("sub %sp, " + info.getType().getSize() + ", %sp");
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
            instsCode.appendAsm("add %sp, " + (-vl.getLocalOffset()) + ", %sp " + genComment("removing local variables"));
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
        return "push " + genLocation(operand);
    }

    /**
     * Generate a mov from registers to memory
     */
    private String genMovRegToMem(String left, Location right, int size) {
        assert(right.getType() == Location.LocationType.REGISTER);

        return "st " + genLocation(right) + ", " + left;
    }

    /**
     * Generate a mov from memory to a register
     */
    private String genMovMemToReg(String left, String right, int size) {
        return "ld " + right + ", " + left;
    }
}
