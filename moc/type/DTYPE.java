package moc.type;

public interface DTYPE {
    /**
     * La taille du type de donnée : dépend de la machine
     */
    public int getTaille();

    public String getNom();

    /**
     * Fonction de compatibilité avec l'autre type
     */
    public boolean compareTo(DTYPE autre);

    public String toString();
}
