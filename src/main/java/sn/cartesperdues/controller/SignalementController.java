package sn.cartesperdues.controller;

import sn.cartesperdues.dto.SignalementDTO;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.service.CarteService;
import sn.cartesperdues.service.SignalementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/signaler")
public class SignalementController {

    @Autowired
    private SignalementService signalementService;

    @Autowired
    private CarteService carteService;

    // Afficher le formulaire de signalement
    @GetMapping("/carte/{carteId}")
    public String showSignalementForm(@PathVariable Long carteId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        // Utilisez getCarteById() qui retourne Optional
        Optional<Carte> carteOptional = carteService.getCarteById(carteId);

        if (carteOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cette carte n'existe plus.");
            return "redirect:/cartes";
        }

        Carte carte = carteOptional.get();
        model.addAttribute("carte", carte);
        model.addAttribute("signalementDTO", new SignalementDTO());

        return "public/signaler/form";
    }

    // Traiter le formulaire de signalement
    @PostMapping("/carte")
    public String processSignalement(@Valid @ModelAttribute SignalementDTO signalementDTO,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Récupérer la carte pour réafficher le formulaire
            Optional<Carte> carteOptional = carteService.getCarteById(signalementDTO.getCarteId());

            if (carteOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Carte non trouvée.");
                return "redirect:/cartes";
            }

            Carte carte = carteOptional.get();
            model.addAttribute("carte", carte);
            return "public/signaler/form";
        }

        // Vérifier l'existence de la carte
        Optional<Carte> carteOptional = carteService.getCarteById(signalementDTO.getCarteId());
        if (carteOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cette carte n'existe plus.");
            return "redirect:/cartes";
        }

        try {
            // Créer le signalement
            signalementService.signalerCarte(
                    signalementDTO.getCarteId(),
                    signalementDTO.getRaison(),
                    signalementDTO.getDescription(),
                    signalementDTO.getEmailSignaleur()
            );

            redirectAttributes.addFlashAttribute("success",
                    "✅ Signalement envoyé avec succès !");
            return "redirect:/signaler/confirmation";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Erreur : " + e.getMessage());
            return "redirect:/signaler/carte/" + signalementDTO.getCarteId();
        }
    }

    // Page de confirmation
    @GetMapping("/confirmation")
    public String confirmation(Model model) {
        return "public/signaler/confirmation";
    }
}