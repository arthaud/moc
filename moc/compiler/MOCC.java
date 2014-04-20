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

    private static void checkFile(String[] args) throws MOCException {
        // check .moc extension
        if (args.length == 0 || !args[0].endsWith(".moc")) {
            throw new MOCException(Messages.getString("MOC.extError"));
        }
    }

    public static void main(String[] args) {
        try {
            // At least the name of the source file is needed
            checkFile(args);

            // Create the source
            ISourceUnit cu = new MOCSourceFile(args);

            // Error management
            ProblemReporter prp = new ProblemReporter(cu);
            ProblemRequestor prq = new ProblemRequestor();

            // Start compilation
            System.err.println("Compiling " + cu.getFileName());
            MOC compilo = new MOC(prp);
            prq.beginReporting();

            compilo.set_source((MOCSourceFile) cu);
            compilo.set_eval(true);
            compilo.compile(cu);

            // Handle errors
            for (IProblem problem : prp.getAllProblems())
                prq.acceptProblem(problem);

            prq.endReporting();
            System.err.println(Messages.getString("MOC.ok"));
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
