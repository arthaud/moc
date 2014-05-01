package moc.cg;

import moc.type.TTYPE;
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

        public int getLocalOffset(){
            return localOffset;
        }

        public VariableLocator getSon(){
            return new TamVariableLocator(offset);
        }

    }

    public VariableLocator getVariableLocator() {
        return new TamVariableLocator(3);
    }

    public Code genFunction(TFUNCTION function, Code code) {
        code.prependAsm("_" + function.getName() + ":");
        return code;
    }

    public Code genConditional(Code condition, Code trueBloc, Code falseBloc) {
        int num = getLabelNum();
        String st;
        boolean hasElse= falseBloc.getAsm().equals("");

        Code retCode = new Code(condition.getAsm());
        if(hasElse)
            st = "JUMPIF 0 ELSEBLOC_" + num;
        else
            st = "JUMPIF 0 END_COND_" + num;

        retCode.appendAsm(st);
        retCode.appendAsm( trueBloc.getAsm() );
        if(hasElse){
            retCode.appendAsm("JUMP END_COND_" + num + ":"); 
            retCode.appendAsm("ELSEBLOC_" + num + ":");
            retCode.appendAsm(falseBloc.getAsm());
        }
        retCode.appendAsm("END_COND_" + num + ":"); 
        
        return retCode;
    }

    public Code genReturn(Code returnVal, TFUNCTION fun) {
        int retsize = fun.getReturnType().getSize();
        int paramsize = fun.getParameterTypes().getSize();
        Code retCode = new Code("RETURN (" + paramsize + ") " + paramsize );
        retCode.prependAsm(returnVal.getAsm());
        return retCode;
    }

    public Code includeAsm(String asmCode) {
        return new Code(asmCode);
    }

    public Code genAffectation(Code address, Code affectedVal,TTYPE type) {
        if ( ! address.getIsAddress() && address.getAddress()==0)
            return new Code(";Affectation error: left operand has no address");
        Code retCode= affectedVal;
        if(address.getAddress()!=0){
            retCode.appendAsm("STORE ("+ type.getSize()+ ") "+ address.getAddress() +"[LB]");
        }else{
            retCode.appendAsm(address.getAsm());
            retCode.appendAsm("STOREI (" +type.getSize() + ")" );
        }
        return retCode;
    }

    public Code genBinary(Code leftOperand, Code rightOperand, String operator) {
        return null;
    }

    public Code genUnary(Code operand, String operator) {
        return null;
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
        return new Code("PUSH (0) " + type.getSize() );
    }

    public Code genAcces(Code pointerCode, TTYPE pointed_type){
        return null;
    }

    public Code genBloc(Code c, VariableLocator vloc){
        TamVariableLocator vl = (TamVariableLocator) vloc;
        String st= "POP (0) " +vl.getLocalOffset();
        Code ret = c;
        ret.appendAsm(st);
        return ret;
    }

    public Code genVariable(INFOVAR i) {
        Code retCode=new Code("LOAD "+i.getSize() +" " + i.getLocation().getOffset());
        retCode.setIsAddress(false);
        retCode.setAddress(i.getLocation().getOffset());
        return retCode;
    }

    public Code genInt(String cst){
        Code retCode= new Code("LOADL "+ cst );
        retCode.setIsAddress(false);
        retCode.setAddress(0);
        return retCode;
    }

    public Code genString(String txt){
        return null;
    }

    public Code genNull(){
        return null;
    }

    public Code genBool(int b){
        Code retCode= new Code("LOADL "+ b );
        retCode.setIsAddress(false);
        retCode.setAddress(0);
        return retCode;
    }

    public Code genChar(String c){
        return null;
    }


    private int labelNum = 0 ;

    private int getLabelNum(){
        labelNum ++ ;
        return labelNum -1 ;
    }

}
