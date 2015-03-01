package moc.cg;

import java.util.ArrayDeque;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import moc.st.ST;
import moc.st.INFO;
import moc.st.INFOFUN;
import moc.st.INFOVAR;
import moc.type.TBOOL;
import moc.type.TFUNCTION;
import moc.type.TTYPE;
import moc.type.TVOID;
import moc.type.TARRAY;
import moc.type.TSTRUCT;
import moc.type.FIELD;
import moc.type.TPOINTER;
import moc.type.TINTEGER;

/**
 * The CRAPS machine and its generation functions
 */
public class MCRAPS extends AbstractMachine {

    public static final String[] registerNames = {
        "%r1", "%r2", "%r3", "%r4", "%r5", "%r6", "%r7", "%r8", "%r9", "%r10",
        "%r11", "%r12", "%r13", "%r14", "%r15", "%r16", "%r17", "%r18", "%r19"
    };

    public static final HashMap<Long, String> fixedRegisters;
    static {
        fixedRegisters = new HashMap<>();
        fixedRegisters.put(0L, "%r0");
        fixedRegisters.put(1L, "%r20");
    }

    private long staticOffset = 0L;

    private Allocator allocator = new Allocator();

    /**
     * An allocator is a stack of used registers
     */
    static private class Allocator extends ArrayDeque<Location> {
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

        public Location getFreeRegFromEnd() {
            for(int i = registerNames.length - 1; i >= 1; i--) {
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

                sb.append(registerNames[(int) l.getOffset()]);
            }

            sb.append("]");
            return sb.toString();
        }
    }

    public MCRAPS() {
        // We will put static data at the end of the code
        endCode = "\n" + genComment("### static section #############") + "\n";
        endCode += "static:\n";
    }

    /**
     * Called before writeCode
     */
    protected void prepareWrite(EntityList entities) {
        if(staticOffset == 0L) {
            endCode += ".word 0\n";
        }

        /*
         * Fix duplicate labels by inserting a null operation.
         * It's ugly, but it works.
         */
        for(EntityCode entity : entities.getList()) {
            String asm = entity.getAsm();

            StringBuffer sb = new StringBuffer();
            Pattern pattern = Pattern.compile("(^|\n)(\\w+):\\s*\n\\s*(\\w+):");
            Matcher matcher = pattern.matcher(asm);

            while (matcher.find()) {
                String firstLabel = matcher.group(2);
                String secondLabel = matcher.group(3);
                matcher.appendReplacement(sb, "\n" + firstLabel + ":\n"
                                            + "nop // fix duplicate labels\n"
                                            + secondLabel + ":");
            }
            matcher.appendTail(sb);
            entity.setAsm(sb.toString());
        }

        super.prepareWrite(entities);
    }

