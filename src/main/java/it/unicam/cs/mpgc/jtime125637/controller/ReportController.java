package it.unicam.cs.mpgc.jtime125637.controller;

import it.unicam.cs.mpgc.jtime125637.model.Activity;
import it.unicam.cs.mpgc.jtime125637.model.ActivityRepository;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import it.unicam.cs.mpgc.jtime125637.model.ProjectRepository;

import java.time.LocalDate;
import java.util.List;

public class ReportController {
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final ActivityRepository activityRepository = new ActivityRepository();

    public StatisticheProgetti getStatisticheProgetti() {
        List<Project> tutti = projectRepository.findAll();
        long attivi = tutti.stream().filter(Project::isAttivo).count();
        long completati = tutti.stream().filter(Project::isCompletato).count();
        long vuoti = tutti.stream().filter(Project::isEmpty).count();
        
        return new StatisticheProgetti(
            (int) tutti.size(),
            (int) attivi,
            (int) completati,
            (int) vuoti
        );
    }

    public StatisticheAttivita getStatisticheAttivita() {
        List<Activity> tutte = activityRepository.findAll();
        long completate = tutte.stream().filter(Activity::isCompletata).count();
        long pianificate = tutte.stream().filter(Activity::isPianificata).count();
        long nonPianificate = tutte.size() - pianificate;
        long nonCompletate = tutte.size() - completate;
        
        return new StatisticheAttivita(
            tutte.size(),
            (int) completate,
            (int) nonCompletate,
            (int) pianificate,
            (int) nonPianificate
        );
    }

    public StatisticheGiorno getStatistichePerGiorno(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data non valida");
        }
        List<Activity> attivitaGiorno = activityRepository.findByDate(data);
        long completate = attivitaGiorno.stream().filter(Activity::isCompletata).count();
        long attive = attivitaGiorno.size() - completate;
        
        double percentualeCompletate = attivitaGiorno.isEmpty() ? 0.0 :
            (completate * 100.0) / attivitaGiorno.size();
        
