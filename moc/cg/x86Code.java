package moc.cg;

public class x86Code extends Code {
    protected int resultRegister = -1;

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
