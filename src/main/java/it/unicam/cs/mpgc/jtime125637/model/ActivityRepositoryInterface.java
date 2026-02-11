package it.unicam.cs.mpgc.jtime125637.model;

import java.time.LocalDate;
import java.util.List;

public interface ActivityRepositoryInterface extends CrudRepository<Activity, Integer> {
    List<Activity> findByProject(Integer projectId);
    List<Activity> findByDate(LocalDate data);
    List<Activity> findByDateRange(LocalDate start, LocalDate end);
    List<Activity> findEliminabili();
    List<Activity> findByFilters(Integer id, String nome, Integer projectId,
                                 boolean completate, boolean pianificate,
                                 boolean assegnate, boolean attive);
}
