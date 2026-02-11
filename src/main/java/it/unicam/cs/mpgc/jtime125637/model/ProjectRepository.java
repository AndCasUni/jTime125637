package it.unicam.cs.mpgc.jtime125637.model;

import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@NoArgsConstructor
public class ProjectRepository implements ProjectRepositoryInterface{

    /**
     * Salva un nuovo progetto nel database.
     *
     * @param project il progetto da salvare
     * @throws RuntimeException se si verifica un errore durante il salvataggio
     */
    public void save(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(project);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nel salvataggio del progetto", e);
        }
    }

    /**
     * Aggiorna un progetto esistente nel database.
     *
     * @param project il progetto da aggiornare
     * @throws RuntimeException se si verifica un errore durante l'aggiornamento
     */
    public void update(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(project);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nell'aggiornamento del progetto", e);
        }
    }

    /**
     * Elimina un progetto dal database.
     *
     * @param id l'ID del progetto da eliminare
     * @throws RuntimeException se si verifica un errore durante l'eliminazione
     */
    public void delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Project project = session.get(Project.class, id);
            if (project != null) {
                session.delete(project);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nell'eliminazione del progetto", e);
        }
    }

    /**
     * Cerca un progetto per ID.
     *
     * @param id l'ID del progetto da cercare
     * @return il progetto trovato o null se non esiste
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public Project findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Project.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella ricerca del progetto", e);
        }
    }

    /**
     * Recupera tutti i progetti ordinati per ID decrescente.
     *
     * @return lista di tutti i progetti
     * @throws RuntimeException se si verifica un errore durante il recupero
     */
    public List<Project> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Project ORDER BY id DESC", Project.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero dei progetti", e);
        }
    }

    /**
     * Cerca tutti i progetti con uno stato specifico.
     * La ricerca è case-insensitive.
     *
     * @param stato lo stato dei progetti da cercare (es. "attivo", "completato")
     * @return lista dei progetti con lo stato specificato
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public List<Project> findByStato(String stato) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Project> query = session.createQuery(
                    "FROM Project WHERE LOWER(stato) = LOWER(:stato) ORDER BY id DESC",
                    Project.class);
            query.setParameter("stato", stato);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero progetti per stato", e);
        }
    }

    /**
     * Recupera tutti i progetti vuoti (senza attività associate).
     *
     * @return lista dei progetti senza attività
     * @throws RuntimeException se si verifica un errore durante il recupero
     */
    public List<Project> findEmpty() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Project p WHERE SIZE(p.activities) = 0 ORDER BY p.id DESC",
                    Project.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero progetti vuoti", e);
        }
    }

    /**
     * Recupera tutti i progetti eliminabili.
     * Un progetto è eliminabile se non contiene attività associate.
     *
     * @return lista dei progetti eliminabili
     * @throws RuntimeException se si verifica un errore durante il recupero
     */
    public List<Project> findEliminabili() {
        return findEmpty();
    }
}
