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

    public Code genFunction(TFUNCTION function, Code code) {
        return null;
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

    public Code genReturn(Code returnVal) {
        return null;
    }

    public Code includeAsm(String asmCode) {
        return new Code(asmCode);
    }

    public Code genAffectation(Code address, Code affectedVal) {
        return null;
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
        return null;
    }

    public Code genDecl(TTYPE type) {
        return null;
    }

    public Code genAcces(TTYPE pointed_type){
        return null;
    }

    public Code genVariable(INFOVAR i) {
        return null;
    }
    public Code genInt(String cst){
        return null;
    }

    public Code genString(String txt){
        return null;
    }

    public Code genNull(){
        return null;
    }

    public Code genBool(int b){
        return null;
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
