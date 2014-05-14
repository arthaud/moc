package moc.cg;

public class x86Code extends Code {
    protected int resultRegister = -1;

    public x86Code(String asm, int resultRegister) {
        this.asm = asm;
        this.resultRegister = resultRegister;
    }
    
    public x86Code(String asm) {
        this.asm = asm;
        this.resultRegister = -1;
    }

    public int getResultRegister()
    {
        return resultRegister;
    }
    
    public void setResultRegister(int r)
    {
        resultRegister = r;
    }
    
    public String resultRegisterName()
    {
        return Mx86.registerNames[resultRegister];
    }
}
