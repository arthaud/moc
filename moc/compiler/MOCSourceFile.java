package moc.compiler;

import mg.egg.eggc.runtime.libjava.SourceUnit;
import moc.cg.AbstractMachine;
import moc.cg.MTAM;

/**
 * Describes a MOC compilation unit
 */
public class MOCSourceFile extends SourceUnit {

    // mandatory for a SourceUnit
    private String fileName;

    // target machine
    private AbstractMachine machine;

    private String machName;

    private int verbosity = 0;

    public MOCSourceFile(String[] args) throws MOCException {
        super(args[0]);
        analyze(args);
    }

    /**
     * Print available options
     */
    private void usage(String a) throws MOCException {
        throw new MOCException("Option incorrecte : " + a + ". "
                + Messages.getString("MOC.usage"));
    }

    /**
     * Analyse supplementary arguments of the compiler
     */
    public void analyze(String[] args) throws MOCException {
        int argc = args.length;
        fileName = args[0];

        if (argc == 1) {
            setMachine("tam");
        } else {
            // machine name
            for (int i = 1; i < argc; i++) {
                String a = args[i];
                if ("-m".equals(a)) {
                    if (i + 1 < argc) {
                        i++;
                        setMachine(args[i]);
                    } else {
                        usage(a);
                    }
                } else if(a.startsWith("-v")) {
                    verbosity = a.length() - 1;
                }
                else {
                    usage(a);
                }
            }
        }
    }

    /**
     * Determines and creates the target machine
     */
    private void setMachine(String mach) {
        machName = mach;
        if ("tam".equals(mach)) {
            machine = new MTAM();
        } else {
            // TODO: x86_32
        }
    }

    public AbstractMachine getMachine() {
        return machine;
    }

    public String getMachName() {
        return machName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getVerbosity() {
        return verbosity;
    }
}
