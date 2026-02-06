package it.unicam.cs.mpgc.jtime125637.model;

import java.time.LocalDate;

public class Activity {
    private Integer id;
    private String nome;
    private String descrizione;
    private String stimaTempo;
    private String durataEffettiva = "00:00";
    private LocalDate dataPianificazione;
    private boolean eliminabile = true;
    private Project project;

    public Activity() {}

    public Activity(String nome, String descrizione, String stimaTempo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stimaTempo = stimaTempo;
        this.eliminabile = true;
        this.durataEffettiva = "00:00";
    }

    // Getter e Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getStimaTempo() { return stimaTempo; }
    public void setStimaTempo(String stimaTempo) { this.stimaTempo = stimaTempo; }

    public String getDurataEffettiva() { return durataEffettiva; }
    public void setDurataEffettiva(String durataEffettiva) {
        this.durataEffettiva = durataEffettiva;
        if (durataEffettiva != null && !durataEffettiva.equals("00:00")) {
            this.eliminabile = false;
        }
    }

    public LocalDate getDataPianificazione() { return dataPianificazione; }
    public void setDataPianificazione(LocalDate dataPianificazione) {
        this.dataPianificazione = dataPianificazione;
        if (dataPianificazione != null) {
            this.eliminabile = false;
        }
    }

    public boolean isEliminabile() { return eliminabile; }
    public void setEliminabile(boolean eliminabile) { this.eliminabile = eliminabile; }

    public Project getProject() { return project; }
    public void setProject(Project project) {
        this.project = project;
        this.eliminabile = (project == null);
    }

    // Metodi helper
    public boolean isCompletata() {
        return durataEffettiva != null && !durataEffettiva.equals("00:00");
    }

    public boolean isPianificata() {
        return dataPianificazione != null;
    }

    public boolean isAssegnata() {
        return project != null;
    }

    public boolean isAttiva() {
        return !isCompletata();
    }

    @Override
    public String toString() {
        return (id != null ? id + ": " : "") + nome;
    }


}