    public String getName() {
        return "CRAPS";
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

    private TINTEGER getIntType() { return new TINTEGER(getIntSize()); }

    private TBOOL getBoolType() { return new TBOOL(getBoolSize()); }

    private TPOINTER getPointerType(TTYPE pointedType) {
        return new TPOINTER(pointedType, getPointerSize());
    }

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

    public class CRAPSVariableLocator extends DefaultVariableLocator {
        public CRAPSVariableLocator(int offset) {
            super(offset);
        }

        public Location generate(TTYPE param, boolean register) {
            if(register) {
                Location reg = allocator.getFreeRegFromEnd(); // because %r1 could be used to return a value
                allocator.push(reg);
                return reg;
            }
            else {
                offset -= param.getSize();
                localOffset -= param.getSize();

                return new Location(Location.LocationType.STACKFRAME, offset);
            }
        }

        public VariableLocator getChild() {
            return new CRAPSVariableLocator(offset);
        }
    }

    /**
     * Returns a VariableLocator, responsible for managing the location of
     * local variables
     */
    public VariableLocator getVariableLocator() {
        /* it will generate [%fp - size of first parameter,
         *                   %fp - size of the first two parameters, …] */
        return new CRAPSVariableLocator(0);
    }

    /**
     * Converts a location to its representation in assembler
     */
    public String genLocation(Location l) {
        if(l.isRegister()) {
            return registerNames[(int) l.getOffset()];
        }
        else if(l.isStackFrame()) {
            if(l.getOffset() >= 0)
                return "[%fp + " + l.getOffset() + "]";
            else
                return "[%fp - " + (-l.getOffset()) + "]";
        }
        else {
            return "[%r24 + " + l.getOffset() + "]";
        }
    }

    public FunctionCode genFunction(TFUNCTION function, Code code, boolean exported) {
        String label = "f_" + function.getName();
        String comment = function.toString();

        code.prependAsm("mov %sp, %fp");
        code.prependAsm("push %fp");
        code.prependAsm(label + ":");
        code.prependAsm("\n" + genComment("### " + comment + " #############"));

        code.appendAsm(label + "_end:");
        code.appendAsm("mov %fp, %sp");
        code.appendAsm("pop %fp");
        code.appendAsm("ret");

        return new FunctionCode(function.getName(), code.getAsm(), exported);
    }

    public Code genCondition(Code conditionCode) {
        if(conditionCode.hasValue() && !conditionCode.isAddress()) {
            return conditionCode;
        }
        else {
            CodeValue cond = forceAsm(conditionCode, getBoolType());
            forceValue(cond, getBoolType());
            cond.code.appendAsm("cmp " + genLocation(cond.reg) + ", %r0");
            return cond.code;
        }
    }

    public Code genConditional(Code conditionCode, Code trueCode, Code falseCode) {
        if(conditionCode.hasValue() && !conditionCode.isAddress()) {
            if(conditionCode.getValue() == 0) { // if(false)
                return falseCode;
            }
            else { // if(true)
                return trueCode;
            }
        }
        else {
            Code code = conditionCode;

            int num = getLabelNum();
            code.prependAsm(genComment("if condition :"));

            if(falseCode.getAsm().trim().isEmpty()) {
                code.appendAsm("be cond_end_" + num);
                code.appendAsm(trueCode.getAsm());
                code.appendAsm("cond_end_" + num + ":");
            }
            else {
                falseCode.appendAsm("ba cond_end_" + num);
                trueCode.prependAsm("cond_then_" + num + ":");
                trueCode.appendAsm("cond_end_" + num + ":");

                code.appendAsm("bne cond_then_" + num);
                code.appendAsm(falseCode.getAsm());
                code.appendAsm(trueCode.getAsm());
            }

            return code;
        }
    }

    public Code genWhileLoop(Code conditionCode, Code body) {
        if(conditionCode.hasValue() && !conditionCode.isAddress()) {
            if(conditionCode.getValue() == 0) { // while(false)
                return new Code("");
            }
            else { // while(true)
                int num = loopLabelStack.peek();
                body.prependAsm("loop_" + num + ":");
                body.prependAsm(genComment("while true loop :"));
                body.appendAsm("ba loop_" + num);
                body.appendAsm("end_loop_" + num + ":"); // needed by genBreak()
                return body;
            }
        }
        else {
            int num = loopLabelStack.peek();
            conditionCode.appendAsm("be end_loop_" + num);

            body.prependAsm(conditionCode.getAsm());
            body.prependAsm(genComment("loop condition :"));
            body.prependAsm("loop_" + num + ":");

            body.appendAsm("ba loop_" + num);
            body.appendAsm("end_loop_" + num + ":");
            return body;
        }
    }

    public Code genForLoop(Code init, Code conditionCode, Code incr, Code body) {
        if(conditionCode.hasValue() && !conditionCode.isAddress()) {
            if(conditionCode.getValue() == 0) { // for(; false; )
                return init;
            }
            else { // for(; true; )
                int num = loopLabelStack.peek();
                body.prependAsm("loop_" + num + ":");
                body.prependAsm(genComment("for true condition :"));
                body.prependAsm(init.getAsm());

                body.appendAsm(incr.getAsm());
                body.appendAsm("ba loop_" + num);
                body.appendAsm("end_loop_" + num + ":"); // needed by genBreak()
                return body;
            }
        }
        else {
            int num = loopLabelStack.peek();
            conditionCode.appendAsm("be end_loop_" + num);

            body.prependAsm(conditionCode.getAsm());
            body.prependAsm(genComment("loop condition :"));
            body.prependAsm("loop_" + num + ":");
            body.prependAsm(init.getAsm());

            body.appendAsm(incr.getAsm());
            body.appendAsm("ba loop_" + num);
            body.appendAsm("end_loop_" + num + ":");
            return body;
        }
    }

    public Code genFunctionReturn(Code returnCode, TTYPE type, TFUNCTION fun) {
        Location r1 = new Location(Location.LocationType.REGISTER, 0);
        Code code;

        if(returnCode.hasValue() && !returnCode.isAddress()) {
            code = new Code(genSet(returnCode.getValue(), r1));
        }
        else if(returnCode.hasLocation() && !returnCode.isAddress()) {
            Location loc = returnCode.getLocation();

            if(loc.isRegister()) {
                code = new Code(genMovRegToReg(loc, r1));
            }
            else {
                code = new Code(genMovMemToReg(genLocation(loc), r1));
            }
        }
        else {
            CodeValue ret = forceAsm(returnCode, type);
            forceValue(ret, type);
            code = ret.code;
            code.appendAsm(genMovRegToReg(ret.reg, r1));
        }

        code.appendAsm("ba f_" + fun.getName() + "_end");
        return code;
    }

    public Code genBreak() {
        int num = loopLabelStack.peek();
        return new Code("ba end_loop_" + num + " // break");
    }

    public Code genContinue() {
        int num = loopLabelStack.peek();
        return new Code("ba loop_" + num + " // continue");
    }

    public Code genAffectation(Code addrCode, Code valueCode, TTYPE addrType, TTYPE valueType) {
        boolean valueInFixedRegisters = valueCode.hasValue() && !valueCode.isAddress()
                && fixedRegisters.containsKey(valueCode.getValue());
        CodeValue value = forceAsm(valueCode, valueType);
        forceValue(value, valueType);
        Code code;
        value.code.prependAsm(genComment("affected value :"));

        if(addrCode.hasLocation() && !addrCode.isAddress()) {
            Location loc = addrCode.getLocation();

            if(valueInFixedRegisters) {
                if(loc.isRegister()) {
                    code = new Code("mov " + fixedRegisters.get(valueCode.getValue()) + ", " + genLocation(loc));
                }
                else {
                    code = new Code("st " + fixedRegisters.get(valueCode.getValue())
                            + ", " + genLocation(loc));
                }
            }
            else {
                code = value.code;

                if(loc.isRegister()) {
                    code.appendAsm(genMovRegToReg(value.reg, loc));
                }
                else {
                    code.appendAsm(genMovRegToMem(value.reg, genLocation(loc)));
                }
            }
        }
        else {
            assert(addrCode.isAddress());

            // in that case, we will need a new register
            if(!addrCode.hasAsm())
                allocator.push(value.reg);

            CodeValue addr = forceAsm(addrCode, addrType);

            if(!addrCode.hasAsm())
                allocator.pop();

            addr.code.prependAsm(genComment("affected address :"));

            if(valueInFixedRegisters) {
                code = addr.code;
                code.appendAsm("st " + fixedRegisters.get(valueCode.getValue()) + ", [" + genLocation(addr.reg) + "]");
            }
            else {
                if(addrCode.hasAsm()) {
                    code = addr.code;
                    code.appendAsm(value.code.getAsm());
                }
                else {
                    code = value.code;
                    code.appendAsm(addr.code.getAsm());
                }

                code.appendAsm(genMovRegToMem(value.reg, "[" + genLocation(addr.reg) + "]"));
            }
        }

        return code;
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        if(leftOperand.hasValue() && !leftOperand.isAddress()) {
            long left = leftOperand.getValue();

            if(rightOperand.hasValue() && !rightOperand.isAddress()) {
                long right = rightOperand.getValue();

                switch(operator) {
                    case "+": return Code.fromValue(left + right);
                    case "-": return Code.fromValue(left - right);
                    case "*": return Code.fromValue(left * right);
                    case "/": return Code.fromValue(left / right);
                    case "%": return Code.fromValue(left % right);
                    case "&": return Code.fromValue(left & right);
                    case "|": return Code.fromValue(left | right);
                    case "^": return Code.fromValue(left ^ right);
                    case "<<": return Code.fromValue(left << right);
                    case ">>": return Code.fromValue(left >> right);
                    case "&&": return Code.fromValue((left != 0 && right != 0) ? 1 : 0);
                    case "||": return Code.fromValue((left != 0 || right != 0) ? 1 : 0);
                    case "==": return Code.fromValue(left == right ? 1 : 0);
                    case "!=": return Code.fromValue(left != right ? 1 : 0);
                    case "<": return Code.fromValue(left < right ? 1 : 0);
                    case ">=": return Code.fromValue(left >= right ? 1 : 0);
                    case ">": return Code.fromValue(left > right ? 1 : 0);
                    case "<=": return Code.fromValue(left <= right ? 1 : 0);
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }
            }
            else {
                CodeValue right = forceAsm(rightOperand, rightType);
                forceValue(right, rightType);
                Code code = right.code;
                Location reg = allocator.getFreeReg();
                Location tmp;
                allocator.push(reg);

                code.prependAsm(genComment("right operand :"));
                code.appendAsm(genComment(left + " " + operator + " " + genLocation(right.reg)));

                switch(operator) {
                    case "+":
                        code.appendAsm(genImmediateOperation("add", right.reg, left, reg));
                        break;
                    case "-":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("sub", tmp, right.reg, reg));
                        break;
                    case "*":
                        code.appendAsm(genImmediateOperation("umulcc", right.reg, left, reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm(genImmediateOperation("and", right.reg, left, reg));
                        break;
                    case "|":
                        code.appendAsm(genImmediateOperation("or", right.reg, left, reg));
                        break;
                    case "^":
                        code.appendAsm(genImmediateOperation("xor", right.reg, left, reg));
                        break;
                    case "<<":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("sll", tmp, right.reg, reg));
                        break;
                    case ">>":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("srl", tmp, right.reg, reg));
                        break;
                    case "&&":
                        if(left == 0) {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(0);
                        }
                        else {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(right.reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        break;
                    case "||":
                        if(left == 0) {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(right.reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        else {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(1);
                        }
                        break;
                    case "==":
                    case "!=":
                        code.appendAsm(genImmediateOperation("subcc", right.reg, left, reg));
                        code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                        code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                        if (operator.equals("!="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case "<":
                    case ">=":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm("subcc " + genLocation(tmp) + ", " + genLocation(right.reg) + ", %r0");
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals(">="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case ">":
                    case "<=":
                        code.appendAsm(genImmediateOperation("subcc", right.reg, left, reg));
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals("<="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }

                return code;
            }
        }
        else { // !leftOperand.hasValue()
            if(rightOperand.hasValue() && !rightOperand.isAddress()) {
                long right = rightOperand.getValue();

                CodeValue left = forceAsm(leftOperand, leftType);
                forceValue(left, leftType);
                Code code = left.code;
                Location reg = allocator.getFreeReg();
                Location tmp;
                allocator.push(reg);

                code.prependAsm(genComment("left operand :"));
                code.appendAsm(genComment(genLocation(left.reg) + " " + operator + " " + right));

                switch(operator) {
                    case "+":
                        code.appendAsm(genImmediateOperation("add", left.reg, right, reg));
                        break;
                    case "-":
                        code.appendAsm(genImmediateOperation("sub", left.reg, right, reg));
                        break;
                    case "*":
                        code.appendAsm(genImmediateOperation("umulcc", left.reg, right, reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm(genImmediateOperation("and", left.reg, right, reg));
                        break;
                    case "|":
                        code.appendAsm(genImmediateOperation("or", left.reg, right, reg));
                        break;
                    case "^":
                        code.appendAsm(genImmediateOperation("xor", left.reg, right, reg));
                        break;
                    case "<<":
                        code.appendAsm(genImmediateOperation("sll", left.reg, right, reg));
                        break;
                    case ">>":
                        code.appendAsm(genImmediateOperation("srl", left.reg, right, reg));
                        break;
                    case "&&":
                        if(right == 0) {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(0);
                        }
                        else {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(left.reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        break;
                    case "||":
                        if(right == 0) {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(left.reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        else {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(1);
                        }
                        break;
                    case "==":
                    case "!=":
                        code.appendAsm(genImmediateOperation("subcc", left.reg, right, reg));
                        code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                        code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                        if (operator.equals("!="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case "<":
                    case ">=":
                        code.appendAsm(genImmediateOperation("subcc", left.reg, right, reg));
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals(">="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case ">":
                    case "<=":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(right, tmp));
                        code.appendAsm("subcc " + genLocation(tmp) + ", " + genLocation(left.reg) + ", %r0");
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals("<="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }

                return code;
            }
            else {
                CodeValue right = forceAsm(rightOperand, rightType);

                // in that case, we will need a new register
                if(!leftOperand.hasAsm()) {
                    allocator.push(right.reg);
                }

                CodeValue left = forceAsm(leftOperand, leftType);

                if(!leftOperand.hasAsm()) {
                    allocator.pop();
                }

                allocator.push(right.reg);
                forceValue(left, leftType);
                allocator.pop();

                allocator.push(left.reg);
                forceValue(right, rightType);
                allocator.pop();

                left.code.prependAsm(genComment("left operand :"));
                right.code.prependAsm(genComment("right operand :"));

                Code code;
                Location reg = allocator.getFreeReg();
                Location tmp;
                allocator.push(reg);

                if(leftOperand.hasAsm()) {
                    code = left.code;
                    code.appendAsm(right.code.getAsm());
                }
                else {
                    code = right.code;
                    code.appendAsm(left.code.getAsm());
                }

                code.appendAsm(genComment(genLocation(left.reg) + " " + operator + " " + genLocation(right.reg)));

                switch(operator) {
                    case "+":
                        code.appendAsm(genOperation("add", left.reg, right.reg, reg));
                        break;
                    case "-":
                        code.appendAsm(genOperation("sub", left.reg, right.reg, reg));
                        break;
                    case "*":
                        code.appendAsm(genOperation("umulcc", left.reg, right.reg, reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm(genOperation("and", left.reg, right.reg, reg));
                        break;
                    case "|":
                        code.appendAsm(genOperation("or", left.reg, right.reg, reg));
                        break;
                    case "^":
                        code.appendAsm(genOperation("xor", left.reg, right.reg, reg));
                        break;
                    case "<<":
                        code.appendAsm(genOperation("sll", left.reg, right.reg, reg));
                        break;
                    case ">>":
                        code.appendAsm(genOperation("srl", left.reg, right.reg, reg));
                        break;
                    case "&&":
                        // "and" in craps is a bitwise operator.
                        // 0b10 and 0b100 = 0, too bad..

                        // we need another register
                        allocator.push(right.reg);
                        tmp = allocator.getFreeReg();
                        allocator.pop();

                        // generates left.reg = (left.reg != 0)
                        code.appendAsm("cmp %r0, " + genLocation(left.reg));
                        code.appendAsm("and %r25, 16, " + genLocation(tmp)); // 16 -> mask for C

                        // generates right.reg = (right.reg != 0)
                        code.appendAsm("cmp %r0, " + genLocation(right.reg));
                        code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C

                        code.appendAsm(genOperation("and", tmp, reg, reg));
                        code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        break;
                    case "||":
                        code.appendAsm(genOperation("or", left.reg, right.reg, reg));
                        break;
                    case "==":
                    case "!=":
                        code.appendAsm("subcc " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", %r0");
                        code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                        code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                        if (operator.equals("!="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case "<":
                    case ">=":
                        code.appendAsm("subcc " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", %r0");
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals(">="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case ">":
                    case "<=":
                        code.appendAsm("subcc " + genLocation(right.reg) + ", " + genLocation(left.reg) + ", %r0");
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals("<="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }

                return code;
            }
        }
    }

    public Code genUnary(Code operand, TTYPE type, String operator) {
        if(operand.hasValue() && !operand.isAddress()) {
            switch(operator) {
                case "-": return Code.fromValue(- operand.getValue());
                case "!": return Code.fromValue(operand.getValue() != 0 ? 0 : 1);
                case "~": return Code.fromValue(~ operand.getValue());
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }
        }
        else {
            CodeValue c = forceAsm(operand, type);
            forceValue(c, type);

            Location reg = allocator.getFreeReg();
            allocator.push(reg);

            c.code.appendAsm(genComment("operator " + operator));

            switch(operator) {
                case "-":
                    c.code.appendAsm(genMovRegToReg(c.reg, reg));
                    c.code.appendAsm("negcc " + genLocation(reg));
                    break;
                case "!":
                    c.code.appendAsm("cmp " + genLocation(c.reg) + ", %r0");
                    c.code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                    c.code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                    break;
                case "~":
                    c.code.appendAsm("xor " + genLocation(c.reg) + ", -1, " + genLocation(reg));
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }

            return c.code;
        }
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        if(!castedCode.isAddress()
                && (castedCode.hasValue() || castedCode.hasLocation())
                && !(oldType instanceof TARRAY || oldType instanceof TSTRUCT)) {
            return castedCode;
        }
        else {
            CodeValue c = forceAsm(castedCode, oldType);
            forceValue(c, oldType);
            allocator.push(c.reg);
            return c.code;
        }
    }

    @Override
    protected Code genFunctionCallImpl(TFUNCTION fun, Code arguments) {
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
        if(info.getLocation().isRegister()) {
            return new Code("");
        }
        else {
            return new Code("sub %sp, " + info.getType().getSize() + ", %sp");
        }
    }

    /**
     * Declare a variable with an initial value
     *
     * @param value The code for the initial value
     */
    public Code genDecl(INFOVAR info, Code operand, TTYPE type) {
        if(info.getLocation().isRegister()) {
            return genAffectation(Code.fromLocation(info.getLocation()),
                                  operand, info.getType(), type);
        }
        else {
            return genArg(operand, type);
        }
    }

    /**
     * Declare a global variable
     */
    public GlobalCode genDeclGlobal(INFOVAR info) {
        return new GlobalCode("");
    }

    /*
     * Generate a new location for a global variable
     */
    public Location genGlobalLocation(TTYPE type) {
        long offset = staticOffset;
        staticOffset += type.getSize();

        endCode += genBytes(Collections.nCopies(type.getSize(), 0)) + "\n";
        return new Location(Location.LocationType.ABSOLUTE, offset);
    }

    /**
     * Expression instruction
     *
     * @param value The code for the expression
     */
    public Code genInst(TTYPE type, Code operand) {
        if(operand.hasValue() || operand.hasLocation()) {
            return new Code("");
        }
        else {
            // if it's an expression of type void, no register are used
            if(!(type instanceof TVOID))
                allocator.pop();

            return operand;
        }
    }

    public Code genArg(Code operand, TTYPE type) {
        if(operand.hasValue() && !operand.isAddress()
                && fixedRegisters.containsKey(operand.getValue())) {
            return new Code("push " + fixedRegisters.get(operand.getValue()));
        }
        else {
            CodeValue c = forceAsm(operand, type);
            forceValue(c, type);
            c.code.appendAsm(genPush(c.reg));
            return c.code;
        }
    }

    public Code genAccess(Code pointerCode, TTYPE pointedType) {
        Code code;

        if(pointerCode.isAddress()) {
            CodeValue c = forceAsm(pointerCode, getPointerType(pointedType));
            forceValue(c, getPointerType(pointedType));
            code = c.code;
            allocator.push(c.reg);
        }
        else {
            code = pointerCode;
        }

        code.setAddress(true);
        return code;
    }

    public Code genStackArrayAccess(INFOVAR info, Code posCode) {
        TARRAY type = (TARRAY) info.getType();
        Code code;

        if(posCode.hasValue() && !posCode.isAddress()) {
            long offset = posCode.getValue() * type.getElementsType().getSize();
            Location loc = info.getLocation();
            code = Code.fromLocation(new Location(loc.getType(),
                                                  loc.getOffset() + offset));
        }
        else {
            CodeValue c = forceAsm(posCode, getIntType());
            forceValue(c, getIntType());
            code = c.code;

            Location reg = allocator.getFreeReg();
            allocator.push(reg);

            code.appendAsm(genComment("stack array access :"));
            if(type.getElementsType().getSize() != 1) {
                code.appendAsm("umulcc " + genLocation(c.reg) + ", " + type.getElementsType().getSize() + ", " + genLocation(reg));
                c.reg = reg;
            }

            if(info.getLocation().isStackFrame()) {
                code.appendAsm("add " + genLocation(c.reg) + ", %fp, " + genLocation(reg));
                code.appendAsm("sub " + genLocation(reg) + ", " + (-info.getLocation().getOffset()) + ", " + genLocation(reg));
            }
            else { // info.getLocation().isAbsolute()
                code.appendAsm("add " + genLocation(c.reg) + ", %r24, " + genLocation(reg));
                code.appendAsm("add " + genLocation(reg) + ", " + info.getLocation().getOffset() + ", " + genLocation(reg));
            }

            code.setAddress(true);
        }

        return code;
    }

    public Code genPointerArrayAccess(INFOVAR info, Code posCode) {
        TPOINTER type = (TPOINTER) info.getType();
        Code code;

        if(posCode.hasValue() && !posCode.isAddress()) {
            long offset = posCode.getValue() * type.getType().getSize();
            Location reg = allocator.getFreeReg();
            allocator.push(reg);

            if(info.getLocation().isRegister()) {
                if(offset > 0) {
                    code = new Code("add " + genLocation(info.getLocation()) + ", " + offset + ", " + genLocation(reg));
                }
                else {
                    allocator.pop();
                    allocator.push(info.getLocation());
                    code = new Code("");
                }
            }
            else {
                code = new Code(genMovMemToReg(genLocation(info.getLocation()), reg)); // pointer value

                if(offset > 0)
                    code.appendAsm("add " + genLocation(reg) + ", " + offset + ", " + genLocation(reg));
            }
        }
        else {
            CodeValue c = forceAsm(posCode, getIntType());
            forceValue(c, getIntType());
            code = c.code;

            Location reg = allocator.getFreeReg();
            allocator.push(reg);

            if(info.getLocation().isRegister()) {
                if(type.getType().getSize() != 1) {
                    code.appendAsm("umulcc " + genLocation(c.reg) + ", " + type.getType().getSize() + ", " + genLocation(reg));
                    c.reg = reg;
                }

                code.appendAsm("add " + genLocation(info.getLocation()) + ", " + genLocation(c.reg) + ", " + genLocation(reg));
            }
            else {
                allocator.push(c.reg);
                Location pointerReg = allocator.getFreeReg();
                allocator.pop();

                code.appendAsm(genMovMemToReg(genLocation(info.getLocation()), pointerReg)); // pointer value

                if(type.getType().getSize() != 1) {
                    code.appendAsm("umulcc " + genLocation(c.reg) + ", " + type.getType().getSize() + ", " + genLocation(reg));
                    c.reg = reg;
                }

                code.appendAsm("add " + genLocation(pointerReg) + ", " + genLocation(c.reg) + ", " + genLocation(reg));
            }
        }

        code.prependAsm(genComment("pointer array access :"));
        code.setAddress(true);
        return code;
    }

    public Code genFieldAccess(TSTRUCT struct, FIELD field, Code operand) {
        int offset = struct.getFieldOffset(field.getName());
        Code code;

        if(operand.hasLocation() && !operand.isAddress()) {
            Location loc = operand.getLocation();
            code = Code.fromLocation(new Location(loc.getType(),
                                                  loc.getOffset() + offset));
        }
        else {
            assert(operand.isAddress());
            CodeValue c = forceAsm(operand, struct);
            code = c.code;

            if(offset > 0)
                code.appendAsm("add " + genLocation(c.reg) + ", " + offset + ", " + genLocation(c.reg));

            allocator.push(c.reg);
            code.setAddress(true);

            // special case for arrays and structures
            if(field.getType() instanceof TARRAY || field.getType() instanceof TSTRUCT) {
                code.setAddress(false);
            }
        }

        return code;
    }

    public Code genPointerFieldAccess(TSTRUCT struct, FIELD field, Code operand) {
        int offset = struct.getFieldOffset(field.getName());
        CodeValue c = forceAsm(operand, getPointerType(struct));
        forceValue(c, getPointerType(struct));

        c.code.appendAsm(genComment("field access :"));
        if(offset > 0) {
            Location reg = allocator.getFreeReg();
            c.code.appendAsm("add " + genLocation(c.reg) + ", " + offset + ", " + genLocation(reg));
            allocator.push(reg);
        }
        else {
            allocator.push(c.reg);
        }

        c.code.setAddress(true);

        // special case for arrays and structures
        if(field.getType() instanceof TARRAY || field.getType() instanceof TSTRUCT) {
            c.code.setAddress(false);
        }

        return c.code;
    }

    public Code genBlock(Code instsCode, VariableLocator vloc, ST symbolsTable) {
        CRAPSVariableLocator vl = (CRAPSVariableLocator) vloc;

        boolean isFunction = symbolsTable.getMother() != null
                                && symbolsTable.getMother().getMother() != null
                                && symbolsTable.getMother().getMother().getMother() == null;
        if(vl.getLocalOffset() != 0 && !isFunction) {
            instsCode.appendAsm("add %sp, " + (-vl.getLocalOffset()) + ", %sp " + genComment("removing local variables"));
        }

        // free register variables
        for(INFO info : symbolsTable.values()) {
            if(info instanceof INFOVAR) {
                INFOVAR var = (INFOVAR) info;
                if(var.getLocation().isRegister()) {
                    allocator.remove(var.getLocation());
                }
            }
        }

        return instsCode;
    }

    public Code genVariable(INFOVAR i) {
        return Code.fromLocation(i.getLocation());
    }

    public Code genVariable(String name, INFOFUN i) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("set f_" + name + ", " + genLocation(reg));
    }

    public Code genAddress(INFOVAR i) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);

        if(i.getLocation().isStackFrame()) {
            return new Code("sub %fp, " + (-i.getLocation().getOffset()) + ", " + genLocation(reg));
        }
        else { // i.getLocation().isAbsolute()
            return new Code("add %r24, " + i.getLocation().getOffset() + ", " + genLocation(reg));
        }
    }

    public Code genInt(String cst) {
        return Code.fromValue(getLongFromString(cst));
    }

    public Code genString(String txt) {
        List<Integer> bytes = getArrayFromString(txt);
        long offset = staticOffset;
        staticOffset += bytes.size();

        endCode += genBytes(getArrayFromString(txt)) + "\n";
        Location reg = allocator.getFreeReg();
        allocator.push(reg);
        return new Code("add %r24, " + offset + ", " + genLocation(reg));
    }

    public Code genNull() {
        return Code.fromValue(0);
    }

    public Code genBool(int b) {
        return Code.fromValue(b);
    }

    public Code genChar(String c) {
        return Code.fromValue(getCharFromString(c));
    }

    public boolean fitInRegister(TTYPE type) {
        return type.getSize() == 1 &&
            !(type instanceof TARRAY) &
            !(type instanceof TSTRUCT);
    }

    /**
     * Generates the assembler for a lazy code
     *
     * warning: the register is not pushed on the allocator, and could be allocated
     */
    private CodeValue forceAsm(Code operand, TTYPE type) {
        Code code;
        Location reg;

        if(operand.hasValue()) {
            reg = allocator.getFreeReg();
            code = new Code(genSet(operand.getValue(), reg));
        }
        else if(operand.hasLocation() && operand.getLocation().isRegister()) {
            reg = operand.getLocation();
            code = new Code("");
        }
        else if(operand.hasLocation()) {
            reg = allocator.getFreeReg();
            Location loc = operand.getLocation();

            // special case for arrays and structures
            if((type instanceof TARRAY || type instanceof TSTRUCT)
                    && !operand.isAddress()) {
                if(loc.isStackFrame()) {
                    code = new Code("sub %fp, " + (-loc.getOffset()) + ", " + genLocation(reg));
                }
                else { // loc.isAbsolute()
                    code = new Code("add %r24, " + loc.getOffset() + ", " + genLocation(reg));
                }
            }
            else {
                code = new Code(genMovMemToReg(genLocation(loc), reg));
            }
        }
        else {
            reg = allocator.pop();
            code = new Code(operand.getAsm());
        }

        code.setAddress(operand.isAddress());
        return new CodeValue(code, reg);
    }

    /**
     * Ensures the Code gives a value (dereference)
     *
     * @param cv The code and register used
     * @param type The type of the value
     *
     * warning: the register is not pushed on the allocator, and could be allocated
     */
    private void forceValue(CodeValue cv, TTYPE type) {
        assert(!cv.code.hasValue() && !cv.code.hasLocation());

        if(cv.code.isAddress()) {
            Location reg = allocator.getFreeReg();
            cv.code.appendAsm(genMovMemToReg("[" + genLocation(cv.reg) + "]", reg));
            cv.reg = reg;
            cv.code.setAddress(false);
        }
    }

    /**
     * A code that puts a value in a register
     */
    static private class CodeValue {
        public Code code;
        public Location reg;

        public CodeValue(Code code, Location reg) {
            assert(!code.hasValue() && !code.hasLocation());
            this.code = code;
            this.reg = reg;
        }
    }

    /**
     * Generate a push
     */
    private String genPush(Location reg) {
        assert(reg.isRegister());
        return "push " + genLocation(reg);
    }

    /**
     * Generate a mov from registers to memory
     */
    private String genMovRegToMem(Location reg, String mem) {
        assert(reg.isRegister());
        return "st " + genLocation(reg) + ", " + mem;
    }

    /**
     * Generate a mov from memory to a register
     */
    private String genMovMemToReg(String mem, Location reg) {
        assert(reg.isRegister());
        return "ld " + mem + ", " + genLocation(reg);
    }

    /**
     * Generate a mov from register to register
     */
    private String genMovRegToReg(Location src, Location dest) {
        assert(src.isRegister());
        assert(dest.isRegister());

        if(src.equals(dest)) {
            return "";
        }
        else {
            return "mov " + genLocation(src) + ", " + genLocation(dest);
        }
    }

    /**
     * Generate a set
     */
    private String genSet(long value, Location reg) {
        assert(reg.isRegister());

        if(value >= -4096 && value <= 4095)
            return "setq " + value + ", " + genLocation(reg);
        else if(value >= 0 && value % 256L == 0L) // equivalent to value & 0xff == 0
            return "sethi " + (value >> 8) + ", " + genLocation(reg);
        else
            return "set " + value + ", " + genLocation(reg);
    }

    /**
     * Generate an operation between a register and an immediate value
     */
    private String genImmediateOperation(String op, Location left, long right, Location result) {
        assert(left.isRegister());
        assert(result.isRegister());

        // useless operation
        if(result.equals(left) && right == 0L
                && (op.equals("add") || op.equals("sub") || op.equals("or")
                    || op.equals("sll") || op.equals("srl"))) {
            return "";
        }

        if(right >= -4096 && right <= 4095)
            return op + " " + genLocation(left) + ", " + right + ", " + genLocation(result);
        else {
            Location tmp = allocator.getFreeReg();
            return genSet(right, tmp) + "\n" + genOperation(op, left, tmp, result);
        }
    }

    /**
     * Generate an operation between a register and a register
     */
    private String genOperation(String op, Location left, Location right, Location result) {
        assert(left.isRegister());
        assert(right.isRegister());
        assert(result.isRegister());

        return op + " " + genLocation(left) + ", " + genLocation(right)
                  + ", " + genLocation(result);
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
