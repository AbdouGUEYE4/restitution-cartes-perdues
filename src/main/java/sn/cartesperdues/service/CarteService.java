package sn.cartesperdues.service;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.StatutCarte;
import sn.cartesperdues.repository.CarteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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

    public List<Carte> rechercherCarteSecurise(String numeroCarte, String nomComplet, LocalDate dateNaissance) {
        System.out.println("=== RECHERCHE SÉCURISÉE SERVICE ===");

        // Recherche par numéro de carte (le plus précis)
        if (numeroCarte != null && !numeroCarte.trim().isEmpty()) {
            String numeroClean = numeroCarte.trim().replaceAll("\\s", "");
            List<Carte> resultats = carteRepository.findByNumeroCarteContaining(numeroClean);

            // Si date de naissance fournie, filtrer
            if (dateNaissance != null && !resultats.isEmpty()) {
                resultats = resultats.stream()
                        .filter(c -> c.getDateNaissance() != null && c.getDateNaissance().equals(dateNaissance))
                        .collect(Collectors.toList());
            }

            return resultats;
        }

        // Recherche par nom complet (avec date de naissance obligatoire pour plus de sécurité)
        if (nomComplet != null && !nomComplet.trim().isEmpty()) {
            if (dateNaissance == null) {
                throw new IllegalArgumentException("La date de naissance est requise pour une recherche par nom.");
            }

            // Recherche approximative du nom
            String nomClean = nomComplet.trim().toLowerCase();
            List<Carte> toutesCartes = carteRepository.findAll();

            // Filtrer par nom (contient, insensible à la casse) ET date de naissance
            return toutesCartes.stream()
                    .filter(c -> {
                        boolean nomMatch = c.getNomComplet() != null &&
                                c.getNomComplet().toLowerCase().contains(nomClean);
                        boolean dateMatch = c.getDateNaissance() != null &&
                                c.getDateNaissance().equals(dateNaissance);
                        return nomMatch && dateMatch;
                    })
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
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
        System.out.println("=== SERVICE RECHERCHE ===");
        System.out.println("Recherche: " + search);
        System.out.println("Type: " + type);
        System.out.println("Statut: " + statut);

        List<Carte> resultats = new ArrayList<>();

        // Si aucun paramètre, retourner toutes les cartes
        if ((search == null || search.trim().isEmpty())
                && (type == null || type.trim().isEmpty())
                && (statut == null || statut.trim().isEmpty())) {

            resultats = carteRepository.findAll();
            System.out.println("Aucun filtre - retourne toutes les cartes: " + resultats.size());
        }
        // Recherche par mot-clé
        else if (search != null && !search.trim().isEmpty()) {
            resultats = carteRepository.findByNomCompletContainingIgnoreCase(search);
            System.out.println("Recherche par nom: " + resultats.size() + " résultats");
        }
        // Recherche par type
        else if (type != null && !type.trim().isEmpty()) {
            resultats = carteRepository.findByTypeCarte(type);
            System.out.println("Recherche par type: " + resultats.size() + " résultats");
        }
        // Recherche par statut
        else if (statut != null && !statut.trim().isEmpty()) {
            try {
                StatutCarte statutEnum = StatutCarte.valueOf(statut);
                resultats = carteRepository.findByStatut(statutEnum);
                System.out.println("Recherche par statut: " + resultats.size() + " résultats");
            } catch (Exception e) {
                System.out.println("Statut invalide: " + statut);
                resultats = carteRepository.findAll();
            }
        }

        return resultats;
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
    public Carte saveCarte(Carte carte) {
        return carteRepository.save(carte);
    }
}