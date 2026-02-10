package it.unicam.cs.mpgc.jtime125637.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    /**
     * Restituisce l'istanza singleton della SessionFactory di Hibernate.
     * Se non è ancora stata creata, la inizializza leggendo la configurazione
     * dal file hibernate.cfg.xml.
     *
     * @return l'istanza di SessionFactory
     * @throws ExceptionInInitializerError se si verifica un errore durante la creazione della SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration()
                        .configure("hibernate.cfg.xml")
                        .buildSessionFactory();
            } catch (Exception e) {
                System.err.println("Errore nella creazione della SessionFactory: " + e);
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    /**
     * Chiude la SessionFactory se è stata inizializzata e non è già chiusa.
     * Questo metodo dovrebbe essere chiamato alla chiusura dell'applicazione
     * per rilasciare correttamente le risorse.
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
