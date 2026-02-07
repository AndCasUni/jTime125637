package it.unicam.cs.mpgc.jtime125637.model;

import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
public class ActivityRepository {

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

    public Activity findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Activity.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella ricerca dell'attività", e);
        }
    }

    public List<Activity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Activity a ORDER BY a.id DESC", Activity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero delle attività", e);
        }
    }

    public List<Activity> findByProject(Integer projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity a WHERE a.project.id = :projectId ORDER BY a.id DESC",
                    Activity.class);
            query.setParameter("projectId", projectId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per progetto", e);
        }
    }

    public List<Activity> findByFilters(Integer id, String nome, Integer projectId,
                                        boolean completate, boolean pianificate,
                                        boolean assegnate, boolean attive) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Activity a WHERE 1=1");

            if (id != null) {
                hql.append(" AND a.id = :id");
            }
            if (nome != null && !nome.trim().isEmpty()) {
                hql.append(" AND LOWER(a.nome) LIKE LOWER(:nome)");
            }
            if (projectId != null) {
                hql.append(" AND a.project.id = :projectId");
            }
            if (completate) {
                hql.append(" AND a.durataEffettiva IS NOT NULL AND a.durataEffettiva != '00:00'");
            }
            if (pianificate) {
                hql.append(" AND a.dataPianificazione IS NOT NULL");
            }
            if (assegnate) {
                hql.append(" AND a.project IS NOT NULL");
            }
            if (attive) {
                hql.append(" AND (a.durataEffettiva IS NULL OR a.durataEffettiva = '00:00')");
            }

            hql.append(" ORDER BY a.id DESC");

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

    public List<Activity> findByDate(LocalDate data) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity a WHERE a.dataPianificazione = :data ORDER BY a.id DESC",
                    Activity.class);
            query.setParameter("data", data);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per data", e);
        }
    }

    public List<Activity> findByDateRange(LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Activity> query = session.createQuery(
                    "FROM Activity a WHERE a.dataPianificazione BETWEEN :start AND :end ORDER BY a.dataPianificazione",
                    Activity.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività per intervallo", e);
        }
    }

    public List<Activity> findEliminabili() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Activity a WHERE a.eliminabile = true ORDER BY a.id DESC",
                    Activity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero attività eliminabili", e);
        }
    }
}
