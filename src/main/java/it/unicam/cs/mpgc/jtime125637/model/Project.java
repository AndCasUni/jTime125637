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

    public Project(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.stato = "attivo";
        this.activities = new HashSet<>();
    }

    // Getter e Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public Set<Activity> getActivities() { return activities; }
    public void setActivities(Set<Activity> activities) { this.activities = activities; }

    // Metodi helper
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
            return false;
        }
        return activities.stream().allMatch(Activity::isCompletata);
    }

    @Override
    public String toString() {
        return (id != null ? id + ": " : "") + nome;
    }
}
