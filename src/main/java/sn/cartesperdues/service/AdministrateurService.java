package sn.cartesperdues.service;

import sn.cartesperdues.entity.Administrateur;
import sn.cartesperdues.repository.AdministrateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AdministrateurService {

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Créer un nouvel administrateur
    @Transactional
    public Administrateur createAdministrateur(Administrateur administrateur) {
        // Vérifier si le username existe déjà
        if (administrateurRepository.existsByUsername(administrateur.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }

        // Hasher le mot de passe
        administrateur.setPasswordHash(passwordEncoder.encode(administrateur.getPasswordHash()));

        // Définir le rôle par défaut
        administrateur.setRole("ADMIN");

        return administrateurRepository.save(administrateur);
    }

    // Authentifier un administrateur
    public Optional<Administrateur> authenticate(String username, String password) {
        Optional<Administrateur> adminOpt = administrateurRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            Administrateur admin = adminOpt.get();
            if (passwordEncoder.matches(password, admin.getPasswordHash())) {
                return Optional.of(admin);
            }
        }

        return Optional.empty();
    }

    // Récupérer tous les administrateurs
    public List<Administrateur> getAllAdministrateurs() {
        return administrateurRepository.findAll();
    }

    // Trouver un administrateur par ID
    public Optional<Administrateur> getAdministrateurById(Long id) {
        return administrateurRepository.findById(id);
    }

    // Mettre à jour un administrateur
    @Transactional
    public Administrateur updateAdministrateur(Long id, Administrateur updatedAdmin) {
        Administrateur existingAdmin = administrateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        // Mettre à jour les champs
        if (updatedAdmin.getNomComplet() != null) {
            existingAdmin.setNomComplet(updatedAdmin.getNomComplet());
        }
        if (updatedAdmin.getEmail() != null) {
            existingAdmin.setEmail(updatedAdmin.getEmail());
        }
        if (updatedAdmin.getTelephone() != null) {
            existingAdmin.setTelephone(updatedAdmin.getTelephone());
        }
        // Mettre à jour le mot de passe si fourni
        if (updatedAdmin.getPasswordHash() != null && !updatedAdmin.getPasswordHash().isEmpty()) {
            existingAdmin.setPasswordHash(passwordEncoder.encode(updatedAdmin.getPasswordHash()));
        }

        return administrateurRepository.save(existingAdmin);
    }

    // Supprimer un administrateur
    @Transactional
    public void deleteAdministrateur(Long id) {
        // Empêcher la suppression du dernier admin
        long adminCount = administrateurRepository.count();
        if (adminCount <= 1) {
            throw new RuntimeException("Impossible de supprimer le dernier administrateur");
        }

        administrateurRepository.deleteById(id);
    }

    // Vérifier si un admin existe par username
    public boolean existsByUsername(String username) {
        return administrateurRepository.existsByUsername(username);
    }

    // Rechercher par nom complet
    public List<Administrateur> searchByNomComplet(String nomComplet) {
        return administrateurRepository.findByNomCompletContainingIgnoreCase(nomComplet);
    }
}