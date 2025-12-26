package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Statistique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatistiqueRepository extends JpaRepository<Statistique, Long> {

    // Trouver les statistiques pour une date spécifique
    Optional<Statistique> findByDateJour(LocalDate dateJour);

    // Trouver les statistiques pour une période
    List<Statistique> findByDateJourBetween(LocalDate startDate, LocalDate endDate);

    // Trouver les statistiques du mois en cours
    @Query("SELECT s FROM Statistique s WHERE YEAR(s.dateJour) = YEAR(CURRENT_DATE) AND MONTH(s.dateJour) = MONTH(CURRENT_DATE)")
    List<Statistique> findCurrentMonthStats();

    // Trouver les statistiques des 30 derniers jours
    @Query("SELECT s FROM Statistique s WHERE s.dateJour >= :date")
    List<Statistique> findLast30DaysStats(@org.springframework.data.repository.query.Param("date") LocalDate date);

    // Récupérer le total des cartes restituées
    @Query("SELECT SUM(s.cartesRestituees) FROM Statistique s")
    Long getTotalRestitutions();

    // Récupérer le total des cartes publiées
    @Query("SELECT SUM(s.cartesPubliees) FROM Statistique s")
    Long getTotalPublications();
}