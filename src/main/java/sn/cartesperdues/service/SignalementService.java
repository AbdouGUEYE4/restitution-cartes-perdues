package sn.cartesperdues.service;

import org.jspecify.annotations.Nullable;
import sn.cartesperdues.dto.SignalementDTO;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.entity.StatutSignalement;
import sn.cartesperdues.repository.SignalementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private CarteService carteService;

    // ========== CRÉATION DE SIGNALEMENTS ==========

    public Signalement signalerCarte(Long carteId, String raison, String description, String emailSignaleur) {
        // Utilisez findCarteById() qui retourne directement Carte
        Carte carte = carteService.findCarteById(carteId);

        Signalement signalement = new Signalement();
        signalement.setCarte(carte);
        signalement.setRaison(raison);
        signalement.setDescription(description);
        signalement.setEmailSignaleur(emailSignaleur);
        signalement.setDateSignalement(LocalDateTime.now());
        signalement.setStatut(StatutSignalement.NOUVEAU);

        return signalementRepository.save(signalement);
    }

    // ========== LECTURE DE SIGNALEMENTS ==========

    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    public List<Signalement> getSignalementsByStatut(StatutSignalement statut) {
        return signalementRepository.findByStatut(statut);
    }

    public List<Signalement> getNouveauxSignalements() {
        return signalementRepository.findByStatutOrderByDateSignalementDesc(StatutSignalement.NOUVEAU);
    }

    public long countNouveauxSignalements() {
        return signalementRepository.countByStatut(StatutSignalement.NOUVEAU);
    }

    public Signalement getSignalementById(Long id) {
        return signalementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé avec ID: " + id));
    }

    public List<Signalement> getSignalementsByCarteId(Long carteId) {
        return signalementRepository.findByCarteId(carteId);
    }

    // ========== MISE À JOUR ==========

    public Signalement mettreAJourStatut(Long id, StatutSignalement nouveauStatut) {
        Signalement signalement = getSignalementById(id);
        signalement.setStatut(nouveauStatut);
        return signalementRepository.save(signalement);
    }

    // ========== SUPPRESSION ==========

    public void supprimerSignalement(Long id) {
        if (!signalementRepository.existsById(id)) {
            throw new RuntimeException("Signalement non trouvé avec ID: " + id);
        }
        signalementRepository.deleteById(id);
    }

    // ========== VÉRIFICATIONS ==========

    public boolean existeDejaSignalement(Long carteId, String emailSignaleur) {
        if (emailSignaleur == null || emailSignaleur.trim().isEmpty()) {
            return false;
        }

        List<Signalement> signalements = getSignalementsByCarteId(carteId);
        return signalements.stream()
                .anyMatch(s -> emailSignaleur.equalsIgnoreCase(s.getEmailSignaleur()));
    }

    public Map<String, Long> getStatistiquesParRaison() {
        List<Signalement> signalements = signalementRepository.findAll();

        return signalements.stream()
                .collect(Collectors.groupingBy(
                        Signalement::getRaison,
                        Collectors.counting()
                ));
    }

    // Dans SignalementService
    public void creerSignalement(SignalementDTO signalementDTO) {
        Carte carte = carteService.findCarteById(signalementDTO.getCarteId());

        Signalement signalement = new Signalement();
        signalement.setCarte(carte);
        signalement.setRaison(signalementDTO.getRaison());
        signalement.setDescription(signalementDTO.getDescription());
        signalement.setEmailSignaleur(signalementDTO.getEmailSignaleur());
        signalement.setStatut(StatutSignalement.NOUVEAU);

        signalementRepository.save(signalement);
    }
}