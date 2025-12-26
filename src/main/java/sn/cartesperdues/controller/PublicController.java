package sn.cartesperdues.controller;

import sn.cartesperdues.dto.*;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Contact;
import sn.cartesperdues.entity.Signalement;
import sn.cartesperdues.service.CarteService;
import sn.cartesperdues.service.ContactService;
import sn.cartesperdues.service.SignalementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class PublicController {

    @Autowired
    private CarteService carteService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private SignalementService signalementService;

    // Liste des types de cartes disponibles
    private final List<String> TYPES_CARTE = Arrays.asList(
            "CNI (Carte Nationale d'Identité)",
            "Permis de conduire",
            "Carte scolaire/universitaire",
            "Badge professionnel",
            "Carte de sécurité sociale",
            "Carte bancaire",
            "Autre"
    );

    // Page d'accueil avec recherche
    @GetMapping("")
    public String home(@ModelAttribute CarteRechercheDTO rechercheDTO, Model model) {
        List<Carte> cartes;

        if (rechercheDTO.getKeyword() != null && !rechercheDTO.getKeyword().isEmpty() ||
                rechercheDTO.getTypeCarte() != null && !rechercheDTO.getTypeCarte().isEmpty() ||
                rechercheDTO.getLieu() != null && !rechercheDTO.getLieu().isEmpty()) {

            // Recherche avec critères
            cartes = carteService.rechercherCartes(
                    rechercheDTO.getKeyword(),
                    rechercheDTO.getTypeCarte(),
                    rechercheDTO.getLieu()
            );
        } else {
            // Cartes récentes validées
            cartes = carteService.getCartesValidees();
        }

        model.addAttribute("cartes", cartes);
        model.addAttribute("typesCarte", TYPES_CARTE);
        model.addAttribute("rechercheDTO", rechercheDTO);
        return "public/index";
    }

    // Page de publication d'une carte trouvée
    @GetMapping("/publier")
    public String showPublierForm(Model model) {
        model.addAttribute("carteDTO", new CartePublicationDTO());
        model.addAttribute("typesCarte", TYPES_CARTE);
        return "public/publier";
    }

    // Traitement de la publication
    @PostMapping("/publier")
    public String publierCarte(@Valid @ModelAttribute CartePublicationDTO carteDTO,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("typesCarte", TYPES_CARTE);
            return "public/publier";
        }

        try {
            // Convertir DTO en Entity
            Carte carte = new Carte();
            carte.setTypeCarte(carteDTO.getTypeCarte());
            carte.setNomComplet(carteDTO.getNomComplet());
            carte.setNumeroCarte(carteDTO.getNumeroCarte());
            carte.setDateNaissance(carteDTO.getDateNaissance());
            carte.setLieuTrouve(carteDTO.getLieuTrouve());
            carte.setTelephoneRamasseur(carteDTO.getTelephoneRamasseur());

            // Sauvegarder avec l'image
            carteService.publierCarte(carte, carteDTO.getImageFile());

            redirectAttributes.addFlashAttribute("success",
                    "Votre annonce a été publiée avec succès. Elle sera visible après validation par un administrateur.");
            return "redirect:/";

        } catch (IOException e) {
            model.addAttribute("error", "Erreur lors du téléchargement de l'image");
            model.addAttribute("typesCarte", TYPES_CARTE);
            return "public/publier";
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
            model.addAttribute("typesCarte", TYPES_CARTE);
            return "public/publier";
        }
    }

    // Détails d'une carte
    @GetMapping("/cartes/{id}")
    public String viewCarteDetails(@PathVariable Long id, Model model) {
        Carte carte = carteService.getCarteById(id)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        model.addAttribute("carte", carte);
        model.addAttribute("telephoneMasque", carteService.masquerTelephone(carte.getTelephoneRamasseur()));

        // Préparer les DTOs pour les formulaires
        model.addAttribute("contactDTO", new ContactDTO());
        model.addAttribute("signalementDTO", new SignalementDTO());

        return "public/details";
    }

    // Contacter le ramasseur
    @PostMapping("/contact")
    public String contacter(@Valid @ModelAttribute ContactDTO contactDTO,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "redirect:/cartes/" + contactDTO.getCarteId() + "?error=validation";
        }

        try {
            Contact contact = new Contact();
            contact.setCarte(new Carte());
            contact.getCarte().setId(contactDTO.getCarteId());
            contact.setTypeContacteur(contactDTO.getTypeContacteur());
            contact.setTelephoneContacteur(contactDTO.getTelephoneContacteur());
            contact.setMessage(contactDTO.getMessage());

            contactService.etablirContact(contact);

            // Récupérer le numéro complet pour l'afficher
            String telephoneComplet = carteService.getTelephoneComplet(contactDTO.getCarteId());

            redirectAttributes.addFlashAttribute("contactSuccess", true);
            redirectAttributes.addFlashAttribute("telephoneRamasseur", telephoneComplet);
            redirectAttributes.addFlashAttribute("nomContacteur", contactDTO.getNomContacteur());

            return "redirect:/cartes/" + contactDTO.getCarteId() + "#contact";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du contact: " + e.getMessage());
            return "redirect:/cartes/" + contactDTO.getCarteId();
        }
    }

    // Signaler une carte
    @PostMapping("/signaler")
    public String signaler(@Valid @ModelAttribute SignalementDTO signalementDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "redirect:/cartes/" + signalementDTO.getCarteId() + "?error=validation";
        }

        try {
            Signalement signalement = new Signalement();
            signalement.setCarte(new Carte());
            signalement.getCarte().setId(signalementDTO.getCarteId());
            signalement.setRaison(signalementDTO.getRaison());
            signalement.setDescription(signalementDTO.getDescription());
            signalement.setEmailSignaleur(signalementDTO.getEmailSignaleur());

            signalementService.creerSignalement(signalement);

            redirectAttributes.addFlashAttribute("signalementSuccess",
                    "Votre signalement a été envoyé. L'équipe de modération le traitera sous 48h.");
            return "redirect:/cartes/" + signalementDTO.getCarteId() + "#signalement";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du signalement: " + e.getMessage());
            return "redirect:/cartes/" + signalementDTO.getCarteId();
        }
    }

    // Page "À propos"
    @GetMapping("/a-propos")
    public String about() {
        return "public/about";
    }

    // Page "Comment ça marche"
    @GetMapping("/comment-ca-marche")
    public String howItWorks() {
        return "public/how-it-works";
    }

    // Page "Contactez-nous" (pour contacter l'administration)
    @GetMapping("/contactez-nous")
    public String contactUs(Model model) {
        model.addAttribute("contactDTO", new ContactDTO());
        return "public/contact-us";
    }
}