        return new StatisticheGiorno(
            attivitaGiorno.size(),
            (int) completate,
            (int) attive,
            percentualeCompletate
        );
    }

    public StatisticheIntervallo getStatisticheIntervallo(LocalDate inizio, LocalDate fine, Integer projectId) {
        if (inizio == null || fine == null) {
            throw new IllegalArgumentException("Date non valide");
        }
        if (fine.isBefore(inizio)) {
            throw new IllegalArgumentException("Data fine deve essere successiva alla data inizio");
        }
        
        List<Activity> attivita = activityRepository.findByDateRange(inizio, fine);
        
        if (projectId != null) {
            attivita = attivita.stream()
                .filter(a -> a.getProject() != null && a.getProject().getId().equals(projectId))
                .toList();
        }
        
        int totaleOreStimate = calcolaTotaleOre(attivita, Activity::getStimaTempo);
        int totaleOreEffettive = calcolaTotaleOre(attivita, Activity::getDurataEffettiva);
        int oreMancanti = Math.max(0, totaleOreStimate - totaleOreEffettive);
        
        return new StatisticheIntervallo(
            attivita.size(),
            (int) attivita.stream().filter(Activity::isCompletata).count(),
            totaleOreStimate,
            totaleOreEffettive,
            oreMancanti
        );
    }

    private int calcolaTotaleOre(List<Activity> attivita, java.util.function.Function<Activity, String> getter) {
        return attivita.stream()
            .map(getter)
            .filter(tempo -> tempo != null && !tempo.isEmpty())
            .mapToInt(this::convertiTempoInMinuti)
            .sum();
    }

    private int convertiTempoInMinuti(String tempo) {
        if (tempo == null || tempo.isEmpty() || tempo.equals("00:00")) {
            return 0;
        }
        String[] parts = tempo.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public String formattaMinutiInOre(int minuti) {
        int ore = minuti / 60;
        int min = minuti % 60;
        return String.format("%02d:%02d", ore, min);
    }

    // ========== Inner Classes (DTO senza Lombok) ==========

    public static class StatisticheProgetti {
        private int totale;
        private int attivi;
        private int completati;
        private int vuoti;

        public StatisticheProgetti() {}

        public StatisticheProgetti(int totale, int attivi, int completati, int vuoti) {
            this.totale = totale;
            this.attivi = attivi;
            this.completati = completati;
            this.vuoti = vuoti;
        }

        public int getTotale() { return totale; }
        public void setTotale(int totale) { this.totale = totale; }

        public int getAttivi() { return attivi; }
        public void setAttivi(int attivi) { this.attivi = attivi; }

        public int getCompletati() { return completati; }
        public void setCompletati(int completati) { this.completati = completati; }

        public int getVuoti() { return vuoti; }
        public void setVuoti(int vuoti) { this.vuoti = vuoti; }
    }

    public static class StatisticheAttivita {
        private int totale;
        private int completate;
        private int nonCompletate;
        private int pianificate;
        private int nonPianificate;

        public StatisticheAttivita() {}

        public StatisticheAttivita(int totale, int completate, int nonCompletate, 
                                   int pianificate, int nonPianificate) {
            this.totale = totale;
            this.completate = completate;
            this.nonCompletate = nonCompletate;
            this.pianificate = pianificate;
            this.nonPianificate = nonPianificate;
        }

        public int getTotale() { return totale; }
        public void setTotale(int totale) { this.totale = totale; }

        public int getCompletate() { return completate; }
        public void setCompletate(int completate) { this.completate = completate; }

        public int getNonCompletate() { return nonCompletate; }
        public void setNonCompletate(int nonCompletate) { this.nonCompletate = nonCompletate; }

        public int getPianificate() { return pianificate; }
        public void setPianificate(int pianificate) { this.pianificate = pianificate; }

        public int getNonPianificate() { return nonPianificate; }
        public void setNonPianificate(int nonPianificate) { this.nonPianificate = nonPianificate; }
    }

    public static class StatisticheGiorno {
        private int totaleProgrammate;
        private int completate;
        private int attive;
        private double percentualeCompletate;

        public StatisticheGiorno() {}

        public StatisticheGiorno(int totaleProgrammate, int completate, int attive, 
                                 double percentualeCompletate) {
            this.totaleProgrammate = totaleProgrammate;
            this.completate = completate;
            this.attive = attive;
            this.percentualeCompletate = percentualeCompletate;
        }

        public int getTotaleProgrammate() { return totaleProgrammate; }
        public void setTotaleProgrammate(int totaleProgrammate) { this.totaleProgrammate = totaleProgrammate; }

        public int getCompletate() { return completate; }
        public void setCompletate(int completate) { this.completate = completate; }

        public int getAttive() { return attive; }
        public void setAttive(int attive) { this.attive = attive; }

        public double getPercentualeCompletate() { return percentualeCompletate; }
        public void setPercentualeCompletate(double percentualeCompletate) { this.percentualeCompletate = percentualeCompletate; }
    }

    public static class StatisticheIntervallo {
        private int totaleAttivita;
        private int completate;
        private int totaleOreStimate;
        private int totaleOreEffettive;
        private int oreMancanti;

        public StatisticheIntervallo() {}

        public StatisticheIntervallo(int totaleAttivita, int completate, 
                                     int totaleOreStimate, int totaleOreEffettive, 
                                     int oreMancanti) {
            this.totaleAttivita = totaleAttivita;
            this.completate = completate;
            this.totaleOreStimate = totaleOreStimate;
            this.totaleOreEffettive = totaleOreEffettive;
            this.oreMancanti = oreMancanti;
        }

        public int getTotaleAttivita() { return totaleAttivita; }
        public void setTotaleAttivita(int totaleAttivita) { this.totaleAttivita = totaleAttivita; }

        public int getCompletate() { return completate; }
        public void setCompletate(int completate) { this.completate = completate; }

        public int getTotaleOreStimate() { return totaleOreStimate; }
        public void setTotaleOreStimate(int totaleOreStimate) { this.totaleOreStimate = totaleOreStimate; }

        public int getTotaleOreEffettive() { return totaleOreEffettive; }
        public void setTotaleOreEffettive(int totaleOreEffettive) { this.totaleOreEffettive = totaleOreEffettive; }

        public int getOreMancanti() { return oreMancanti; }
        public void setOreMancanti(int oreMancanti) { this.oreMancanti = oreMancanti; }
    }
}
