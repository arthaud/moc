package moc.gc;

import moc.compiler.MOCException;

/**
 * Cette interface décrit une machine cible.
 */
public interface IMachine {
    /**
     * Suffixe du fichier cible (.tam par exemple)
     */
    String getSuffixe();

    /**
     * Écrit le code dans un fichier à partir du nom du fichier source et du suffixe
     */
    void writeCode(String fileName, String code) throws MOCException;
}
