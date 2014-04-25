package moc.compiler;

import java.io.Serializable;

import mg.egg.eggc.runtime.libjava.ISourceUnit;
import mg.egg.eggc.runtime.libjava.problem.IProblem;
import mg.egg.eggc.runtime.libjava.problem.ProblemReporter;
import mg.egg.eggc.runtime.libjava.problem.ProblemRequestor;
import moc.egg.MOC;

/**
 * The main class of the generated MOC compiler
 */
public class MOCC implements Serializable {
    private static final long serialVersionUID = 1L;

    private static void help() throws MOCException {
        throw new MOCException(Messages.getString("MOC.help"));
    }

    private static void error(String a) throws MOCException {
        throw new MOCException(Messages.getString("MOC.error", a));
    }

    private static void checkFile(String fileName) throws MOCException {
        if (!fileName.endsWith(".moc")) {
            error(Messages.getString("MOC.ext_error"));
        }
    }

    private static MOCSourceFile parseArguments(String[] args) throws MOCException {
        String fileName = null;
        String machine = null;
        int verbosity = 0;

        for (int i = 0; i < args.length; i++) {
            String opt = args[i];
            if(opt.equals("-h") || opt.equals("--help")) { /* -h, --help */
                help();
            }
            else if(opt.equals("-m") || opt.equals("--machine")) { /* -m, --machine */
                if(i + 1 < args.length) {
                    i++;
                    machine = args[i];
                }
                else {
                    error(Messages.getString("MOC.machine_error", opt));
                }
            }
            else if(opt.startsWith("-v")) { /* -v */
                verbosity = opt.length() - 1;
            }
            else if(fileName == null) { /* FILE.moc */
                checkFile(opt);
                fileName = opt;
            }
            else {
                error(Messages.getString("MOC.unknown_option", opt));
            }
        }

        if(fileName == null) {
            error(Messages.getString("MOC.file_error"));
        }

        return new MOCSourceFile(fileName, machine, verbosity);
    }

    public static void main(String[] args) {
        try {
            // Create the source
            MOCSourceFile cu = parseArguments(args);

            // Error management
            ProblemReporter prp = new ProblemReporter(cu);
            ProblemRequestor prq = new ProblemRequestor();

            // Start compilation
            System.out.println(Messages.getString("MOC.compiling", cu.getFileName(), cu.getMachine().getName()));
            MOC compilo = new MOC(prp);
            prq.beginReporting();

            compilo.set_source((MOCSourceFile) cu);
            compilo.set_eval(true);
            compilo.compile(cu);

            // Handle errors
            for (IProblem problem : prp.getAllProblems())
                prq.acceptProblem(problem);

            prq.endReporting();
            System.exit(prq.getFatal());
        } catch (MOCException e) {
            // Internal errors
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            // Other errors
            e.printStackTrace();
            System.exit(1);
        }
    }
}
