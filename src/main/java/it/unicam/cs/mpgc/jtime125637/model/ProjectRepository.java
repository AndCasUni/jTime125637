package it.unicam.cs.mpgc.jtime125637.model;

import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@NoArgsConstructor
public class ProjectRepository {

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

    public Project findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Project.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella ricerca del progetto", e);
        }
    }

    public List<Project> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Project ORDER BY id DESC", Project.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero dei progetti", e);
        }
    }

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

    public List<Project> findEmpty() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Project p WHERE SIZE(p.activities) = 0 ORDER BY p.id DESC", 
                    Project.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero progetti vuoti", e);
        }
    }

    public List<Project> findEliminabili() {
        return findEmpty();
    }
}
