package sn.cartesperdues.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sn.cartesperdues.service.CarteService;

@Controller
public class PublicController {

    @Autowired
    private CarteService carteService;

    @GetMapping("/")
    public String home(Model model) {
        // Récupérer quelques cartes pour la page d'accueil
        var cartes = carteService.getAllCartes();
        model.addAttribute("cartes", cartes.size() > 6 ? cartes.subList(0, Math.min(6, cartes.size())) : cartes);
        return "public/index";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/";
    }

    // MODIFIÉ : Redirige vers la page de construction TEMPORAIREMENT
    @GetMapping("/publier")
    public String publierCarte(Model model) {
        model.addAttribute("pageTitle", "Publier une carte");
        return "public/signaler/form";
    }

    // UNE SEULE méthode /cartes - éliminez le doublon
    @GetMapping("/cartes")
    public String rechercherCartes(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String type,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String statut,
            Model model) {

        // Rechercher les cartes
        var cartes = carteService.rechercherCartes(search, type, statut);

        model.addAttribute("cartes", cartes);
        model.addAttribute("searchQuery", search);
        model.addAttribute("pageTitle", "Rechercher une carte");

        return "public/cartes";
    }

    @GetMapping("/a-propos")
    public String aPropos(Model model) {
        model.addAttribute("pageTitle", "À propos de nous");
        model.addAttribute("pageMessage", "Cette page sera disponible prochainement.");
        return "public/construction";
    }

    @GetMapping("/comment-ca-marche")
    public String commentCaMarche(Model model) {
        model.addAttribute("pageTitle", "Comment ça marche");
        model.addAttribute("pageMessage", "Cette page sera disponible prochainement.");
        return "public/construction";
    }

    @GetMapping("/contactez-nous")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contactez-nous");
        model.addAttribute("pageMessage", "Cette page sera disponible prochainement.");
        return "public/construction";
    }

    // Ajoutez ces endpoints pour éviter les erreurs 404
    @GetMapping("/contact")
    public String contactPage(Model model) {
        return contact(model);
    }

    @GetMapping("/signaler")
    public String signaler(Model model) {
        model.addAttribute("pageTitle", "Signaler un problème");
        model.addAttribute("pageMessage", "Cette fonctionnalité sera disponible prochainement.");
        return "public/construction";
    }

    @GetMapping("/api/**")
    public String apiRedirect() {
        return "redirect:/";
    }
}