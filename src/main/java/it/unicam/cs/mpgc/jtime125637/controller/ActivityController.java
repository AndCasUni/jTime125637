package it.unicam.cs.mpgc.jtime125637.controller;

import it.unicam.cs.mpgc.jtime125637.model.Activity;
import it.unicam.cs.mpgc.jtime125637.model.ActivityRepository;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import it.unicam.cs.mpgc.jtime125637.model.ProjectRepository;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
public class ActivityController {
    private final ActivityRepository activityRepository = new ActivityRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();

    public void creaAttivita(String nome, String descrizione, String stimaTempo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'attività è obbligatorio");
        }
        if (!validaTempoFormato(stimaTempo)) {
            throw new IllegalArgumentException("Formato tempo non valido (HH:MM)");
        }
        if (!validaTempoRange(stimaTempo)) {
            throw new IllegalArgumentException("Il tempo deve essere tra 00:05 e 24:00");
        }
        Activity activity = new Activity(nome, descrizione, stimaTempo);
        activityRepository.save(activity);
    }

    public void eliminaAttivita(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        Activity activity = activityRepository.findById(id);
        if (activity == null) {
            throw new IllegalArgumentException("Attività non trovata");
        }
        if (!activity.isEliminabile()) {
            throw new IllegalStateException("Impossibile eliminare: l'attività è assegnata, pianificata o completata");
        }
        activityRepository.delete(id);
    }

    public void completaAttivita(Integer id, String durataEffettiva) {
        if (id == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        if (!validaTempoFormato(durataEffettiva)) {
            throw new IllegalArgumentException("Formato durata non valido (HH:MM)");
        }
        if (!validaTempoRange(durataEffettiva)) {
            throw new IllegalArgumentException("La durata deve essere tra 00:05 e 24:00");
        }
        Activity activity = activityRepository.findById(id);
        if (activity == null) {
            throw new IllegalArgumentException("Attività non trovata");
        }
        if (activity.isCompletata()) {
            throw new IllegalStateException("L'attività è già completata");
        }
        activity.setDurataEffettiva(durataEffettiva);
        activityRepository.update(activity);
    }

    public void pianificaAttivita(Integer id, LocalDate data) {
        if (id == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data di pianificazione obbligatoria");
        }
        Activity activity = activityRepository.findById(id);
        if (activity == null) {
            throw new IllegalArgumentException("Attività non trovata");
        }
        activity.setDataPianificazione(data);
        activityRepository.update(activity);
    }

    public void associaProgetto(Integer activityId, Integer projectId) {
        if (activityId == null || projectId == null) {
            throw new IllegalArgumentException("ID attività e progetto obbligatori");
        }
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("Attività non trovata");
        }
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Progetto non trovato");
        }
        if (!project.isAttivo()) {
            throw new IllegalStateException("Impossibile assegnare: il progetto è completato");
        }
        activity.setProject(project);
        activityRepository.update(activity);
    }

    public void rimuoviProgetto(Integer activityId) {
        if (activityId == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("Attività non trovata");
        }
        activity.setProject(null);
        activity.setEliminabile(true);
        activityRepository.update(activity);
    }

    public List<Activity> getTutteAttivita() {
        return activityRepository.findAll();
    }

    public List<Activity> getAttivitaEliminabili() {
        return activityRepository.findEliminabili();
    }

    public List<Activity> cercaAttivita(Integer id, String nome, Integer projectId,
                                        boolean completate, boolean pianificate,
                                        boolean assegnate, boolean attive) {
        return activityRepository.findByFilters(id, nome, projectId, completate, 
                                                pianificate, assegnate, attive);
    }

    public List<Activity> getAttivitaPerProgetto(Integer projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        return activityRepository.findByProject(projectId);
    }

    public List<Activity> getAttivitaPerData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data non valida");
        }
        return activityRepository.findByDate(data);
    }

    public Activity getAttivitaById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        return activityRepository.findById(id);
    }

    public void aggiornaAttivita(Activity activity) {
        if (activity == null || activity.getId() == null) {
            throw new IllegalArgumentException("Attività non valida");
        }
        activityRepository.update(activity);
    }

    private boolean validaTempoFormato(String tempo) {
        if (tempo == null || tempo.isEmpty()) {
            return false;
        }
        return tempo.matches("^([0-1]?[0-9]|2[0-4]):[0-5][0-9]$");
    }

    private boolean validaTempoRange(String tempo) {
        if (tempo == null) {
            return false;
        }
        String[] parts = tempo.split(":");
        int ore = Integer.parseInt(parts[0]);
        int minuti = Integer.parseInt(parts[1]);
        int totaleMinuti = (ore * 60) + minuti;
        return totaleMinuti >= 5 && totaleMinuti <= 1440;
    }
}
