package it.unicam.cs.mpgc.jtime125637.controller;

import it.unicam.cs.mpgc.jtime125637.model.Activity;
import it.unicam.cs.mpgc.jtime125637.model.ActivityRepository;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import it.unicam.cs.mpgc.jtime125637.model.ProjectRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
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

    @Data
    @NoArgsConstructor
    public static class StatisticheProgetti {
        private int totale;
        private int attivi;
        private int completati;
        private int vuoti;

        public StatisticheProgetti(int totale, int attivi, int completati, int vuoti) {
            this.totale = totale;
            this.attivi = attivi;
            this.completati = completati;
            this.vuoti = vuoti;
        }
    }

    @Data
    @NoArgsConstructor
    public static class StatisticheAttivita {
        private int totale;
        private int completate;
        private int nonCompletate;
        private int pianificate;
        private int nonPianificate;

        public StatisticheAttivita(int totale, int completate, int nonCompletate, 
                                   int pianificate, int nonPianificate) {
            this.totale = totale;
            this.completate = completate;
            this.nonCompletate = nonCompletate;
            this.pianificate = pianificate;
            this.nonPianificate = nonPianificate;
        }
    }

    @Data
    @NoArgsConstructor
    public static class StatisticheGiorno {
        private int totaleProgrammate;
        private int completate;
        private int attive;
        private double percentualeCompletate;

        public StatisticheGiorno(int totaleProgrammate, int completate, int attive, 
                                 double percentualeCompletate) {
            this.totaleProgrammate = totaleProgrammate;
            this.completate = completate;
            this.attive = attive;
            this.percentualeCompletate = percentualeCompletate;
        }
    }

    @Data
    @NoArgsConstructor
    public static class StatisticheIntervallo {
        private int totaleAttivita;
        private int completate;
        private int totaleOreStimate;
        private int totaleOreEffettive;
        private int oreMancanti;

        public StatisticheIntervallo(int totaleAttivita, int completate, 
                                     int totaleOreStimate, int totaleOreEffettive, 
                                     int oreMancanti) {
            this.totaleAttivita = totaleAttivita;
            this.completate = completate;
            this.totaleOreStimate = totaleOreStimate;
            this.totaleOreEffettive = totaleOreEffettive;
            this.oreMancanti = oreMancanti;
        }
    }
}
