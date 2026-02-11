package it.unicam.cs.mpgc.jtime125637.model;

import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
public class ActivityRepository {

    /**
     * Salva una nuova attività nel database.
     *
     * @param activity l'attività da salvare
     * @throws RuntimeException se si verifica un errore durante il salvataggio
     */
    public void save(Activity activity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(activity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nel salvataggio dell'attività", e);
        }
    }

    /**
     * Aggiorna un'attività esistente nel database.
     *
     * @param activity l'attività da aggiornare
     * @throws RuntimeException se si verifica un errore durante l'aggiornamento
     */
    public void update(Activity activity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(activity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nell'aggiornamento dell'attività", e);
        }
    }

    /**
     * Elimina un'attività dal database.
     *
     * @param id l'ID dell'attività da eliminare
     * @throws RuntimeException se si verifica un errore durante l'eliminazione
     */
    public void delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Activity activity = session.get(Activity.class, id);
            if (activity != null) {
                session.delete(activity);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Errore nell'eliminazione dell'attività", e);
        }
    }

    /**
     * Cerca un'attività per ID.
     *
     * @param id l'ID dell'attività da cercare
     * @return l'attività trovata o null se non esiste
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public Activity findById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Activity activity = session.get(Activity.class, id);
            tx.commit();
            return activity;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Errore ricerca attività", e);
        }
    }


    /**
     * Recupera tutte le attività ordinate per ID decrescente.
     *
     * @return lista di tutte le attività
     * @throws RuntimeException se si verifica un errore durante il recupero
     */
    public List<Activity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Activity ORDER BY id DESC", Activity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero delle attività", e);
        }
    }

    /**
     * Cerca tutte le attività associate a un progetto specifico.
     *
     * @param projectId l'ID del progetto
     * @return lista delle attività associate al progetto
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public List<Activity> findByProject(Integer projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity WHERE project.id = :projectId ORDER BY id DESC",
                    Activity.class);
            query.setParameter("projectId", projectId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per progetto", e);
        }
    }

    /**
     * Cerca attività applicando filtri multipli.
     *
     * @param id l'ID dell'attività (opzionale)
     * @param nome il nome o parte del nome dell'attività (opzionale, case-insensitive)
     * @param projectId l'ID del progetto (opzionale)
     * @param completate se true, filtra solo le attività completate
     * @param pianificate se true, filtra solo le attività pianificate
     * @param assegnate se true, filtra solo le attività assegnate a un progetto
     * @param attive se true, filtra solo le attività non completate
     * @return lista delle attività che soddisfano i filtri specificati
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public List<Activity> findByFilters(Integer id, String nome, Integer projectId,
                                        boolean completate, boolean pianificate,
                                        boolean assegnate, boolean attive) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Activity WHERE 1=1");

            if (id != null) {
                hql.append(" AND id = :id");
            }
            if (nome != null && !nome.trim().isEmpty()) {
                hql.append(" AND LOWER(nome) LIKE LOWER(:nome)");
            }
            if (projectId != null) {
                hql.append(" AND project.id = :projectId");
            }
            if (completate) {
                hql.append(" AND durataEffettiva IS NOT NULL AND durataEffettiva != '00:00'");
            }
            if (pianificate) {
                hql.append(" AND dataPianificazione IS NOT NULL");
            }
            if (assegnate) {
                hql.append(" AND project IS NOT NULL");
            }
            if (attive) {
                hql.append(" AND (durataEffettiva IS NULL OR durataEffettiva = '00:00')");
            }

            hql.append(" ORDER BY id DESC");

            Query<Activity> query = session.createQuery(hql.toString(), Activity.class);

            if (id != null) {
                query.setParameter("id", id);
            }
            if (nome != null && !nome.trim().isEmpty()) {
                query.setParameter("nome", "%" + nome + "%");
            }
            if (projectId != null) {
                query.setParameter("projectId", projectId);
            }

            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nella ricerca filtrata", e);
        }
    }

    /**
     * Cerca tutte le attività pianificate per una data specifica.
     *
     * @param data la data di pianificazione
     * @return lista delle attività pianificate per la data specificata
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public List<Activity> findByDate(LocalDate data) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity WHERE dataPianificazione = :data ORDER BY id DESC",
                    Activity.class);
            query.setParameter("data", data);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per data", e);
        }
    }

    /**
     * Cerca tutte le attività pianificate in un intervallo di date.
     *
     * @param start data di inizio dell'intervallo
     * @param end data di fine dell'intervallo
     * @return lista delle attività pianificate nell'intervallo, ordinate per data
     * @throws RuntimeException se si verifica un errore durante la ricerca
     */
    public List<Activity> findByDateRange(LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity WHERE dataPianificazione BETWEEN :start AND :end ORDER BY dataPianificazione",
                    Activity.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per intervallo", e);
        }
    }

    /**
     * Recupera tutte le attività marcate come eliminabili.
     *
     * @return lista delle attività eliminabili
     * @throws RuntimeException se si verifica un errore durante il recupero
     */
    public List<Activity> findEliminabili() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Activity WHERE eliminabile = true ORDER BY id DESC",
                    Activity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività eliminabili", e);
        }
    }
}
