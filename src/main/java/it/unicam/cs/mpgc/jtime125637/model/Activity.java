package it.unicam.cs.mpgc.jtime125637.model;

import java.util.Date;


public class Activity {
    private Integer id;
    private String nome;
    private String descrizione;
    private String stimaTempo;
    private String durataEffettiva = "00:00";
    private Date dataPianificazione;
    private boolean eliminabile = true;
    private Project project;

    public Activity() {}

    /**
     * Costruttore per creare una nuova attività con nome, descrizione e stima del tempo.
     *
     * @param nome nome dell'attività
     * @param descrizione descrizione dell'attività
     * @param stimaTempo stima del tempo richiesto in formato "HH:MM"
     */
    public Activity(String nome, String descrizione, String stimaTempo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stimaTempo = stimaTempo;
        this.eliminabile = true;
        this.durataEffettiva = "00:00";
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

    public String getStimaTempo() {
        return stimaTempo;
    }

    public void setStimaTempo(String stimaTempo) {
        this.stimaTempo = stimaTempo;
    }

    public String getDurataEffettiva() {
        return durataEffettiva;
    }

    public void setDurataEffettiva(String durataEffettiva) {
        this.durataEffettiva = durataEffettiva;
        if (durataEffettiva != null && !durataEffettiva.equals("00:00")) {
            this.eliminabile = false;
        }
    }


    public Date getDataPianificazione() {
        return dataPianificazione;
    }

    public void setDataPianificazione(Date dataPianificazione) {
        this.dataPianificazione = dataPianificazione;
        if (dataPianificazione != null) {
            this.eliminabile = false;
        }
    }

    public boolean isEliminabile() {
        return eliminabile;
    }

    public void setEliminabile(boolean eliminabile) {
        this.eliminabile = eliminabile;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        this.eliminabile = (project == null);
    }

    /**
     * Verifica se l'attività è stata completata.
     *
     * @return true se la durata effettiva è stata registrata, false altrimenti
     */
    public boolean isCompletata() {
        return durataEffettiva != null && !durataEffettiva.equals("00:00");
    }

    /**
     * Verifica se l'attività è stata pianificata.
     *
     * @return true se è stata assegnata una data di pianificazione, false altrimenti
     */
    public boolean isPianificata() {
        return dataPianificazione != null;
    }

    /**
     * Verifica se l'attività è assegnata a un progetto.
     *
     * @return true se l'attività è associata a un progetto, false altrimenti
     */
    public boolean isAssegnata() {
        return project != null;
    }

    /**
     * Verifica se l'attività è ancora attiva (non completata).
     *
     * @return true se l'attività non è stata completata, false altrimenti
     */
    public boolean isAttiva() {
        return !isCompletata();
    }
}
