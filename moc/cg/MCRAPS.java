package moc.cg;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Collections;

import moc.st.INFOVAR;
import moc.type.TBOOL;
import moc.type.TFUNCTION;
import moc.type.TTYPE;
import moc.type.TVOID;
import moc.type.TARRAY;
import moc.type.TPOINTER;

/**
 * The CRAPS machine and its generation functions
 */
public class MCRAPS extends AbstractMachine {

    public static final String[] registerNames = {
        "%r1", "%r2", "%r3", "%r4", "%r5", "%r6", "%r7", "%r8", "%r9"
    };

    private Allocator allocator = new Allocator();

    /**
     * An allocator is a stack of used registers
     */
    private class Allocator extends ArrayDeque<Location> {
        private static final long serialVersionUID = 1L;

        /**
         * Returns a new unused register
         */
        public Location getFreeReg() {
            for(int i = 0; i < registerNames.length; i++) {
                Location l = new Location(Location.LocationType.REGISTER, i);

                if(!this.contains(l))
                    return l;
            }

            throw new RuntimeException("No more unused registers !");
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
        // We will put static data at the end of the code
        endCode = "\n" + genComment("### static section #############") + "\n";
    }

    public String getName() {
        return "SPARC";
    }

    public String getSuffix() {
        return "asm";
    }

    @Override
    protected String asmVariablePattern() {
        return "\\$([a-z][_0-9A-Za-z]*)";
    }

    /* warning: the memory is word addressed! */
    public int getIntSize() { return 1; }

    public int getCharSize() { return 1; }

    public int getBoolSize() { return 1; }

    public int getPointerSize() { return 1; }

    @Override
    public String genComment(String comm) {
        return "// " + comm;
    }

    /**
     * Returns a ParametersLocator, responsible for managing the location of
     * parameters
     */
    public ParametersLocator getParametersLocator() {
        /* it will generate [%fp + 2, %fp + 2 + size of first parameter,
         *                   %fp + 2 + size of the first two parameters, …] */
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

    /**
     * Returns a VariableLocator, responsible for managing the location of
     * local variables
     */
    public VariableLocator getVariableLocator() {
        /* it will generate [%fp - size of first parameter,
         *                   %fp - size of the first two parameters, …] */
        return new SPARCVariableLocator(0);
    }

    /**
     * Converts a location to its representation in assembler
     */
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
            return "[glob_" + l.getOffset() + "]";
        }
    }

    public Code genFunction(TFUNCTION function, Code code) {
        Code c = new Code(code.getAsm());

        // preambule to manage %fp (in reverse order because of the prepend!)
        c.prependAsm("mov %sp, %fp");
        c.prependAsm("push %fp");
        c.prependAsm("f_" + function.getName() + ":");
        c.prependAsm("\n" + genComment("### " + function + " #############"));

        c.appendAsm("f_" + function.getName() + "_end:");
        c.appendAsm("mov %fp, %sp");
        c.appendAsm("pop %fp");
        c.appendAsm("ret");
        return c;
    }

    public Code genConditional(Code codeCond, Code trueBloc, Code falseBloc) {
        Location regCond = allocator.pop();
        Code c = forceValue(codeCond, regCond, new TBOOL(getBoolSize()));

        int num = getLabelNum();
        falseBloc.appendAsm("ba cond_end_" + num);
        trueBloc.prependAsm("cond_then_" + num + ":");
        trueBloc.appendAsm("cond_end_" + num + ":");

        c.prependAsm(genComment("if condition :"));
        c.appendAsm("cmp " + genLocation(regCond) + ", %r0");
        c.appendAsm("bne cond_then_" + num);
        c.appendAsm(falseBloc.getAsm());
        c.appendAsm(trueBloc.getAsm());
        return c;
    }

    public Code genLoop(Code condition, Code c) {
        Location regCond = allocator.pop();
        condition = forceValue(condition, regCond, new TBOOL(1));

        int num = getLabelNum();
        condition.appendAsm("cmp " + genLocation(regCond) + ", %r0");
        condition.appendAsm("be end_loop_" + num);

        c.prependAsm(condition.getAsm());
        c.prependAsm(genComment("loop condition :"));
        c.prependAsm("loop_" + num + ":");
        c.appendAsm("ba loop_" + num);
        c.appendAsm("end_loop_" + num + ":");
        return c;
    }

