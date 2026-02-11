package it.unicam.cs.mpgc.jtime125637.model;

import java.util.List;

public interface ProjectRepositoryInterface extends CrudRepository<Project, Integer> {
    List<Project> findByStato(String stato);
    List<Project> findEmpty();
    List<Project> findEliminabili();
}
