package sn.cartesperdues.service;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.entity.StatutSignalement;
import sn.cartesperdues.repository.CarteRepository;
import sn.cartesperdues.repository.SignalementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private CarteRepository carteRepository;

    // Créer un nouveau signalement
    @Transactional
    public Signalement creerSignalement(Signalement signalement) {
        // Vérifier si la carte existe
        Carte carte = carteRepository.findById(signalement.getCarte().getId())
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        signalement.setCarte(carte);
        signalement.setDateSignalement(LocalDateTime.now());
        signalement.setStatut(StatutSignalement.NOUVEAU);

        return signalementRepository.save(signalement);
    }

    // Traiter un signalement (par admin)
    @Transactional
    public Signalement traiterSignalement(Long signalementId, String action) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));

        switch (action.toUpperCase()) {
            case "RESOLU":
                signalement.setStatut(StatutSignalement.RESOLU);
                break;
            case "REJETE":
                signalement.setStatut(StatutSignalement.REJETE);
                break;
            case "EN_COURS":
                signalement.setStatut(StatutSignalement.EN_COURS);
                break;
            default:
                throw new IllegalArgumentException("Action invalide");
        }

        return signalementRepository.save(signalement);
    }

    // Récupérer tous les signalements
    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    // Récupérer les nouveaux signalements
    public List<Signalement> getNouveauxSignalements() {
        return signalementRepository.findByStatutOrderByDateSignalementAsc(StatutSignalement.NOUVEAU);
    }

    // Récupérer les signalements pour une carte
    public List<Signalement> getSignalementsPourCarte(Long carteId) {
        return signalementRepository.findByCarteId(carteId);
    }

    // Supprimer un signalement
    @Transactional
    public void supprimerSignalement(Long signalementId) {
        signalementRepository.deleteById(signalementId);
    }

    // Statistiques
    public long countNouveauxSignalements() {
        return signalementRepository.countByStatut(StatutSignalement.NOUVEAU);
    }

    public long countSignalementsResolus() {
        return signalementRepository.countByStatut(StatutSignalement.RESOLU);
    }
}