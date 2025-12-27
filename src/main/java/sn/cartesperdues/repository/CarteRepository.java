package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.StatutCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarteRepository extends JpaRepository<Carte, Long> {

    // ==== REQUÊTES DE RECHERCHE ====

    // Trouver par nom complet
    List<Carte> findByNomCompletContainingIgnoreCase(String nomComplet);

    // Trouver par numéro de carte
    List<Carte> findByNumeroCarteContaining(String numeroCarte);

    // Trouver par type de carte
    List<Carte> findByTypeCarte(String typeCarte);

    // Trouver par statut
    List<Carte> findByStatut(StatutCarte statut);

    // Trouver par lieu
    List<Carte> findByLieuTrouveContainingIgnoreCase(String lieuTrouve);

    // Compter par statut
    long countByStatut(StatutCarte statut);

    // Trouver les dernières cartes publiées
    List<Carte> findTop10ByOrderByDatePublicationDesc();

    // Trouver par téléphone
    List<Carte> findByTelephoneRamasseurContaining(String telephone);

    // ==== REQUÊTES PERSONNALISÉES (si besoin) ====

    // Requête JPQL pour formater le téléphone
    @Query("SELECT CASE " +
            "WHEN c.telephoneRamasseur IS NULL OR c.telephoneRamasseur = '' THEN 'Non renseigné' " +
            "WHEN c.telephoneRamasseur LIKE '+221%' THEN c.telephoneRamasseur " +
            "WHEN c.telephoneRamasseur LIKE '221%' THEN CONCAT('+', c.telephoneRamasseur) " +
            "WHEN LENGTH(c.telephoneRamasseur) = 9 THEN CONCAT('+221', c.telephoneRamasseur) " +
            "WHEN LENGTH(c.telephoneRamasseur) = 10 AND c.telephoneRamasseur LIKE '0%' " +
            "THEN CONCAT('+221', SUBSTRING(c.telephoneRamasseur, 2)) " +
            "ELSE c.telephoneRamasseur END " +
            "FROM Carte c WHERE c.id = :carteId")
    Optional<String> findFormattedTelephone(@Param("carteId") Long carteId);

    // Requête native SQL (alternative)
    @Query(value = "SELECT " +
            "CASE " +
            "WHEN telephone_ramasseur IS NULL OR telephone_ramasseur = '' THEN 'Non renseigné' " +
            "WHEN telephone_ramasseur LIKE '+221%' THEN telephone_ramasseur " +
            "WHEN telephone_ramasseur LIKE '221%' THEN CONCAT('+', telephone_ramasseur) " +
            "WHEN LENGTH(telephone_ramasseur) = 9 THEN CONCAT('+221', telephone_ramasseur) " +
            "WHEN LENGTH(telephone_ramasseur) = 10 AND telephone_ramasseur LIKE '0%' " +
            "THEN CONCAT('+221', SUBSTRING(telephone_ramasseur, 2)) " +
            "ELSE telephone_ramasseur END " +
            "FROM cartes WHERE id = :carteId",
            nativeQuery = true)
    Optional<String> findFormattedTelephoneNative(@Param("carteId") Long carteId);
}