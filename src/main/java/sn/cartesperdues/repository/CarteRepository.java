package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.StatutCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CarteRepository extends JpaRepository<Carte, Long> {

    // Rechercher par nom (insensible à la casse)
    List<Carte> findByNomCompletContainingIgnoreCase(String nomComplet);

    // Rechercher par type de carte
    List<Carte> findByTypeCarte(String typeCarte);

    // Rechercher par lieu
    List<Carte> findByLieuTrouveContainingIgnoreCase(String lieu);

    // Rechercher par statut
    List<Carte> findByStatut(StatutCarte statut);

    // Rechercher par date de publication (après une certaine date)
    List<Carte> findByDatePublicationAfter(LocalDateTime date);

    // Recherche combinée (nom + type + lieu)
    List<Carte> findByNomCompletContainingIgnoreCaseAndTypeCarteAndLieuTrouveContainingIgnoreCase(
            String nomComplet, String typeCarte, String lieu);

    // Recherche pour le dashboard admin : cartes en attente de validation
    List<Carte> findByStatutOrderByDatePublicationAsc(StatutCarte statut);

    // Recherche pour l'accueil : cartes validées récentes
    List<Carte> findByStatutOrderByDatePublicationDesc(StatutCarte statut);

    // Recherche avancée avec @Query
    @Query("SELECT c FROM Carte c WHERE " +
            "LOWER(c.nomComplet) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.typeCarte) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lieuTrouve) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.numeroCarte) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Carte> searchByKeyword(@Param("keyword") String keyword);

    // Statistiques : compter par statut
    long countByStatut(StatutCarte statut);

    // Statistiques : compter par type de carte
    @Query("SELECT c.typeCarte, COUNT(c) FROM Carte c GROUP BY c.typeCarte")
    List<Object[]> countByTypeCarte();

    // Trouver les cartes avec beaucoup de contacts (potentiellement frauduleuses)
    List<Carte> findByContactCountGreaterThan(int threshold);
}