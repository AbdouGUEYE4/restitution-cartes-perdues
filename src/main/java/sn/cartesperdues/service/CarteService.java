package sn.cartesperdues.service;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.StatutCarte;
import sn.cartesperdues.repository.CarteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarteService {

    @Autowired
    private CarteRepository carteRepository;

    // Retourne Optional
    public Optional<Carte> getCarteById(Long id) {
        return carteRepository.findById(id);
    }

    // Méthode qui lance une exception si non trouvé
    public Carte findCarteById(Long id) {
        return carteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée avec ID: " + id));
    }

    // Autres méthodes
    public List<Carte> getAllCartes() {
        return carteRepository.findAll();
    }

    public List<Carte> getCartesEnAttente() {
        return carteRepository.findByStatut(StatutCarte.EN_ATTENTE);
    }

    public List<Carte> getCartesValidees() {
        return carteRepository.findByStatut(StatutCarte.VALIDEE);
    }

    public List<Carte> getCartesRestituees() {
        return carteRepository.findByStatut(StatutCarte.RESTITUEE);
    }

    public long countCartesEnAttente() {
        return carteRepository.countByStatut(StatutCarte.EN_ATTENTE);
    }

    public long countCartesValidees() {
        return carteRepository.countByStatut(StatutCarte.VALIDEE);
    }

    public long countCartesRestituees() {
        return carteRepository.countByStatut(StatutCarte.RESTITUEE);
    }

    public Carte createCarte(Carte carte) {
        carte.setStatut(StatutCarte.EN_ATTENTE);
        carte.setContactCount(0);
        return carteRepository.save(carte);
    }

    public Carte updateCarte(Long id, Carte carteDetails) {
        Carte carte = findCarteById(id);

        // Mettre à jour les champs
        carte.setTypeCarte(carteDetails.getTypeCarte());
        carte.setNomComplet(carteDetails.getNomComplet());
        carte.setNumeroCarte(carteDetails.getNumeroCarte());
        carte.setDateNaissance(carteDetails.getDateNaissance());
        carte.setLieuTrouve(carteDetails.getLieuTrouve());
        carte.setTelephoneRamasseur(carteDetails.getTelephoneRamasseur());
        carte.setImageUrl(carteDetails.getImageUrl());
        carte.setStatut(carteDetails.getStatut());
        carte.setContactCount(carteDetails.getContactCount());
        carte.setRaisonSuppression(carteDetails.getRaisonSuppression());

        return carteRepository.save(carte);
    }

    public void deleteCarte(Long id) {
        Carte carte = findCarteById(id);
        carteRepository.delete(carte);
    }

    public Carte validerCarte(Long id) {
        Carte carte = findCarteById(id);
        carte.setStatut(StatutCarte.VALIDEE);
        return carteRepository.save(carte);
    }

    public Carte marquerCommeRestituee(Long id) {
        Carte carte = findCarteById(id);
        carte.setStatut(StatutCarte.RESTITUEE);
        return carteRepository.save(carte);
    }

    public void incrementerContactCount(Long id) {
        Carte carte = findCarteById(id);
        carte.setContactCount(carte.getContactCount() + 1);
        carteRepository.save(carte);
    }

    public List<Carte> rechercherCartes(String search, String type, String statut) {
        // Implémentation basique - à améliorer
        if (search != null && !search.trim().isEmpty()) {
            return carteRepository.findByNomCompletContainingIgnoreCase(search);
        }
        return carteRepository.findAll();
    }

    public Carte publierCarte(Carte carte, Object o) {
        // Définir les valeurs par défaut pour une nouvelle carte
        carte.setStatut(StatutCarte.EN_ATTENTE);
        carte.setContactCount(0);
        carte.setDatePublication(java.time.LocalDateTime.now());

        return carteRepository.save(carte);
    }

    // ==== MÉTHODE POUR LE TÉLÉPHONE ====
    public String getTelephoneComplet(Long carteId) {
        Carte carte = findCarteById(carteId);
        return formatTelephone(carte.getTelephoneRamasseur());
    }

    // Méthode utilitaire pour formater le téléphone
    private String formatTelephone(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            return "Non renseigné";
        }

        String numeroNettoye = telephone.replaceAll("[^0-9+]", "");

        // Format déjà international
        if (numeroNettoye.startsWith("+221") && numeroNettoye.length() == 13) {
            return numeroNettoye;
        }

        // Format avec 221
        if (numeroNettoye.startsWith("221") && numeroNettoye.length() == 12) {
            return "+" + numeroNettoye;
        }

        // Format local 9 chiffres (77, 76, 78, 70)
        if (numeroNettoye.matches("^[7][0-9]{8}$") && numeroNettoye.length() == 9) {
            return "+221" + numeroNettoye;
        }

        // Format local avec 0
        if (numeroNettoye.matches("^0[7][0-9]{8}$") && numeroNettoye.length() == 10) {
            return "+221" + numeroNettoye.substring(1);
        }

        // Retourner le numéro tel quel si aucun format reconnu
        return telephone;
    }
    // Statistiques par type
    public Map<String, Long> getStatistiquesParType() {
        // Implémentation basique
        List<Carte> cartes = getAllCartes();
        return cartes.stream()
                .collect(Collectors.groupingBy(Carte::getTypeCarte, Collectors.counting()));
    }
}