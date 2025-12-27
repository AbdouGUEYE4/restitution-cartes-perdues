package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.entity.StatutSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    // Trouver les signalements par statut
    List<Signalement> findByStatut(StatutSignalement statut);

    // Trouver les signalements par carte
    List<Signalement> findByCarteId(Long carteId);

    // Compter les signalements par statut
    long countByStatut(StatutSignalement statut);

    // Trouver les nouveaux signalements
    List<Signalement> findByStatutOrderByDateSignalementDesc(StatutSignalement statut);
}