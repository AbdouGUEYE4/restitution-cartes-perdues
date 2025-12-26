package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.entity.StatutSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    // Trouver tous les signalements pour une carte
    List<Signalement> findByCarteId(Long carteId);

    // Trouver par statut
    List<Signalement> findByStatut(StatutSignalement statut);

    // Trouver les nouveaux signalements (pour le dashboard admin)
    List<Signalement> findByStatutOrderByDateSignalementAsc(StatutSignalement statut);

    // Compter les signalements par statut
    long countByStatut(StatutSignalement statut);

    // Vérifier si un email a déjà signalé cette carte
    boolean existsByCarteIdAndEmailSignaleur(Long carteId, String email);
}