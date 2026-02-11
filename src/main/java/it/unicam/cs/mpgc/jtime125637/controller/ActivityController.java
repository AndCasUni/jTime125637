package it.unicam.cs.mpgc.jtime125637.controller;

import it.unicam.cs.mpgc.jtime125637.model.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

/**
 * Controller per la gestione delle attività.
 * Gestisce le operazioni CRUD e la logica di business per le attività,
 * inclusa la validazione dei vincoli temporali e delle associazioni con i progetti.
 */
@NoArgsConstructor
public class ActivityController {
    private final ActivityRepositoryInterface activityRepository = new ActivityRepository();
    private final ProjectRepositoryInterface  projectRepository = new ProjectRepository();

    /**
     * Crea una nuova attività con nome, descrizione e stima del tempo.
     *
     * @param nome nome dell'attività
     * @param descrizione descrizione dell'attività
     * @param stimaTempo stima del tempo in formato "HH:MM" (da 00:05 a 24:00)
     * @throws IllegalArgumentException se il nome è vuoto o il formato del tempo non è valido
     */
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

    /**
     * Elimina un'attività dal database.
     * L'attività può essere eliminata solo se non è assegnata a un progetto,
     * non è pianificata e non è completata.
     *
     * @param id l'ID dell'attività da eliminare
     * @throws IllegalArgumentException se l'ID non è valido o l'attività non esiste
     * @throws IllegalStateException se l'attività non è eliminabile
     */
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

    /**
     * Completa un'attività registrando la durata effettiva.
     * Una volta completata, l'attività non può più essere eliminata.
     *
     * @param id l'ID dell'attività da completare
     * @param durataEffettiva la durata effettiva in formato "HH:MM" (da 00:05 a 24:00)
     * @throws IllegalArgumentException se l'ID non è valido, l'attività non esiste o il formato è errato
     * @throws IllegalStateException se l'attività è già completata
     */
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

    /**
     * Pianifica un'attività assegnando una data di esecuzione.
     * Una volta pianificata, l'attività non può più essere eliminata.
     * Verifica che il totale delle ore stimate per la data non superi le 24 ore.
     *
     * @param id l'ID dell'attività da pianificare
     * @param data la data di pianificazione
     * @throws IllegalArgumentException se l'ID o la data non sono validi, o se l'attività non esiste
     * @throws IllegalStateException se pianificare l'attività supererebbe le 24 ore per la data
     */
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

        // Verifica vincolo 24 ore
        if (!puoPianificareAttivita(data, activity.getStimaTempo(), id)) {
            int totaleMinuti = calcolaTotaleOreStimatePerData(data);
            int ore = totaleMinuti / 60;
            int minuti = totaleMinuti % 60;
            throw new IllegalStateException(
                    String.format("Impossibile pianificare: la data %s ha già %02d:%02d ore stimate. " +
                                    "Aggiungere questa attività supererebbe il limite di 24 ore.",
                            data, ore, minuti)
            );
        }

