package sn.cartesperdues.controller;

import sn.cartesperdues.dto.*;
import sn.cartesperdues.entity.Administrateur;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.entity.StatutCarte;
import sn.cartesperdues.entity.StatutSignalement;
import sn.cartesperdues.service.AdministrateurService;
import sn.cartesperdues.service.CarteService;
import sn.cartesperdues.service.SignalementService;
import sn.cartesperdues.service.StatistiqueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // ==================== AUTHENTIFICATION ====================

    // Page de login (GET)
    // Page de login (GET)
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {

        // Si déjà connecté, rediriger vers le dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !"anonymousUser".equals(auth.getName())) {
            return "redirect:/admin/dashboard";
        }

        // Messages d'erreur/succès
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("logout", "Vous avez été déconnecté avec succès");
        }

        model.addAttribute("loginDTO", new AdminLoginDTO());
        return "login";  // ← ICI: "login" au lieu de "admin/login"
    }

    // Page d'accès refusé
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/access-denied";
    }

    // Récupérer l'admin connecté
    private Administrateur getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (username != null && !username.isEmpty() && !"anonymousUser".equals(username)) {
                return administrateurService.getAdministrateurByUsername(username);
            }
        }
        return null;
    }

    // Ajouter l'admin au model
    @ModelAttribute
    public void addAdminToModel(Model model) {
        String requestURI = org.springframework.web.context.request.RequestContextHolder
                .getRequestAttributes() != null ?
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder
                                .getRequestAttributes()).getRequest().getRequestURI() : "";

        if (!requestURI.contains("/admin/login") && !requestURI.contains("/admin/access-denied")) {
            Administrateur admin = getCurrentAdmin();
            if (admin != null) {
                model.addAttribute("adminName", admin.getNomComplet());
                model.addAttribute("adminId", admin.getId());
            }
        }
    }

    // ==================== DASHBOARD ====================

    @GetMapping("")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setCartesEnAttente(carteService.countCartesEnAttente());
        stats.setCartesValidees(carteService.countCartesValidees());
        stats.setCartesRestituees(carteService.countCartesRestituees());
        stats.setNouveauxSignalements(signalementService.countNouveauxSignalements());

        model.addAttribute("stats", stats);
        model.addAttribute("cartesEnAttente", carteService.getCartesEnAttente());
        model.addAttribute("nouveauxSignalements", signalementService.getNouveauxSignalements());

        return "admin/dashboard";
    }

    // ==================== GESTION DES CARTES ====================

    @GetMapping("/cartes")
    public String listCartes(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Carte> cartes = carteService.getAllCartes();
        model.addAttribute("cartes", cartes);
        model.addAttribute("totalCartes", cartes.size());

        return "admin/cartes/list";
    }

    @GetMapping("/cartes/en-attente")
    public String cartesEnAttente(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Carte> cartes = carteService.getCartesEnAttente();
        model.addAttribute("cartes", cartes);
        model.addAttribute("titre", "Cartes en attente de validation");

        return "admin/cartes/list";
    }

    @GetMapping("/cartes/validees")
    public String cartesValidees(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Carte> cartes = carteService.getCartesValidees();
        model.addAttribute("cartes", cartes);
        model.addAttribute("titre", "Cartes validées");

        return "admin/cartes/list";
    }

    @GetMapping("/cartes/restituees")
    public String cartesRestituees(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Carte> cartes = carteService.getCartesRestituees();
        model.addAttribute("cartes", cartes);
        model.addAttribute("titre", "Cartes restituées");

        return "admin/cartes/list";
    }

    @GetMapping("/cartes/{id}")
    public String detailCarte(@PathVariable Long id, Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            Carte carte = carteService.findCarteById(id);
            model.addAttribute("carte", carte);
            model.addAttribute("signalements", signalementService.getSignalementsByCarteId(id));
            return "admin/cartes/detail";
        } catch (Exception e) {
            return "redirect:/admin/cartes?error=Carte+non+trouvée";
        }
    }

    @PostMapping("/cartes/{id}/valider")
    public String validerCarte(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            carteService.validerCarte(id);
            redirectAttributes.addFlashAttribute("success", "Carte validée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/cartes/" + id;
    }

    @PostMapping("/cartes/{id}/restituee")
    public String marquerRestituee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            carteService.marquerCommeRestituee(id);
            redirectAttributes.addFlashAttribute("success", "Carte marquée comme restituée !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/cartes/" + id;
    }

    @GetMapping("/cartes/{id}/supprimer")
    public String showDeleteCarteForm(@PathVariable Long id, Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            Carte carte = carteService.findCarteById(id);
            model.addAttribute("carte", carte);
            return "admin/cartes/delete";
        } catch (Exception e) {
            return "redirect:/admin/cartes?error=Carte+non+trouvée";
        }
    }

    @PostMapping("/cartes/{id}/supprimer")
    public String deleteCarte(@PathVariable Long id,
                              @RequestParam(required = false) String raison,
                              RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            carteService.deleteCarte(id);
            redirectAttributes.addFlashAttribute("success", "Carte supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/cartes";
    }

    // ==================== GESTION DES SIGNALEMENTS ====================

    @GetMapping("/signalements")
    public String listSignalements(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Signalement> signalements = signalementService.getAllSignalements();
        model.addAttribute("signalements", signalements);
        model.addAttribute("totalSignalements", signalements.size());
        model.addAttribute("nouveauxCount", signalementService.countNouveauxSignalements());

        return "admin/signalements/list";
    }

    @GetMapping("/signalements/nouveaux")
    public String nouveauxSignalements(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Signalement> signalements = signalementService.getNouveauxSignalements();
        model.addAttribute("signalements", signalements);
        model.addAttribute("titre", "Nouveaux signalements");

        return "admin/signalements/list";
    }

    @GetMapping("/signalements/{id}")
    public String detailSignalement(@PathVariable Long id, Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            Signalement signalement = signalementService.getSignalementById(id);
            model.addAttribute("signalement", signalement);
            model.addAttribute("statuts", StatutSignalement.values());
            return "admin/signalements/detail";
        } catch (Exception e) {
            return "redirect:/admin/signalements?error=Signalement+non+trouvé";
        }
    }

    @PostMapping("/signalements/{id}/statut")
    public String updateStatutSignalement(@PathVariable Long id,
                                          @RequestParam StatutSignalement statut,
                                          RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            signalementService.mettreAJourStatut(id, statut);
            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/signalements/" + id;
    }

    @PostMapping("/signalements/{id}/supprimer")
    public String deleteSignalement(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            signalementService.supprimerSignalement(id);
            redirectAttributes.addFlashAttribute("success", "Signalement supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/signalements";
    }

    // ==================== GESTION DES ADMINISTRATEURS ====================

    @GetMapping("/administrateurs")
    public String listAdministrateurs(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        List<Administrateur> administrateurs = administrateurService.getAllAdministrateurs();
        model.addAttribute("administrateurs", administrateurs);

        return "admin/administrateurs/list";
    }

    @GetMapping("/administrateurs/creer")
    public String showCreateAdminForm(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("administrateur", new Administrateur());
        return "admin/administrateurs/create";
    }

    @PostMapping("/administrateurs/creer")
    public String createAdministrateur(@Valid @ModelAttribute("administrateur") Administrateur administrateur,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        if (result.hasErrors()) {
            return "admin/administrateurs/create";
        }

        try {
            administrateurService.createAdministrateur(administrateur);
            redirectAttributes.addFlashAttribute("success", "Administrateur créé avec succès !");
            return "redirect:/admin/administrateurs";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/administrateurs/create";
        }
    }

    @GetMapping("/administrateurs/{id}/modifier")
    public String showEditAdminForm(@PathVariable Long id, Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            // CORRECTION : Gérer l'Optional
            Administrateur admin = administrateurService.getAdministrateurById(id)
                    .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
            model.addAttribute("administrateur", admin);
            return "admin/administrateurs/edit";
        } catch (Exception e) {
            return "redirect:/admin/administrateurs?error=Admin+non+trouvé";
        }
    }

    @PostMapping("/administrateurs/{id}/modifier")
    public String updateAdministrateur(@PathVariable Long id,
                                       @Valid @ModelAttribute("administrateur") Administrateur administrateur,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        if (result.hasErrors()) {
            // Récupérer l'admin pour réafficher le formulaire
            try {
                Administrateur existingAdmin = administrateurService.getAdministrateurById(id)
                        .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
                model.addAttribute("administrateur", existingAdmin);
            } catch (Exception e) {
                // Ignorer
            }
            return "admin/administrateurs/edit";
        }

        try {
            administrateurService.updateAdministrateur(id, administrateur);
            redirectAttributes.addFlashAttribute("success", "Administrateur mis à jour avec succès !");
            return "redirect:/admin/administrateurs";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // Récupérer l'admin existant
            try {
                Administrateur existingAdmin = administrateurService.getAdministrateurById(id)
                        .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
                model.addAttribute("administrateur", existingAdmin);
            } catch (Exception ex) {
                // Ignorer
            }
            return "admin/administrateurs/edit";
        }
    }

    @PostMapping("/administrateurs/{id}/supprimer")
    public String deleteAdministrateur(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        try {
            administrateurService.deleteAdministrateur(id);
            redirectAttributes.addFlashAttribute("success", "Administrateur supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/administrateurs";
    }

    // ==================== PROFIL ADMIN ====================

    @GetMapping("/profil")
    public String showProfile(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        Administrateur admin = getCurrentAdmin();
        model.addAttribute("administrateur", admin);

        return "admin/profil";
    }

    @PostMapping("/profil/mot-de-passe")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        Administrateur admin = getCurrentAdmin();

        // Vérifier le mot de passe actuel
        if (!administrateurService.verifyPassword(currentPassword, admin.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe actuel est incorrect");
            return "redirect:/admin/profil";
        }

        // Vérifier la confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
            return "redirect:/admin/profil";
        }

        // Vérifier la force du mot de passe
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 8 caractères");
            return "redirect:/admin/profil";
        }

        // Changer le mot de passe
        administrateurService.resetPassword(admin.getId(), newPassword);

        redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès !");
        return "redirect:/admin/profil";
    }

    // ==================== STATISTIQUES ====================

    @GetMapping("/statistiques")
    public String showStatistics(Model model) {
        if (getCurrentAdmin() == null) {
            return "redirect:/admin/login";
        }

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setCartesEnAttente(carteService.countCartesEnAttente());
        stats.setCartesValidees(carteService.countCartesValidees());
        stats.setCartesRestituees(carteService.countCartesRestituees());
        stats.setNouveauxSignalements(signalementService.countNouveauxSignalements());

        model.addAttribute("stats", stats);
        model.addAttribute("totalAdmins", administrateurService.countActiveAdmins());
        model.addAttribute("cartesParType", carteService.getStatistiquesParType());
        model.addAttribute("signalementsParRaison", signalementService.getStatistiquesParRaison());

        return "admin/statistiques";
    }

    // ==================== DEBUG ====================

    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        StringBuilder sb = new StringBuilder();
        sb.append("=== SPRING SECURITY DEBUG ===\n");
        sb.append("Principal: ").append(auth.getPrincipal()).append("\n");
        sb.append("Name: ").append(auth.getName()).append("\n");
        sb.append("Authenticated: ").append(auth.isAuthenticated()).append("\n");
        sb.append("Authorities: ").append(auth.getAuthorities()).append("\n");
        return sb.toString();
    }

    @GetMapping("/test")
    @ResponseBody
    public String testAdmin() {
        Administrateur admin = getCurrentAdmin();
        if (admin != null) {
            return "Admin connecté: " + admin.getNomComplet() + " (" + admin.getUsername() + ")";
        } else {
            return "Aucun admin connecté";
        }
    }
}