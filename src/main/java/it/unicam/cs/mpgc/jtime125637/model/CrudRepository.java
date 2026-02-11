package it.unicam.cs.mpgc.jtime125637.model;

import java.util.List;

public interface CrudRepository<T, ID> {
    void save(T entity);
    void update(T entity);
    void delete(ID id);
    T findById(ID id);
    List<T> findAll();
}
