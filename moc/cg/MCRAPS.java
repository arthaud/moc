package moc.cg;

import java.util.ArrayDeque;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

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
        "%r1", "%r2", "%r3", "%r4", "%r5", "%r6", "%r7", "%r8", "%r9"
    };

    public static final HashMap<Long, String> fixedRegisters;
    static {
        fixedRegisters = new HashMap<>();
        fixedRegisters.put(0L, "%r0");
        fixedRegisters.put(1L, "%r20");
    }

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

                sb.append(registerNames[(int) l.getOffset()]);
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

        public Location generate(TTYPE param) {
            offset -= param.getSize();
            localOffset -= param.getSize();

            return new Location(Location.LocationType.STACKFRAME, offset);
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
        if(l.getType() == Location.LocationType.REGISTER) {
            return registerNames[(int) l.getOffset()];
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

    public Code genConditional(Code conditionCode, Code trueCode, Code falseCode) {
        if(conditionCode.hasValue() && !conditionCode.getIsAddress()) {
            if(conditionCode.getValue() == 0) { // if(false)
                return falseCode;
            }
            else { // if(true)
                return trueCode;
            }
        }
        else {
            CodeValue cond = forceAsm(conditionCode, new TBOOL(getBoolSize()));
            forceValue(cond.code, cond.reg, new TBOOL(getBoolSize()));
            Code code = cond.code;

            int num = getLabelNum();
            falseCode.appendAsm("ba cond_end_" + num);
            trueCode.prependAsm("cond_then_" + num + ":");
            trueCode.appendAsm("cond_end_" + num + ":");

            code.prependAsm(genComment("if condition :"));
            code.appendAsm("cmp " + genLocation(cond.reg) + ", %r0");
            code.appendAsm("bne cond_then_" + num);
            code.appendAsm(falseCode.getAsm());
            code.appendAsm(trueCode.getAsm());
            return code;
        }
    }

    public Code genWhileLoop(Code conditionCode, Code body) {
        if(conditionCode.hasValue() && !conditionCode.getIsAddress()) {
            if(conditionCode.getValue() == 0) { // while(false)
                return new Code("");
            }
            else { // while(true)
                int num = getLabelNum();
                body.prependAsm("loop_" + num + ":");
                body.prependAsm(genComment("while true loop :"));
                body.appendAsm("ba loop_" + num);
                return body;
            }
        }
        else {
            CodeValue cond = forceAsm(conditionCode, new TBOOL(getBoolSize()));
            forceValue(cond.code, cond.reg, new TBOOL(getBoolSize()));

            int num = getLabelNum();
            cond.code.appendAsm("cmp " + genLocation(cond.reg) + ", %r0");
            cond.code.appendAsm("be end_loop_" + num);

            body.prependAsm(cond.code.getAsm());
            body.prependAsm(genComment("loop condition :"));
            body.prependAsm("loop_" + num + ":");

            body.appendAsm("ba loop_" + num);
            body.appendAsm("end_loop_" + num + ":");
            return body;
        }
    }

    public Code genForLoop(Code init, Code conditionCode, Code incr, Code body) {
        CodeValue cond = forceAsm(conditionCode, new TBOOL(getBoolSize()));
        forceValue(cond.code, cond.reg, new TBOOL(getBoolSize()));

        int num = getLabelNum();
        cond.code.appendAsm("cmp " + genLocation(cond.reg) + ", %r0");
        cond.code.appendAsm("be end_loop_" + num);

        body.prependAsm(cond.code.getAsm());
        body.prependAsm(genComment("loop condition :"));
        body.prependAsm("loop_" + num + ":");
        body.prependAsm(init.getAsm());

        body.appendAsm(incr.getAsm());
        body.appendAsm("ba loop_" + num);
        body.appendAsm("end_loop_" + num + ":");
        return body;
    }

    public Code genFunctionReturn(Code returnCode, TTYPE type, TFUNCTION fun) {
        Location r1 = new Location(Location.LocationType.REGISTER, 0);
        Code code;

        if(returnCode.hasValue() && !returnCode.getIsAddress()) {
            code = new Code(genSet(returnCode.getValue(), r1));
        }
        else if(returnCode.hasLocation() && !returnCode.getIsAddress()) {
            code = new Code(genMovMemToReg(genLocation(returnCode.getLocation()), r1));
        }
        else {
            CodeValue ret = forceAsm(returnCode, type);
            code = ret.code;
            forceValue(code, ret.reg, type);

            if(!ret.reg.equals(r1))
                code.appendAsm("mov " + genLocation(ret.reg) + ", " + genLocation(r1));
        }

        code.appendAsm("ba f_" + fun.getName() + "_end");
        return code;
    }

    public Code genAffectation(Code addrCode, Code valueCode, TTYPE addrType, TTYPE valueType) {
        CodeValue value = forceAsm(valueCode, valueType);
        forceValue(value.code, value.reg, valueType);
        Code code = value.code;
        code.prependAsm(genComment("affected value :"));

        if(addrCode.hasLocation() && !addrCode.getIsAddress()) {
            code.appendAsm(genMovRegToMem(value.reg, genLocation(addrCode.getLocation())));
        }
        else {
            assert(addrCode.getIsAddress());

            // in that case, we will need a new register
            if(addrCode.hasValue() || addrCode.hasLocation())
                allocator.push(value.reg);

            CodeValue addr = forceAsm(addrCode, addrType);

            if(addrCode.hasValue() || addrCode.hasLocation())
                allocator.pop();

            code = addr.code;
            code.prependAsm(genComment("affected address :"));
            code.appendAsm(value.code.getAsm());
            code.appendAsm(genMovRegToMem(value.reg, "[" + genLocation(addr.reg) + "]"));
        }

        return code;
    }

    public Code genBinary(Code leftOperand, TTYPE leftType, Code rightOperand, TTYPE rightType, String operator) {
        if(leftOperand.hasValue() && !leftOperand.getIsAddress()) {
            long left = leftOperand.getValue();

            if(rightOperand.hasValue() && !rightOperand.getIsAddress()) {
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
                forceValue(right.code, right.reg, rightType);
                Code code = right.code;
                Location reg = right.reg;
                Location tmp;
                allocator.push(reg);

                code.prependAsm(genComment("right operand :"));
                code.appendAsm(genComment(left + " " + operator + " " + genLocation(reg)));

                switch(operator) {
                    case "+":
                        code.appendAsm(genImmediateOperation("add", reg, left, reg));
                        break;
                    case "-":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("sub", tmp, reg, reg));
                        break;
                    case "*":
                        code.appendAsm(genImmediateOperation("umulcc", reg, left, reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm(genImmediateOperation("and", reg, left, reg));
                        break;
                    case "|":
                        code.appendAsm(genImmediateOperation("or", reg, left, reg));
                        break;
                    case "^":
                        code.appendAsm(genImmediateOperation("xor", reg, left, reg));
                        break;
                    case "<<":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("sll", tmp, reg, reg));
                        break;
                    case ">>":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm(genOperation("srl", tmp, reg, reg));
                        break;
                    case "&&":
                        if(left == 0) {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(0);
                        }
                        else {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        break;
                    case "||":
                        if(left == 0) {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(reg));
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
                        code.appendAsm(genImmediateOperation("subcc", reg, left, reg));
                        code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                        code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                        if (operator.equals("!="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case "<":
                    case ">=":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(left, tmp));
                        code.appendAsm("subcc " + genLocation(tmp) + ", " + genLocation(reg) + ", %r0");
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals(">="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case ">":
                    case "<=":
                        code.appendAsm(genImmediateOperation("subcc", reg, left, reg));
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
            if(rightOperand.hasValue() && !rightOperand.getIsAddress()) {
                long right = rightOperand.getValue();

                CodeValue left = forceAsm(leftOperand, leftType);
                forceValue(left.code, left.reg, leftType);
                Code code = left.code;
                Location reg = left.reg;
                Location tmp;
                allocator.push(reg);

                code.prependAsm(genComment("left operand :"));
                code.appendAsm(genComment(genLocation(reg) + " " + operator + " " + right));

                switch(operator) {
                    case "+":
                        code.appendAsm(genImmediateOperation("add", reg, right, reg));
                        break;
                    case "-":
                        code.appendAsm(genImmediateOperation("sub", reg, right, reg));
                        break;
                    case "*":
                        code.appendAsm(genImmediateOperation("umulcc", reg, right, reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm(genImmediateOperation("and", reg, right, reg));
                        break;
                    case "|":
                        code.appendAsm(genImmediateOperation("or", reg, right, reg));
                        break;
                    case "^":
                        code.appendAsm(genImmediateOperation("xor", reg, right, reg));
                        break;
                    case "<<":
                        code.appendAsm(genImmediateOperation("sll", reg, right, reg));
                        break;
                    case ">>":
                        code.appendAsm(genImmediateOperation("srl", reg, right, reg));
                        break;
                    case "&&":
                        if(right == 0) {
                            allocator.pop(); // reg is no more used
                            return Code.fromValue(0);
                        }
                        else {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(reg));
                            code.appendAsm("and %r25, 16, " + genLocation(reg)); // 16 -> mask for C
                            code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        }
                        break;
                    case "||":
                        if(right == 0) {
                            // generates reg = (reg != 0)
                            code.appendAsm("cmp %r0, " + genLocation(reg));
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
                        code.appendAsm(genImmediateOperation("subcc", reg, right, reg));
                        code.appendAsm("and %r25, 64, " + genLocation(reg)); // 64 -> mask for Z
                        code.appendAsm("srl " + genLocation(reg) + ", 6, " + genLocation(reg)); // normalization
                        if (operator.equals("!="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case "<":
                    case ">=":
                        code.appendAsm(genImmediateOperation("subcc", reg, right, reg));
                        code.appendAsm("and %r25, 128, " + genLocation(reg)); // 128 -> mask for N (= left < right)
                        code.appendAsm("srl " + genLocation(reg) + ", 7, " + genLocation(reg)); // normalization
                        if(operator.equals(">="))
                            code.appendAsm("xor " + genLocation(reg) + ", 1, " + genLocation(reg));
                        break;
                    case ">":
                    case "<=":
                        tmp = allocator.getFreeReg();
                        code.appendAsm(genSet(right, tmp));
                        code.appendAsm("subcc " + genLocation(tmp) + ", " + genLocation(reg) + ", %r0");
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
                // warning : Don't forget the order is important here...
                CodeValue right = forceAsm(rightOperand, rightType);
                CodeValue left = forceAsm(leftOperand, leftType);

                Location reg = right.reg;
                allocator.push(reg);

                /* This is very tricky.
                 * It is possible that both forceAsm(right) and forceAsm(left) make
                 * a getFreeReg(), and so right.reg and left.reg are the same.
                 * An other potential problem is when forceAsm(right) makes a pop()
                 * and forceAsm(left) makes a getFreeReg().
                 */
                if(right.reg.equals(left.reg)) {
                    left = forceAsm(leftOperand, leftType);
                }

                forceValue(left.code, left.reg, leftType);
                forceValue(right.code, right.reg, rightType);

                left.code.prependAsm(genComment("left operand :"));
                right.code.prependAsm(genComment("right operand :"));

                Code code = left.code;
                code.appendAsm(right.code.getAsm());
                code.appendAsm(genComment(genLocation(left.reg) + " " + operator + " " + genLocation(right.reg)));

                switch(operator) {
                    case "+":
                        code.appendAsm("add " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "-":
                        code.appendAsm("sub " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "*":
                        code.appendAsm("umulcc " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "/":
                    case "%":
                        throw new UnsupportedOperationException("CRAPS");
                    case "&":
                        code.appendAsm("and " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "|":
                        code.appendAsm("or " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "^":
                        code.appendAsm("xor " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "<<":
                        code.appendAsm("sll " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case ">>":
                        code.appendAsm("srl " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        break;
                    case "&&":
                        // "and" in craps is a bitwise operator.
                        // 0b10 and 0b100 = 0, too bad..

                        // generates left.reg = (left.reg != 0)
                        code.appendAsm("cmp %r0, " + genLocation(left.reg));
                        code.appendAsm("and %r25, 16, " + genLocation(left.reg)); // 16 -> mask for C

                        // generates right.reg = (right.reg != 0)
                        code.appendAsm("cmp %r0, " + genLocation(right.reg));
                        code.appendAsm("and %r25, 16, " + genLocation(right.reg)); // 16 -> mask for C

                        code.appendAsm("and " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
                        code.appendAsm("srl " + genLocation(reg) + ", 4, " + genLocation(reg)); // normalization
                        break;
                    case "||":
                        code.appendAsm("or " + genLocation(left.reg) + ", " + genLocation(right.reg) + ", " + genLocation(reg));
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
        if(operand.hasValue() && !operand.getIsAddress()) {
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
            forceValue(c.code, c.reg, type);
            c.code.appendAsm(genComment("operator " + operator));

            switch(operator) {
                case "-":
                    c.code.appendAsm("negcc " + genLocation(c.reg));
                    break;
                case "!":
                    c.code.appendAsm("cmp " + genLocation(c.reg) + ", %r0");
                    c.code.appendAsm("and %r25, 64, " + genLocation(c.reg)); // 64 -> mask for Z
                    c.code.appendAsm("srl " + genLocation(c.reg) + ", 6, " + genLocation(c.reg)); // normalization
                    break;
                case "~":
                    c.code.appendAsm("xor " + genLocation(c.reg) + ", -1, " + genLocation(c.reg));
                    break;
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }

            allocator.push(c.reg);
            return c.code;
        }
    }

    public Code genCast(TTYPE newType, TTYPE oldType, Code castedCode) {
        if(!castedCode.getIsAddress()
                && (castedCode.hasValue() || castedCode.hasLocation())) {
            return castedCode;
        }
        else {
            CodeValue c = forceAsm(castedCode, oldType);
            forceValue(c.code, c.reg, oldType);
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
        return new Code("sub %sp, " + info.getType().getSize() + ", %sp");
    }

    /**
     * Declare a variable with an initial value
     *
     * @param value The code for the initial value
     */
    public Code genDecl(INFOVAR info, Code operand, TTYPE type) {
        return genArg(operand, type);
    }

    /**
     * Declare a global variable
     */
    public GlobalCode genDeclGlobal(INFOVAR info) {
        return new GlobalCode(
            "glob_" + info.getLocation().getOffset() + ": "
            + genBytes(Collections.nCopies(info.getSize(), 0)) + "\n"
        );
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
        if(operand.hasValue() && !operand.getIsAddress()
                && fixedRegisters.containsKey(operand.getValue())) {
            return new Code("push " + fixedRegisters.get(operand.getValue()));
        }
        else {
            CodeValue c = forceAsm(operand, type);
            forceValue(c.code, c.reg, type);
            c.code.appendAsm(genPush(c.reg));
            return c.code;
        }
    }

    public Code genAccess(Code pointerCode, TTYPE pointedType) {
        Code code;

        if(pointerCode.getIsAddress()) {
            CodeValue c = forceAsm(pointerCode, new TPOINTER(pointedType, getPointerSize()));
            code = c.code;
            forceValue(code, c.reg, new TPOINTER(pointedType, getPointerSize()));
            allocator.push(c.reg);
        }
        else {
            code = pointerCode;
        }

        code.setIsAddress(true);
        return code;
    }

    public Code genStackArrayAccess(INFOVAR info, Code posCode) {
        TARRAY type = (TARRAY) info.getType();
        Code code;

        if(posCode.hasValue() && !posCode.getIsAddress()) {
            long offset = posCode.getValue() * type.getElementsType().getSize();
            Location loc = info.getLocation();
            code = Code.fromLocation(new Location(loc.getType(),
                                                  loc.getOffset() + offset));
        }
        else {
            CodeValue c = forceAsm(posCode, new TINTEGER(getIntSize()));
            code = c.code;
            allocator.push(c.reg);

            code.appendAsm(genComment("stack array access :"));
            if(type.getElementsType().getSize() != 1)
                code.appendAsm("umulcc " + genLocation(c.reg) + ", " + type.getElementsType().getSize() + ", " + genLocation(c.reg));

            code.appendAsm("add " + genLocation(c.reg) + ", %fp, " + genLocation(c.reg));
            code.appendAsm("sub " + genLocation(c.reg) + ", " + (-info.getLocation().getOffset()) + ", " + genLocation(c.reg));
            code.setIsAddress(true);
        }

        return code;
    }

    public Code genPointerArrayAccess(INFOVAR info, Code posCode) {
        TPOINTER type = (TPOINTER) info.getType();
        Code code;

        if(posCode.hasValue() && !posCode.getIsAddress()) {
            long offset = posCode.getValue() * type.getType().getSize();
            Location reg = allocator.getFreeReg();
            allocator.push(reg);

            code = new Code(genMovMemToReg(genLocation(info.getLocation()), reg)); // pointer value
            code.appendAsm(genComment("pointer array access :"));

            if(offset > 0)
                code.appendAsm("add " + genLocation(reg) + ", " + offset + ", " + genLocation(reg));
        }
        else {
            CodeValue c = forceAsm(posCode, new TINTEGER(getIntSize()));
            code = c.code;
            forceValue(code, c.reg, new TINTEGER(getIntSize()));
            allocator.push(c.reg);

            Location pointerReg = allocator.getFreeReg();
            code.appendAsm(genMovMemToReg(genLocation(info.getLocation()), pointerReg)); // pointer value
            code.appendAsm(genComment("pointer array access :"));

            if(type.getType().getSize() != 1)
                code.appendAsm("umulcc " + genLocation(c.reg) + ", " + type.getType().getSize() + ", " + genLocation(c.reg));

            code.appendAsm("add " + genLocation(pointerReg) + ", " + genLocation(c.reg) + ", " + genLocation(c.reg));
        }

        code.setIsAddress(true);
        return code;
    }

    public Code genFieldAccess(TSTRUCT struct, FIELD field, Code operand) {
        int offset = struct.getFieldOffset(field.getName());
        Code code;

        if(operand.hasLocation() && !operand.getIsAddress()) {
            Location loc = operand.getLocation();
            code = Code.fromLocation(new Location(loc.getType(),
                                                  loc.getOffset() + offset));
        }
        else {
            assert(operand.getIsAddress());
            CodeValue c = forceAsm(operand, struct);
            code = c.code;

            if(offset > 0)
                code.appendAsm("add " + genLocation(c.reg) + ", " + offset + ", " + genLocation(c.reg));

            allocator.push(c.reg);
            code.setIsAddress(true);
        }

        return code;
    }

    public Code genPointerFieldAccess(TSTRUCT struct, FIELD field, Code operand) {
        int offset = struct.getFieldOffset(field.getName());
        CodeValue c = forceAsm(operand, new TPOINTER(struct, getPointerSize()));
        forceValue(c.code, c.reg, new TPOINTER(struct, getPointerSize()));

        c.code.appendAsm(genComment("field access :"));
        if(offset > 0)
            c.code.appendAsm("add " + genLocation(c.reg) + ", " + offset + ", " + genLocation(c.reg));

        c.code.setIsAddress(true);
        allocator.push(c.reg);
        return c.code;
    }

    public Code genBlock(Code instsCode, VariableLocator vloc) {
        CRAPSVariableLocator vl = (CRAPSVariableLocator) vloc;

        if(vl.getLocalOffset() != 0) {
            instsCode.appendAsm("add %sp, " + (-vl.getLocalOffset()) + ", %sp " + genComment("removing local variables"));
        }

        return instsCode;
    }

    public Code genVariable(INFOVAR i) {
        return Code.fromLocation(i.getLocation());
    }

    public Code genAddress(INFOVAR i) {
        Location reg = allocator.getFreeReg();
        allocator.push(reg);

        if(i.getLocation().getType() == Location.LocationType.STACKFRAME) {
            return new Code("sub %fp, " + (-i.getLocation().getOffset()) + ", " + genLocation(reg));
        }
        else { // locType == Location.LocationType.ABSOLUTE
            return new Code("set glob_" + i.getLocation().getOffset() + ", " + genLocation(reg));
        }
    }

    public Code genInt(String cst) {
        return Code.fromValue(getLongFromString(cst));
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
        return Code.fromValue(0);
    }

    public Code genBool(int b) {
        return Code.fromValue(b);
    }

    public Code genChar(String c) {
        return Code.fromValue(getCharFromString(c));
    }

    /**
     * Generates the assembler for a lazy code
     *
     * warning: the register is not pushed on the allocator
     */
    private CodeValue forceAsm(Code operand, TTYPE type) {
        Code code;
        Location reg;

        if(operand.hasValue()) {
            reg = allocator.getFreeReg();
            code = new Code(genSet(operand.getValue(), reg));
        }
        else if(operand.hasLocation()) {
            reg = allocator.getFreeReg();
            Location loc = operand.getLocation();

            // special case for arrays and structures
            if((type instanceof TARRAY || type instanceof TSTRUCT)
                    && !operand.getIsAddress()) {
                if(loc.getType() == Location.LocationType.STACKFRAME) {
                    code = new Code("sub %fp, " + (-loc.getOffset()) + ", " + genLocation(reg));
                }
                else { // locType == Location.LocationType.ABSOLUTE
                    code = new Code("set glob_" + loc.getOffset() + ", " + genLocation(reg));
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

        code.setIsAddress(operand.getIsAddress());
        return new CodeValue(code, reg);
    }

    /**
     * Ensures the Code gives a value (dereference)
     *
     * @param operand The code
     * @param reg In which register is the value
     * @param type The type of the value
     */
    private void forceValue(Code operand, Location reg, TTYPE type) {
        assert(!operand.hasValue() && !operand.hasLocation());

        if(operand.getIsAddress()) {
            operand.appendAsm(genMovMemToReg("[" + genLocation(reg) + "]", reg));
            operand.setIsAddress(false);
        }
    }

    /**
     * A code that puts a value in a register
     */
    private class CodeValue {
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
        assert(reg.getType() == Location.LocationType.REGISTER);
        return "push " + genLocation(reg);
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
    private String genMovMemToReg(String mem, Location reg) {
        assert(reg.getType() == Location.LocationType.REGISTER);
        return "ld " + mem + ", " + genLocation(reg);
    }

    /**
     * Generate a set
     */
    private String genSet(long value, Location reg) {
        assert(reg.getType() == Location.LocationType.REGISTER);

        if(value >= -4096 && value <= 4095)
            return "setq " + value + ", " + genLocation(reg);
        else
            return "set " + value + ", " + genLocation(reg);
    }

    /**
     * Generate an operation between a register and an immediate value
     */
    private String genImmediateOperation(String op, Location left, long right, Location result) {
        assert(left.getType() == Location.LocationType.REGISTER);
        assert(result.getType() == Location.LocationType.REGISTER);

        if(right >= -4096 && right <= 4095)
            return op + " " + genLocation(left) + ", " + right + ", " + genLocation(result);
        else {
            Location tmp = allocator.getFreeReg();
            return "set " + right + ", " + genLocation(tmp) + "\n"
                          + genOperation(op, left, tmp, result);
        }
    }

    /**
     * Generate an operation between a register and a register
     */
    private String genOperation(String op, Location left, Location right, Location result) {
        assert(left.getType() == Location.LocationType.REGISTER);
        assert(right.getType() == Location.LocationType.REGISTER);
        assert(result.getType() == Location.LocationType.REGISTER);

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
