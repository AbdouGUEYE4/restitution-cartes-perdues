package sn.cartesperdues.service;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.StatutCarte;
import sn.cartesperdues.repository.CarteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class CarteService {

    @Autowired
    private CarteRepository carteRepository;

    // Chemin de stockage des images (à configurer dans application.properties)
    private final String uploadDir = "uploads/cartes/";

    // Publier une nouvelle carte
    @Transactional
    public Carte publierCarte(Carte carte, MultipartFile imageFile) throws IOException {
        // Validation basique
        if (carte.getNomComplet() == null || carte.getNomComplet().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom complet est obligatoire");
        }
        if (carte.getTelephoneRamasseur() == null || carte.getTelephoneRamasseur().trim().isEmpty()) {
            throw new IllegalArgumentException("Le téléphone du ramasseur est obligatoire");
        }

        // Définir la date de publication
        carte.setDatePublication(LocalDateTime.now());
        carte.setStatut(StatutCarte.EN_ATTENTE);
        carte.setContactCount(0);

        // Gérer l'upload de l'image
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            carte.setImageUrl(fileName);
        }

        // Masquer partiellement le numéro pour l'affichage public
        // (Le numéro complet est stocké mais masqué dans les réponses)

        return carteRepository.save(carte);
    }

    // Valider une carte (par admin)
    @Transactional
    public Carte validerCarte(Long carteId, Long adminId) {
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        if (carte.getStatut() != StatutCarte.EN_ATTENTE) {
            throw new RuntimeException("Cette carte ne peut pas être validée");
        }

        carte.setStatut(StatutCarte.VALIDEE);
        return carteRepository.save(carte);
    }

    // Marquer une carte comme restituée
    @Transactional
    public Carte marquerCommeRestituee(Long carteId) {
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        carte.setStatut(StatutCarte.RESTITUEE);
        return carteRepository.save(carte);
    }

    // Supprimer une carte (soft delete par admin)
    @Transactional
    public Carte supprimerCarte(Long carteId, String raison, Long adminId) {
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        carte.setStatut(StatutCarte.SUPPRIMEE);
        carte.setRaisonSuppression(raison);
        return carteRepository.save(carte);
    }

    // Rechercher des cartes (pour le public)
    public List<Carte> rechercherCartes(String keyword, String typeCarte, String lieu) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return carteRepository.searchByKeyword(keyword.trim());
        }

        if (typeCarte != null && lieu != null) {
            return carteRepository.findByNomCompletContainingIgnoreCaseAndTypeCarteAndLieuTrouveContainingIgnoreCase(
                    "", typeCarte, lieu);
        }

        // Retourner les cartes validées récentes
        return carteRepository.findByStatutOrderByDatePublicationDesc(StatutCarte.VALIDEE);
    }

    // Récupérer les cartes en attente de validation (pour admin)
    public List<Carte> getCartesEnAttente() {
        return carteRepository.findByStatutOrderByDatePublicationAsc(StatutCarte.EN_ATTENTE);
    }

    // Récupérer les cartes validées
    public List<Carte> getCartesValidees() {
        return carteRepository.findByStatut(StatutCarte.VALIDEE);
    }

    // Récupérer une carte par ID (avec vérification de statut)
    public Optional<Carte> getCarteById(Long id) {
        Optional<Carte> carte = carteRepository.findById(id);

        // Ne retourner que les cartes validées ou en attente
        if (carte.isPresent() &&
                (carte.get().getStatut() == StatutCarte.VALIDEE ||
                        carte.get().getStatut() == StatutCarte.EN_ATTENTE)) {
            return carte;
        }

        return Optional.empty();
    }

    // Incrémenter le compteur de contacts
    @Transactional
    public void incrementerContactCount(Long carteId) {
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        carte.setContactCount(carte.getContactCount() + 1);
        carteRepository.save(carte);
    }

    // Obtenir le numéro complet (pour le contact)
    public String getTelephoneComplet(Long carteId) {
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        return carte.getTelephoneRamasseur();
    }

    // Masquer partiellement un numéro pour l'affichage public
    public String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 4) {
            return "***";
        }

        int longueur = telephone.length();
        String debut = telephone.substring(0, 2);
        String fin = telephone.substring(longueur - 2);

        return debut + "***" + fin;
    }

    // Sauvegarder une image
    private String saveImage(MultipartFile file) throws IOException {
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    // Statistiques
    public long countCartesEnAttente() {
        return carteRepository.countByStatut(StatutCarte.EN_ATTENTE);
    }

    public long countCartesValidees() {
        return carteRepository.countByStatut(StatutCarte.VALIDEE);
    }

    public long countCartesRestituees() {
        return carteRepository.countByStatut(StatutCarte.RESTITUEE);
    }
}