package com.uef.library.repository;

import com.uef.library.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByName(String name);

    Optional<Persona> findByActiveTrue();

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Persona p SET p.active = false")
    void deactivateAll();

    @Modifying
    @Query("UPDATE Persona p SET p.active = true WHERE p.name = :name")
    void activateByName(@Param("name") String name);
}