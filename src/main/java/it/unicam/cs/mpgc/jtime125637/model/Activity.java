package it.unicam.cs.mpgc.jtime125637.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Activity {
    private Integer id;
    private String nome;
    private String descrizione;
    private String stimaTempo;
    private String durataEffettiva;
    private LocalDate dataPianificazione;
    private boolean eliminabile;
    private Project project;

    public Activity(String nome, String descrizione, String stimaTempo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stimaTempo = stimaTempo;
        this.eliminabile = true;
        this.durataEffettiva = "00:00";
    }

    public void setDurataEffettiva(String durataEffettiva) {
        this.durataEffettiva = durataEffettiva;
        if (durataEffettiva != null && !durataEffettiva.equals("00:00")) {
            this.eliminabile = false;
        }
    }

    public void setDataPianificazione(LocalDate dataPianificazione) {
        this.dataPianificazione = dataPianificazione;
        if (dataPianificazione != null) {
            this.eliminabile = false;
        }
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            this.eliminabile = false;
        }
    }

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
}