    public Code genFunctionReturn(Code returnVal, TFUNCTION function) {
        Location valueReg = allocator.pop();
        returnVal = forceValue(returnVal, valueReg, function.getReturnType());

        if(!genLocation(valueReg).equals("%r1")) {
            returnVal.appendAsm("mov " + genLocation(valueReg) + ", %r1");
        }

        returnVal.appendAsm("ba f_" + function.getName() + "_end");
        return returnVal;
    }

    public Code genAffectation(Code address, Code affectedVal, TTYPE type) {
        Location valueReg = allocator.pop();
        Location addrReg = allocator.pop();

        affectedVal = forceValue(affectedVal, valueReg, type);
        affectedVal.prependAsm(genComment("affected value :"));

        if (address.getLocation() != null) {
            affectedVal.appendAsm(genMovRegToMem(valueReg, genLocation(address.getLocation())));
            return affectedVal;
        } else {
            address.prependAsm(genComment("affected address :"));
            address.appendAsm(affectedVal.getAsm());
            address.appendAsm(genMovRegToMem(valueReg, "[" + genLocation(addrReg) + "]"));
            return address;
        }
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        // Warning : Don't forget the order is important here...
        Location rightReg = allocator.pop();
        Location leftReg = allocator.pop();

        leftOperand = forceValue(leftOperand, leftReg, leftType);
        rightOperand = forceValue(rightOperand, rightReg, rightType);

        leftOperand.prependAsm(genComment("left operand :"));
        rightOperand.prependAsm(genComment("right operand :"));

        // the result will be in leftReg
        leftOperand.appendAsm(rightOperand.getAsm());

        switch(operator) {
            case "+":
                leftOperand.appendAsm("add " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "-":
                leftOperand.appendAsm("sub " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "*":
                leftOperand.appendAsm("umulcc " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "/":
            case "%":
                throw new UnsupportedOperationException("CRAPS");
            case "&":
                leftOperand.appendAsm("and " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "|":
                leftOperand.appendAsm("or " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "&&":
                // "and" in craps is a bitwise operator.
                // 0b10 and 0b100 = 0, too bad..
                leftOperand.appendAsm(genComment("operator " + operator));

                // leftReg > 0
                leftOperand.appendAsm("subcc %r0, " + genLocation(leftReg) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftReg)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftReg) + ", 7, " + genLocation(leftReg)); // normalization

                // rightReg > 0
                leftOperand.appendAsm("subcc %r0, " + genLocation(rightReg) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(rightReg)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(rightReg) + ", 7, " + genLocation(rightReg)); // normalization

                leftOperand.appendAsm("and " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "||":
                leftOperand.appendAsm("or " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", " + genLocation(leftReg));
                break;
            case "==":
            case "!=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", %r0");
                leftOperand.appendAsm("and %r25, 64, " + genLocation(leftReg)); // 64 -> mask for Z
                leftOperand.appendAsm("srl " + genLocation(leftReg) + ", 6, " + genLocation(leftReg)); // normalization
                if (operator.equals("!="))
                    leftOperand.appendAsm("xor " + genLocation(leftReg) + ", 1, " + genLocation(leftReg));
                break;
            case "<":
            case ">=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(leftReg) + ", " + genLocation(rightReg) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftReg)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftReg) + ", 7, " + genLocation(leftReg)); // normalization
                if(operator.equals(">="))
                    leftOperand.appendAsm("xor " + genLocation(leftReg) + ", 1, " + genLocation(leftReg));
                break;
            case ">":
            case "<=":
                leftOperand.appendAsm(genComment("operator " + operator));
                leftOperand.appendAsm("subcc " + genLocation(rightReg) + ", " + genLocation(leftReg) + ", %r0");
                leftOperand.appendAsm("and %r25, 128, " + genLocation(leftReg)); // 128 -> mask for N (= left < right)
                leftOperand.appendAsm("srl " + genLocation(leftReg) + ", 7, " + genLocation(leftReg)); // normalization
                if(operator.equals("<="))
                    leftOperand.appendAsm("xor " + genLocation(leftReg) + ", 1, " + genLocation(leftReg));
                break;
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }

        allocator.push(leftReg);
        return leftOperand;
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        Location reg = allocator.pop();
        operand = forceValue(operand, reg, type);

        switch(operator) {
            case "-":
                operand.appendAsm("negcc " + genLocation(reg));
                break;
            case "!":
                operand.appendAsm(genComment("operator " + operator));
                operand.appendAsm("subcc " + genLocation(reg) + ", %r0, %r0");
                operand.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                operand.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                break;
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }

        allocator.push(reg);
        return operand;
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        Location reg = allocator.pop();
        castedCode = forceValue(castedCode, reg, oldType);
        allocator.push(reg);
        return castedCode;
    }

    public Code genFunctionCall(TFUNCTION fun, Code arguments) {
        Location resultReg = allocator.getFreeReg();

        arguments.prependAsm(genComment("push parameters :"));

        arguments.appendAsm("push %r28");
        arguments.appendAsm("call f_" + fun.getName());
        arguments.appendAsm("pop %r28");

        // put the result in resultReg (if it's not a void)
        if (!(fun.getReturnType() instanceof TVOID) && resultReg.getOffset() != 0) {
            arguments.appendAsm("mov %r1, " + genLocation(resultReg));
        }

        // remove parameters from the stack
        if (fun.getParameterTypes().getSize() > 0) {
            arguments.appendAsm("add %sp, " + fun.getParameterTypes().getSize() + ", %sp " + genComment("removing parameters"));
        }

        // calling convention : the caller saves all registers
        for (Location reg : allocator) {
            arguments.prependAsm("push " + genLocation(reg));
            arguments.appendAsm("pop " + genLocation(reg));
        }

        // need to be done here and not before (not to disturb the backup registers)
        if(!(fun.getReturnType() instanceof TVOID))
            allocator.push(resultReg);

        return arguments;
    }

    /**
     * Declare a variable
     */
    public Code genDecl(INFOVAR info) {
        return new Code("sub %sp, " + info.getType().getSize() + ", %sp");
    }

    /**
     * Declare a variable with an initial value
     *
     * @param value The code for the initial value
     */
    public Code genDecl(INFOVAR info, Code value, TTYPE type) {
        Location valueReg = allocator.pop();
        value = forceValue(value, valueReg, type);
        value.appendAsm(genPush(valueReg));
        return value;
    }

    /**
     * Declare a global variable
     */
    public Code genDeclGlobal(INFOVAR info) {
        endCode += "glob_" + info.getLocation().getOffset() + ": "
                + genBytes(Collections.nCopies(info.getSize(), 0)) + "\n";
        return new Code("");
    }

    /**
     * Expression instruction
     *
     * @param value The code for the expression
     */
    public Code genInst(TTYPE type, Code value) {
        // if it's an expression of type void, no register are used
        if(!(type instanceof TVOID))
            allocator.pop();

        return value;
    }

    public Code genArg(Code e, TTYPE type) {
        Location argReg = allocator.pop();
        e = forceValue(e, argReg, type);
        e.appendAsm(genPush(argReg));
        return e;
    }

    public Code genAccess(Code pointerCode, TTYPE pointedType) {
        Location pointerReg = allocator.pop();
        Location valueReg = allocator.getFreeReg();

        if(pointerCode.getIsAddress()) {
            pointerCode.appendAsm(genMovMemToReg("[" + genLocation(pointerReg) + "]", genLocation(valueReg)));
        }

        pointerCode.setIsAddress(true);
        pointerCode.setLocation(null);
        allocator.push(valueReg);
        return pointerCode;
    }

    public Code genStackArrayAccess(INFOVAR info, Code posCode) {
        TARRAY type = (TARRAY) info.getType();
        Location posReg = allocator.pop();

        posCode.appendAsm(genComment("stack array access :"));
        // compute %fp - tab.offset + index * elementsType.size in posReg

        if(type.getElementsType().getSize() != 1)
            posCode.appendAsm("umulcc " + genLocation(posReg) + ", " + type.getElementsType().getSize() + ", " + genLocation(posReg));

        posCode.appendAsm("add " + genLocation(posReg) + ", %fp, " + genLocation(posReg));
        posCode.appendAsm("sub " + genLocation(posReg) + ", " + (-info.getLocation().getOffset()) + ", " + genLocation(posReg));
        posCode.setIsAddress(true);
        posCode.setLocation(null);

        allocator.push(posReg);
        return posCode;
    }

    public Code genPointerArrayAccess(INFOVAR info, Code posCode) {
        TPOINTER type = (TPOINTER) info.getType();
        Location posReg = allocator.pop();
        allocator.push(posReg);
        Location arrayReg = allocator.getFreeReg();

        posCode.appendAsm(genComment("pointer array access :"));
        // compute %fp - tab.offset + index * elementsType.size in posReg

        if(type.getType().getSize() != 1)
            posCode.appendAsm("umulcc " + genLocation(posReg) + ", " + type.getType().getSize() + ", " + genLocation(posReg));

        posCode.appendAsm(genMovMemToReg(genLocation(info.getLocation()), genLocation(arrayReg)));
        posCode.appendAsm("add " + genLocation(arrayReg) + ", " + genLocation(posReg) + ", " + genLocation(posReg));
        posCode.setIsAddress(true);
        posCode.setLocation(null);

        return posCode;
    }

    public Code genBlock(Code instsCode, VariableLocator vloc) {
        SPARCVariableLocator vl = (SPARCVariableLocator) vloc;

        if(vl.getLocalOffset() != 0) {
            instsCode.appendAsm("add %sp, " + (-vl.getLocalOffset()) + ", %sp " + genComment("removing local variables"));
        }

        return instsCode;
    }

    public Code genVariable(INFOVAR i) {
        Location.LocationType locType = i.getLocation().getType();
        assert(locType == Location.LocationType.STACKFRAME ||
               locType == Location.LocationType.ABSOLUTE);

        Location reg = allocator.getFreeReg();
        allocator.push(reg);

        if(i.getType() instanceof TARRAY) { // special case for arrays : generate the address
            Code c = new Code("sub %fp, " + (-i.getLocation().getOffset()) + ", " + genLocation(reg));
            c.setIsAddress(false);
            return c;
        }

        Code c = new Code(genMovMemToReg(genLocation(i.getLocation()), genLocation(reg)));
        c.setIsAddress(false);
        c.setLocation(i.getLocation());

        return c;
    }

    public Code genInt(String cst) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("set " + cst + ", " + genLocation(reg));
    }

    private int stringOffset = 0;

    public Code genString(String txt) {
        int offset = stringOffset;
        stringOffset++;

        endCode += "str_" + offset + ": " + genBytes(getArrayFromString(txt)) + "\n";
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("set str_" + offset + ", " + genLocation(reg));
    }

    public Code genNull() {
        return genBool(0);
    }

    public Code genBool(int b) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("set " + b + ", " + genLocation(reg));
    }

    public Code genChar(String c) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("set " + getCharFromString(c) + ", " + genLocation(reg));
    }

    /**
     * Ensures the Code gives a value
     *
     * @param operand The code
     * @param valueReg Where to store the value
     * @param type The type of the value
     */
    private Code forceValue(Code operand, Location valueReg, TTYPE type) {
        if(type instanceof TARRAY) // special case for arrays : no dereferencing
            return operand;

        if(operand.getLocation() != null) {
            return new Code(genMovMemToReg(genLocation(operand.getLocation()), genLocation(valueReg)));
        }
        else if(operand.getIsAddress()) {
            operand.appendAsm(genMovMemToReg("[" + genLocation(valueReg) + "]", genLocation(valueReg)));
            operand.setIsAddress(false);
        }

        return operand;
    }

    /**
     * Generate a push
     */
    private String genPush(Location operand) {
        return "push " + genLocation(operand);
    }

    /**
     * Generate a mov from registers to memory
     */
    private String genMovRegToMem(Location reg, String mem) {
        assert(reg.getType() == Location.LocationType.REGISTER);
        return "st " + genLocation(reg) + ", " + mem;
    }

    /**
     * Generate a mov from memory to a register
     */
    private String genMovMemToReg(String mem, String reg) {
        return "ld " + mem + ", " + reg;
    }

    /**
     * Generate a list of bytes
     *
     * ex: genBytes({1, 2, 3}) = ".word 1, 2, 3"
     */
    private String genBytes(List<Integer> bytes) {
        StringBuilder s = new StringBuilder();

        for(Integer b : bytes) {
            if(s.length() == 0)
                s.append("" + b);
            else
                s.append(", " + b);
        }

        return ".word " + s;
    }
}