        Date dataSql = Date.valueOf(data);
        activity.setDataPianificazione(dataSql);
        activityRepository.update(activity);
    }

    /**
     * Associa un'attività a un progetto.
     * L'attività può essere assegnata solo a progetti attivi.
     * Una volta associata, l'attività non può più essere eliminata.
     *
     * @param activityId l'ID dell'attività da associare
     * @param projectId l'ID del progetto a cui associare l'attività
     * @throws IllegalArgumentException se gli ID non sono validi o le entità non esistono
     * @throws IllegalStateException se il progetto è completato
     */
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

    /**
     * Rimuove l'associazione di un'attività da un progetto.
     * L'attività diventa nuovamente eliminabile dopo la rimozione del progetto.
     *
     * @param activityId l'ID dell'attività da dissociare
     * @throws IllegalArgumentException se l'ID non è valido o l'attività non esiste
     */
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

    /**
     * Recupera tutte le attività ordinate per ID decrescente.
     *
     * @return lista di tutte le attività
     */
    public List<Activity> getTutteAttivita() {
        return activityRepository.findAll();
    }

    /**
     * Recupera tutte le attività eliminabili.
     * Un'attività è eliminabile se non è assegnata a un progetto,
     * non è pianificata e non è completata.
     *
     * @return lista delle attività eliminabili
     */
    public List<Activity> getAttivitaEliminabili() {
        return activityRepository.findEliminabili();
    }

    /**
     * Cerca attività applicando filtri multipli.
     *
     * @param id l'ID dell'attività (opzionale)
     * @param nome il nome o parte del nome dell'attività (opzionale)
     * @param projectId l'ID del progetto (opzionale)
     * @param completate se true, filtra solo le attività completate
     * @param pianificate se true, filtra solo le attività pianificate
     * @param assegnate se true, filtra solo le attività assegnate a un progetto
     * @param attive se true, filtra solo le attività non completate
     * @return lista delle attività che soddisfano i filtri
     */
    public List<Activity> cercaAttivita(Integer id, String nome, Integer projectId,
                                        boolean completate, boolean pianificate,
                                        boolean assegnate, boolean attive) {
        return activityRepository.findByFilters(id, nome, projectId, completate,
                pianificate, assegnate, attive);
    }

    /**
     * Recupera tutte le attività associate a un progetto specifico.
     *
     * @param projectId l'ID del progetto
     * @return lista delle attività del progetto
     * @throws IllegalArgumentException se l'ID del progetto non è valido
     */
    public List<Activity> getAttivitaPerProgetto(Integer projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("ID progetto non valido");
        }
        return activityRepository.findByProject(projectId);
    }

    /**
     * Recupera tutte le attività pianificate per una data specifica.
     *
     * @param data la data di pianificazione
     * @return lista delle attività pianificate per la data specificata
     * @throws IllegalArgumentException se la data non è valida
     */
    public List<Activity> getAttivitaPerData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data non valida");
        }
        return activityRepository.findByDate(data);
    }

    /**
     * Recupera un'attività per ID.
     *
     * @param id l'ID dell'attività
     * @return l'attività trovata o null se non esiste
     * @throws IllegalArgumentException se l'ID non è valido
     */
    public Activity getAttivitaById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID attività non valido");
        }
        return activityRepository.findById(id);
    }

    /**
     * Aggiorna un'attività esistente nel database.
     *
     * @param activity l'attività da aggiornare
     * @throws IllegalArgumentException se l'attività o il suo ID non sono validi
     */
    public void aggiornaAttivita(Activity activity) {
        if (activity == null || activity.getId() == null) {
            throw new IllegalArgumentException("Attività non valida");
        }
        activityRepository.update(activity);
    }

    /**
     * Calcola il totale delle ore stimate per una data specifica.
     * Somma tutte le stime delle attività pianificate per quella data.
     *
     * @param data la data da verificare
     * @return totale minuti stimati per quella data
     */
    public int calcolaTotaleOreStimatePerData(LocalDate data) {
        List<Activity> attivita = getAttivitaPerData(data);

        int totaleMinuti = 0;
        for (Activity a : attivita) {
            if (a.getStimaTempo() != null && !a.getStimaTempo().isEmpty()) {
                String[] parti = a.getStimaTempo().split(":");
                if (parti.length == 2) {
                    try {
                        int ore = Integer.parseInt(parti[0]);
                        int minuti = Integer.parseInt(parti[1]);
                        totaleMinuti += (ore * 60) + minuti;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        return totaleMinuti;
    }

    /**
     * Verifica se è possibile pianificare un'attività in una data
     * senza superare il limite di 24 ore totali.
     *
     * @param data la data target
     * @param stimaNuovaAttivita stima in formato "HH:mm" dell'attività da pianificare
     * @param activityIdDaEscludere ID attività da escludere dal calcolo (per modifiche)
     * @return true se pianificabile senza superare 24 ore, false altrimenti
     */
    public boolean puoPianificareAttivita(LocalDate data, String stimaNuovaAttivita, Integer activityIdDaEscludere) {
        List<Activity> attivita = getAttivitaPerData(data);

        int totaleMinuti = 0;
        for (Activity a : attivita) {
            if (activityIdDaEscludere != null && a.getId().equals(activityIdDaEscludere)) {
                continue;
            }

            if (a.getStimaTempo() != null && !a.getStimaTempo().isEmpty()) {
                String[] parti = a.getStimaTempo().split(":");
                if (parti.length == 2) {
                    try {
                        int ore = Integer.parseInt(parti[0]);
                        int minuti = Integer.parseInt(parti[1]);
                        totaleMinuti += (ore * 60) + minuti;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        if (stimaNuovaAttivita != null && !stimaNuovaAttivita.isEmpty()) {
            String[] parti = stimaNuovaAttivita.split(":");
            if (parti.length == 2) {
                try {
                    int ore = Integer.parseInt(parti[0]);
                    int minuti = Integer.parseInt(parti[1]);
                    totaleMinuti += (ore * 60) + minuti;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        return totaleMinuti <= 1440;
    }

    /**
     * Valida il formato di una stringa tempo.
     *
     * @param tempo la stringa da validare
     * @return true se il formato è valido (HH:MM), false altrimenti
     */
    private boolean validaTempoFormato(String tempo) {
        if (tempo == null || tempo.isEmpty()) {
            return false;
        }
        return tempo.matches("^([0-1]?[0-9]|2[0-4]):[0-5][0-9]$");
    }

    /**
     * Valida che il tempo sia compreso nel range consentito (5-1440 minuti).
     *
     * @param tempo la stringa tempo in formato "HH:MM"
     * @return true se il tempo è tra 00:05 e 24:00, false altrimenti
     */
    private boolean validaTempoRange(String tempo) {
        if (tempo == null) {
            return false;
        }
        String[] parts = tempo.split(":");
        try {
            int ore = Integer.parseInt(parts[0]);
            int minuti = Integer.parseInt(parts[1]);
            int totaleMinuti = (ore * 60) + minuti;
            return totaleMinuti >= 5 && totaleMinuti <= 1440;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
