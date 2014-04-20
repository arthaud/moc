package moc.gc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import moc.compiler.MOCException;

/**
 * Cette classe décrit une machine cible.
 */
public abstract class AbstractMachine implements IMachine {

    /**
     * Écrit le code dans un fichier à partir du nom du fichier source et du suffixe
     */
    @Override
    public void writeCode(String fname, String code) throws MOCException {
        try {
            // pré verifiée au lancement
            int pt = fname.lastIndexOf('.');
            String name = fname.substring(0, pt);
            String asmName = name + "." + getSuffixe();
            System.err.println("Écriture du code dans " + asmName);
            PrintWriter pw = new PrintWriter(new FileOutputStream(asmName));
            pw.print("; Generated code for " + fname
                    + ".\n; Do not modify by hand\n" + code);
            pw.close();
        } catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }
}
