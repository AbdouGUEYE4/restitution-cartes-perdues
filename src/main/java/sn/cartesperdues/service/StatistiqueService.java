package sn.cartesperdues.service;

import sn.cartesperdues.entity.Statistique;
import sn.cartesperdues.repository.StatistiqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StatistiqueService {

    @Autowired
    private StatistiqueRepository statistiqueRepository;

    // Version simplifiée - retourne Statistique
    @Transactional
    public Statistique mettreAJourStatistiques() {
        LocalDate aujourdhui = LocalDate.now();
        Optional<Statistique> statsOpt = statistiqueRepository.findByDateJour(aujourdhui);

        Statistique stats;
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
            // Incrémenter les compteurs existants
            stats.setCartesPubliees(stats.getCartesPubliees() + 0); // À adapter
            stats.setContactsEtablis(stats.getContactsEtablis() + 0); // À adapter
        } else {
            stats = new Statistique();
            stats.setDateJour(aujourdhui);
            // Initialiser avec des valeurs par défaut
            stats.setCartesPubliees(0);
            stats.setCartesValidees(0);
            stats.setCartesRestituees(0);
            stats.setSignalementsRecus(0);
            stats.setContactsEtablis(0);
            stats.setNouveauxUtilisateurs(0);
        }

        return statistiqueRepository.save(stats);
    }

    // Version avec seulement la date - ne retourne rien (void)
    @Transactional
    public void mettreAJourStatistiquesSimple() {
        LocalDate aujourdhui = LocalDate.now();
        Optional<Statistique> statsOpt = statistiqueRepository.findByDateJour(aujourdhui);

        if (statsOpt.isEmpty()) {
            // Créer une nouvelle entrée pour aujourd'hui avec des zéros
            Statistique stats = new Statistique();
            stats.setDateJour(aujourdhui);
            statistiqueRepository.save(stats);
        }
        // Sinon, ne rien faire - on garde les statistiques existantes
    }

    // Récupérer les statistiques du jour
    public Statistique getStatistiquesDuJour() {
        LocalDate aujourdhui = LocalDate.now();
        return statistiqueRepository.findByDateJour(aujourdhui)
                .orElse(new Statistique(aujourdhui));
    }

    // Récupérer les statistiques des 30 derniers jours
    public List<Statistique> getStatistiques30Jours() {
        LocalDate dateDebut = LocalDate.now().minusDays(30);
        LocalDate dateFin = LocalDate.now();
        return statistiqueRepository.findByDateJourBetween(dateDebut, dateFin);
    }

    // Obtenir le total des restitutions
    public Long getTotalRestitutions() {
        Long total = statistiqueRepository.getTotalRestitutions();
        return total != null ? total : 0L;
    }

    // Obtenir le total des publications
    public Long getTotalPublications() {
        Long total = statistiqueRepository.getTotalPublications();
        return total != null ? total : 0L;
    }
}