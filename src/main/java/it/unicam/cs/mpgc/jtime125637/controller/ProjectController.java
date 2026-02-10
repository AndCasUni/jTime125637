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

    /**
     * Crea un nuovo progetto con nome e descrizione.
     * Il progetto viene creato con stato "attivo".
     *
     * @param nome nome del progetto
     * @param descrizione descrizione del progetto
     * @throws IllegalArgumentException se il nome è vuoto o null
     */
    public void creaProgetto(String nome, String descrizione) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del progetto è obbligatorio");
        }
        Project project = new Project(nome, descrizione);
        projectRepository.save(project);
    }

    /**
     * Elimina un progetto dal database.
     * Un progetto può essere eliminato solo se non ha attività assegnate.
     *
     * @param id l'ID del progetto da eliminare
     * @throws IllegalArgumentException se l'ID non è valido o il progetto non esiste
     * @throws IllegalStateException se il progetto ha attività assegnate
     */
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

    /**
     * Chiude un progetto impostando il suo stato a "completato".
     * Un progetto può essere chiuso solo se:
     * - non è vuoto (ha almeno un'attività)
     * - tutte le attività sono state completate
     *
     * @param id l'ID del progetto da chiudere
     * @throws IllegalArgumentException se l'ID non è valido o il progetto non esiste
     * @throws IllegalStateException se il progetto è vuoto o ha attività non completate
     */
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

    /**
     * Riapre un progetto completato impostando il suo stato a "attivo".
     * Permette di aggiungere nuove attività o modificare il progetto.
     *
     * @param id l'ID del progetto da riaprire
     * @throws IllegalArgumentException se l'ID non è valido o il progetto non esiste
     */
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

    /**
     * Recupera tutti i progetti ordinati per ID decrescente.
     *
     * @return lista di tutti i progetti
     */
    public List<Project> getTuttiProgetti() {
        return projectRepository.findAll();
    }

    /**
     * Recupera tutti i progetti con stato "attivo".
     *
     * @return lista dei progetti attivi
     */
    public List<Project> getProgettiAttivi() {
        return projectRepository.findByStato("attivo");
    }

    /**
     * Recupera tutti i progetti con stato "completato".
     *
     * @return lista dei progetti completati
     */
    public List<Project> getProgettiCompletati() {
        return projectRepository.findByStato("completato");
    }

    /**
     * Recupera tutti i progetti vuoti (senza attività assegnate).
     *
     * @return lista dei progetti vuoti
     */
    public List<Project> getProgettiVuoti() {
        return projectRepository.findEmpty();
    }

    /**
     * Recupera tutti i progetti eliminabili.
     * Un progetto è eliminabile solo se non ha attività assegnate.
     *
     * @return lista dei progetti eliminabili
     */
    public List<Project> getProgettiEliminabili() {
        return projectRepository.findEliminabili();
    }

    /**
     * Recupera un progetto per ID.
     *
     * @param id l'ID del progetto
     * @return il progetto trovato o null se non esiste
     * @throws IllegalArgumentException se l'ID non è valido
     */
    public Project getProgettoById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        return projectRepository.findById(id);
    }

    /**
     * Aggiorna un progetto esistente nel database.
     *
     * @param project il progetto da aggiornare
     * @throws IllegalArgumentException se il progetto o il suo ID non sono validi
     */
    public void aggiornaProgetto(Project project) {
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException("Progetto non valido");
        }
        projectRepository.update(project);
    }
}
