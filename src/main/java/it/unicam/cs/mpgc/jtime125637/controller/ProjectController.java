package it.unicam.cs.mpgc.jtime125637.controller;

import it.unicam.cs.mpgc.jtime125637.model.Project;
import it.unicam.cs.mpgc.jtime125637.model.ProjectRepository;
import it.unicam.cs.mpgc.jtime125637.model.ActivityRepository;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class ProjectController {
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final ActivityRepository activityRepository = new ActivityRepository();

    public void creaProgetto(String nome, String descrizione) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del progetto è obbligatorio");
        }
        Project project = new Project(nome, descrizione);
        projectRepository.save(project);
    }

    public void eliminaProgetto(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        Project project = projectRepository.findById(id);
        if (project == null) {
            throw new IllegalArgumentException("Progetto non trovato");
        }
        if (!project.isEmpty()) {
            throw new IllegalStateException("Impossibile eliminare: il progetto ha attività assegnate");
        }
        projectRepository.delete(id);
    }

    public void chiudiProgetto(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        Project project = projectRepository.findById(id);
        if (project == null) {
            throw new IllegalArgumentException("Progetto non trovato");
        }
        if (project.isEmpty()) {
            throw new IllegalStateException("Impossibile chiudere: il progetto è vuoto");
        }
        if (!project.tutteAttivitaTerminate()) {
            throw new IllegalStateException("Impossibile chiudere: ci sono attività non completate");
        }
        project.setStato("completato");
        projectRepository.update(project);
    }

    public void riaperiProgetto(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        Project project = projectRepository.findById(id);
        if (project == null) {
            throw new IllegalArgumentException("Progetto non trovato");
        }
        project.setStato("attivo");
        projectRepository.update(project);
    }

    public List<Project> getTuttiProgetti() {
        return projectRepository.findAll();
    }

    public List<Project> getProgettiAttivi() {
        return projectRepository.findByStato("attivo");
    }

    public List<Project> getProgettiCompletati() {
        return projectRepository.findByStato("completato");
    }

    public List<Project> getProgettiVuoti() {
        return projectRepository.findEmpty();
    }

    public List<Project> getProgettiEliminabili() {
        return projectRepository.findEliminabili();
    }

    public Project getProgettoById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        return projectRepository.findById(id);
    }

    public void aggiornaProgetto(Project project) {
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException("Progetto non valido");
        }
        projectRepository.update(project);
    }
}
