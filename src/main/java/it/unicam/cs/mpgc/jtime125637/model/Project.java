package it.unicam.cs.mpgc.jtime125637.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class Project {
    private Integer id;
    private String nome;
    private String descrizione;
    private String stato;
    private Set<Activity> activities;

    public Project(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stato = "attivo";
        this.activities = new HashSet<>();
    }

    public boolean isAttivo() {
        return "attivo".equalsIgnoreCase(stato);
    }

    public boolean isCompletato() {
        return "completato".equalsIgnoreCase(stato);
    }

    public boolean isEmpty() {
        return activities == null || activities.isEmpty();
    }

    public boolean tutteAttivitaTerminate() {
        if (activities == null || activities.isEmpty()) {
            return true;
        }
        return activities.stream().allMatch(Activity::isCompletata);
    }
}
