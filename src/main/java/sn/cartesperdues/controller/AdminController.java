package sn.cartesperdues.controller;

import sn.cartesperdues.dto.*;
import sn.cartesperdues.entity.Administrateur;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.service.AdministrateurService;
import sn.cartesperdues.service.CarteService;
import sn.cartesperdues.service.SignalementService;
import sn.cartesperdues.service.StatistiqueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdministrateurService administrateurService;

    @Autowired
    private CarteService carteService;

    @Autowired
    private SignalementService signalementService;

    @Autowired
    private StatistiqueService statistiqueService;

    // Page de login admin
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDTO", new AdminLoginDTO());
        return "admin/login";
    }

    // Traitement du login
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute AdminLoginDTO loginDTO,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/login";
        }

        try {
            var adminOpt = administrateurService.authenticate(loginDTO.getUsername(), loginDTO.getPassword());

            if (adminOpt.isPresent()) {
                // En production, on utiliserait une session Spring Security
                redirectAttributes.addFlashAttribute("adminId", adminOpt.get().getId());
                redirectAttributes.addFlashAttribute("adminName", adminOpt.get().getNomComplet());
                return "redirect:/admin/dashboard";
            } else {
                model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
                return "admin/login";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Erreur de connexion: " + e.getMessage());
            return "admin/login";
        }
    }

    // Dashboard admin
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Récupérer les statistiques
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setCartesEnAttente(carteService.countCartesEnAttente());
        stats.setCartesValidees(carteService.countCartesValidees());
        stats.setCartesRestituees(carteService.countCartesRestituees());
        stats.setNouveauxSignalements(signalementService.countNouveauxSignalements());

        // Cartes en attente de validation
        List<Carte> cartesEnAttente = carteService.getCartesEnAttente();

        // Nouveaux signalements
        List<Signalement> nouveauxSignalements = signalementService.getNouveauxSignalements();

        model.addAttribute("stats", stats);
        model.addAttribute("cartesEnAttente", cartesEnAttente);
        model.addAttribute("nouveauxSignalements", nouveauxSignalements);

        return "admin/dashboard";
    }

    // Gestion des cartes
    @GetMapping("/cartes")
    public String gestionCartes(@RequestParam(required = false) String statut, Model model) {
        List<Carte> cartes;

        if (statut != null && !statut.isEmpty()) {
            switch (statut.toUpperCase()) {
                case "EN_ATTENTE":
                    cartes = carteService.getCartesEnAttente();
                    break;
                case "VALIDEE":
                    cartes = carteService.getCartesValidees();
                    break;
                case "RESTITUEE":
                    // Implémenter cette méthode dans le service
                    // cartes = carteService.getCartesRestituees();
                    cartes = List.of();
                    break;
                default:
                    cartes = carteService.getCartesEnAttente();
            }
        } else {
            cartes = carteService.getCartesEnAttente();
        }

        model.addAttribute("cartes", cartes);
        model.addAttribute("statutFiltre", statut);
        return "admin/cartes";
    }

    // Valider une carte
    @PostMapping("/cartes/valider")
    public String validerCarte(@RequestParam Long carteId,
                               @RequestParam Long adminId,
                               RedirectAttributes redirectAttributes) {

        try {
            carteService.validerCarte(carteId, adminId);
            redirectAttributes.addFlashAttribute("success", "Carte validée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/cartes";
    }

    // Supprimer une carte
    @PostMapping("/cartes/supprimer")
    public String supprimerCarte(@RequestParam Long carteId,
                                 @RequestParam String raison,
                                 @RequestParam Long adminId,
                                 RedirectAttributes redirectAttributes) {

        try {
            carteService.supprimerCarte(carteId, raison, adminId);
            redirectAttributes.addFlashAttribute("success", "Carte supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/cartes";
    }

    // Gestion des signalements
    @GetMapping("/signalements")
    public String gestionSignalements(@RequestParam(required = false) String statut, Model model) {
        List<Signalement> signalements;

        if (statut != null && !statut.isEmpty()) {
            // Implémenter findByStatut dans le service
            // signalements = signalementService.getSignalementsByStatut(statut);
            signalements = signalementService.getAllSignalements();
        } else {
            signalements = signalementService.getAllSignalements();
        }

        model.addAttribute("signalements", signalements);
        model.addAttribute("statutFiltre", statut);
        return "admin/signalements";
    }

    // Traiter un signalement
    @PostMapping("/signalements/traiter")
    public String traiterSignalement(@RequestParam Long signalementId,
                                     @RequestParam String action,
                                     RedirectAttributes redirectAttributes) {

        try {
            signalementService.traiterSignalement(signalementId, action);
            redirectAttributes.addFlashAttribute("success", "Signalement traité avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/signalements";
    }

    // Gestion des administrateurs
    @GetMapping("/administrateurs")
    public String gestionAdministrateurs(Model model) {
        List<Administrateur> administrateurs = administrateurService.getAllAdministrateurs();
        model.addAttribute("administrateurs", administrateurs);
        model.addAttribute("adminDTO", new AdminDTO());
        return "admin/administrateurs";
    }

    // Ajouter un nouvel administrateur
    @PostMapping("/administrateurs/ajouter")
    public String ajouterAdministrateur(@Valid @ModelAttribute AdminDTO adminDTO,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez corriger les erreurs du formulaire");
            return "redirect:/admin/administrateurs";
        }

        if (!adminDTO.passwordsMatch()) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
            return "redirect:/admin/administrateurs";
        }

        try {
            Administrateur admin = new Administrateur();
            admin.setUsername(adminDTO.getUsername());
            admin.setPasswordHash(adminDTO.getPassword());
            admin.setNomComplet(adminDTO.getNomComplet());
            admin.setEmail(adminDTO.getEmail());
            admin.setTelephone(adminDTO.getTelephone());
            admin.setRole(adminDTO.getRole());

            administrateurService.createAdministrateur(admin);
            redirectAttributes.addFlashAttribute("success", "Administrateur ajouté avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/administrateurs";
    }

    // Supprimer un administrateur
    @PostMapping("/administrateurs/supprimer")
    public String supprimerAdministrateur(@RequestParam Long id,
                                          RedirectAttributes redirectAttributes) {

        try {
            administrateurService.deleteAdministrateur(id);
            redirectAttributes.addFlashAttribute("success", "Administrateur supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/administrateurs";
    }

    // Statistiques détaillées
    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        // Statistiques des 30 derniers jours
        var stats30Jours = statistiqueService.getStatistiques30Jours();
        var statsDuJour = statistiqueService.getStatistiquesDuJour();
        var totalRestitutions = statistiqueService.getTotalRestitutions();
        var totalPublications = statistiqueService.getTotalPublications();

        model.addAttribute("stats30Jours", stats30Jours);
        model.addAttribute("statsDuJour", statsDuJour);
        model.addAttribute("totalRestitutions", totalRestitutions);
        model.addAttribute("totalPublications", totalPublications);

        return "admin/statistiques";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("logout", true);
        return "redirect:/admin/login";
    }
}