package it.unicam.cs.mpgc.jtime125637.model;

import java.util.HashSet;
import java.util.Set;


public class Project {
    private Integer id;
    private String nome;
    private String descrizione;
    private String stato = "attivo";
    private Set<Activity> activities = new HashSet<>();

    public Project() {}

    /**
     * Costruttore per creare un nuovo progetto con nome e descrizione.
     * Il progetto viene creato con stato "attivo" e senza attività.
     *
     * @param nome nome del progetto
     * @param descrizione descrizione del progetto
     */
    public Project(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stato = "attivo";
        this.activities = new HashSet<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Set<Activity> getActivities() {
        return activities;
    }

    public void setActivities(Set<Activity> activities) {
        this.activities = activities;
    }

    /**
     * Verifica se il progetto è eliminabile.
     * Un progetto è eliminabile solo se non ha attività assegnate.
     *
     * @return true se il progetto non contiene attività, false altrimenti
     */
    public boolean isEliminabile() {
        return isEmpty();
    }

    /**
     * Verifica se il progetto è nello stato attivo.
     *
     * @return true se lo stato è "attivo", false altrimenti
     */
    public boolean isAttivo() {
        return "attivo".equalsIgnoreCase(stato);
    }

    /**
     * Verifica se il progetto è nello stato completato.
     *
     * @return true se lo stato è "completato", false altrimenti
     */
    public boolean isCompletato() {
        return "completato".equalsIgnoreCase(stato);
    }

    /**
     * Verifica se il progetto non contiene attività.
     *
     * @return true se non ci sono attività associate, false altrimenti
     */
    public boolean isEmpty() {
        return activities == null || activities.isEmpty();
    }

    /**
     * Verifica se tutte le attività del progetto sono state completate.
     *
     * @return true se tutte le attività sono completate, false se il progetto è vuoto o ha attività non completate
     */
    public boolean tutteAttivitaTerminate() {
        if (activities == null || activities.isEmpty()) {
            return false;
        }
        return activities.stream().allMatch(Activity::isCompletata);
    }

    /**
     * Restituisce una rappresentazione testuale del progetto.
     *
     * @return stringa nel formato "id: nome" o solo "nome" se l'ID è null
     */
    @Override
    public String toString() {
        return (id != null ? id + ": " : "") + nome;
    }
}
