package moc.compiler;

import mg.egg.eggc.runtime.libjava.SourceUnit;
import moc.cg.AbstractMachine;
import moc.cg.MTAM;
import moc.cg.Mx86;
import moc.cg.MCRAPS;

/**
 * Describes a MOC compilation unit
 */
public class MOCSourceFile extends SourceUnit {

    // target machine
    private AbstractMachine machine;

    private int verbosity = 0;

    public MOCSourceFile(String fileName, String machine, int verbosity) throws MOCException {
        super(fileName);
        setMachine(machine);
        setVerbosity(verbosity);
    }

    /**
     * Determines and creates the target machine
     */
    private void setMachine(String machine) {
        if (machine != null) {
            if (machine.equals("tam")) {
                this.machine = new MTAM();
            }
            else if (machine.equals("x86")) {
                this.machine = new Mx86();
            }
            else { // default
                this.machine = new MCRAPS();
            }
        }
    }

    private void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    public AbstractMachine getMachine() {
        return machine;
    }

    public int getVerbosity() {
        return verbosity;
    }
}
