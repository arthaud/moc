package moc.compiler;

import mg.egg.eggc.runtime.libjava.SourceUnit;
import moc.gc.AbstractMachine;
import moc.gc.MTAM;

/**
 * Décrit une unité de compilation MOC
 */
public class MOCSourceFile extends SourceUnit {

    // obligatoire pour un SourceUnit : le nom du fichier
    private String fileName;

    // la machine cible
    private AbstractMachine machine;

    private String machName;

    public MOCSourceFile(String[] args) throws MOCException {
        super(args[0]);
        analyze(args);
    }

    /**
     * Affiche les options disponibles
     */
    private void usage(String a) throws MOCException {
        throw new MOCException("Option incorrecte : " + a + ". "
                + Messages.getString("MOC.usage"));
    }

    /**
     * Analyse les arguments supplémentaires du compilateur
     */
    public void analyze(String[] args) throws MOCException {
        int argc = args.length;
        fileName = args[0];

        // machine cible ?
        if (argc == 1) {
            setMachine("tam");
        } else {
            // nom de la machine ?
            for (int i = 1; i < argc; i++) {
                String a = args[i];
                if ("-m".equals(a)) {
                    if (i + 1 < argc) {
                        i++;
                        setMachine(args[i]);
                    } else {
                        usage(a);
                    }
                } else {
                    usage(a);
                }
            }
        }
    }

    /**
     * Fixe et créé la machine cible
     */
    private void setMachine(String mach) {
        machName = mach;
        if ("tam".equals(mach)) {
            machine = new MTAM();
        } else {
            // TODO si la machine n'est pas tam
            // machine = new ???();
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

}
