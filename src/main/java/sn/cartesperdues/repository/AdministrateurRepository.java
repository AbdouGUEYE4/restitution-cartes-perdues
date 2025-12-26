package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    // Trouver un admin par username
    Optional<Administrateur> findByUsername(String username);

    // Vérifier si un username existe déjà
    boolean existsByUsername(String username);

    // Trouver par email
    Optional<Administrateur> findByEmail(String email);

    // Rechercher par nom complet (LIKE)
    java.util.List<Administrateur> findByNomCompletContainingIgnoreCase(String nomComplet);
}