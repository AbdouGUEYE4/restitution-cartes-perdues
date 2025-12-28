package sn.cartesperdues.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import sn.cartesperdues.entity.StatutCarte;
import sn.cartesperdues.service.CarteService;
import sn.cartesperdues.service.SignalementService;
import sn.cartesperdues.dto.SignalementDTO;
import sn.cartesperdues.entity.Carte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class PublicController {

    @Autowired
    private CarteService carteService;

    @Autowired
    private SignalementService signalementService;

    private final String UPLOAD_DIR = "./uploads/";

    // === PAGES PRINCIPALES ===

    @GetMapping("/")
    public String home(Model model) {
        var cartes = carteService.getAllCartes();
        model.addAttribute("cartes", cartes.size() > 6 ? cartes.subList(0, Math.min(6, cartes.size())) : cartes);
        return "public/index";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/";
    }

    // === FORMULAIRE DE PUBLICATION ===

    @GetMapping("/publier")
    public String publierCarte(Model model) {
        model.addAttribute("carte", new Carte());
        model.addAttribute("pageTitle", "Publier une carte trouvée");
        return "public/construction";
    }

    @PostMapping("/publier")
    public String traiterPublication(
            @Valid @ModelAttribute("carte") Carte carte,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "dateNaissance", required = false) String dateNaissanceStr,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("pageTitle", "Publier une carte trouvée");

        if (result.hasErrors()) {
            System.out.println("Erreurs de validation: " + result.getAllErrors());
            return "public/construction";
        }

        try {
            // Log des données reçues
            System.out.println("=== DONNEES RECUES ===");
            System.out.println("Type carte: " + carte.getTypeCarte());
            System.out.println("Nom complet: " + carte.getNomComplet());
            System.out.println("Lieu trouvé: " + carte.getLieuTrouve());
            System.out.println("Téléphone: " + carte.getTelephoneRamasseur());
            System.out.println("Numéro carte: " + carte.getNumeroCarte());
            System.out.println("Date naissance string: " + dateNaissanceStr);

            // Convertir la date de naissance si présente
            if (dateNaissanceStr != null && !dateNaissanceStr.trim().isEmpty()) {
                try {
                    LocalDate dateNaissance = LocalDate.parse(dateNaissanceStr);
                    carte.setDateNaissance(dateNaissance);
                    System.out.println("Date naissance convertie: " + dateNaissance);
                } catch (Exception e) {
                    System.out.println("Erreur parsing date naissance: " + e.getMessage());
                }
            }

            // Gérer l'upload d'image
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveUploadedFile(imageFile);
                carte.setImageUrl(imageUrl);
                System.out.println("Image sauvegardée: " + imageUrl);
            }

            // Définir les valeurs par défaut
            carte.setStatut(StatutCarte.EN_ATTENTE);
            carte.setDatePublication(LocalDateTime.now());
            carte.setContactCount(0);

            // Sauvegarder la carte
            Carte savedCarte = carteService.saveCarte(carte);

            System.out.println("✅ Carte sauvegardée avec ID: " + savedCarte.getId());
            System.out.println("Statut: " + savedCarte.getStatut());
            System.out.println("Date publication: " + savedCarte.getDatePublication());

            redirectAttributes.addFlashAttribute("success",
                    "Votre carte a été publiée avec succès ! Elle sera visible après validation.");
            return "redirect:/publier?success";

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la publication: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Erreur lors de la publication : " + e.getMessage());
            return "public/construction";
        }
    }

    // === METHODE POUR SAUVEGARDER LES FICHIERS ===

    private String saveUploadedFile(MultipartFile file) throws IOException {
        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath);

        // Retourner le chemin d'accès relatif
        return "/uploads/" + uniqueFileName;
    }

    // === RECHERCHE ET DETAILS ===

    @GetMapping("/rechercher")
    public String rechercherCarteForm(Model model) {
        model.addAttribute("pageTitle", "Rechercher ma carte perdue");
        return "public/rechercher-form";
    }

    @PostMapping("/rechercher")
    public String rechercherCarte(
            @RequestParam(required = false) String numeroCarte,
            @RequestParam(required = false) String nomComplet,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateNaissance,
            Model model) {

        System.out.println("=== RECHERCHE SÉCURISÉE ===");
        System.out.println("Numéro carte: " + numeroCarte);
        System.out.println("Nom complet: " + nomComplet);
        System.out.println("Date naissance: " + dateNaissance);

        try {
            // Validation : au moins un critère doit être fourni
            if ((numeroCarte == null || numeroCarte.trim().isEmpty())
                    && (nomComplet == null || nomComplet.trim().isEmpty())) {
                model.addAttribute("error", "Veuillez fournir au moins le numéro de carte ou le nom complet.");
                return "public/rechercher-form";
            }

            // Recherche sécurisée
            List<Carte> resultats = carteService.rechercherCarteSecurise(
                    numeroCarte != null ? numeroCarte.trim() : null,
                    nomComplet != null ? nomComplet.trim() : null,
                    dateNaissance
            );

            System.out.println("Résultats trouvés: " + resultats.size());

            if (resultats.isEmpty()) {
                model.addAttribute("message", "Aucune carte ne correspond à votre recherche.");
                model.addAttribute("messageType", "info");
            } else {
                model.addAttribute("cartes", resultats);
            }

            // Réinitialiser les champs pour la sécurité
            model.addAttribute("numeroCarte", "");
            model.addAttribute("nomComplet", "");
            model.addAttribute("dateNaissance", "");

        } catch (Exception e) {
            System.err.println("Erreur recherche: " + e.getMessage());
            model.addAttribute("error", "Une erreur est survenue lors de la recherche.");
        }

        model.addAttribute("pageTitle", "Résultats de recherche");
        return "public/rechercher-form";
    }

    @GetMapping("/cartes/{id}")
    public String detailCarte(@PathVariable Long id, Model model) {
        try {
            Carte carte = carteService.findCarteById(id);
            model.addAttribute("carte", carte);
            model.addAttribute("pageTitle", "Détails de la carte");
            return "public/cartes/detail";
        } catch (Exception e) {
            return "redirect:/cartes?error=Carte+non+trouvée";
        }

    }
    @GetMapping("/cartes")
    public String redirectToSearch() {
        return "redirect:/rechercher";
    }
    @Controller
    public static class LoginController {  // NOTE: "static" est important ici

        @GetMapping("/login")
        public String login() {
            return "login";  // Renvoie vers templates/login.html
        }
    }

    // === SIGNALEMENTS ===

    @GetMapping("/signaler/carte/{carteId}")
    public String signalerCarte(@PathVariable Long carteId, Model model) {
        try {
            Carte carte = carteService.findCarteById(carteId);

            SignalementDTO signalementDTO = new SignalementDTO();
            signalementDTO.setCarteId(carteId);

            model.addAttribute("carte", carte);
            model.addAttribute("signalementDTO", signalementDTO);
            model.addAttribute("pageTitle", "Signaler cette carte");

            return "public/signaler/form";
        } catch (Exception e) {
            return "redirect:/cartes?error=Carte+non+trouvée";
        }
    }

    @PostMapping("/signaler/carte")
    public String traiterSignalement(@Valid @ModelAttribute SignalementDTO signalementDTO,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            try {
                Carte carte = carteService.findCarteById(signalementDTO.getCarteId());
                model.addAttribute("carte", carte);
                model.addAttribute("pageTitle", "Signaler cette carte");
                return "public/signaler/form";
            } catch (Exception e) {
                return "redirect:/cartes?error=Carte+non+trouvée";
            }
        }

        try {
            signalementService.creerSignalement(signalementDTO);

            redirectAttributes.addFlashAttribute("success",
                    "Votre signalement a été envoyé avec succès. Notre équipe l'examinera sous 24-48 heures.");

            return "redirect:/cartes/" + signalementDTO.getCarteId();
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'envoi du signalement: " + e.getMessage());

            try {
                Carte carte = carteService.findCarteById(signalementDTO.getCarteId());
                model.addAttribute("carte", carte);
                model.addAttribute("signalementDTO", signalementDTO);
                return "public/signaler/form";
            } catch (Exception ex) {
                return "redirect:/cartes?error=Carte+non+trouvée";
            }
        }
    }

    @GetMapping("/signaler")
    public String signaler(Model model) {
        return "redirect:/cartes";
    }

    // === PAGES D'INFORMATION ===

    @GetMapping("/a-propos")
    public String aPropos(Model model) {
        model.addAttribute("pageTitle", "À propos de nous");
        model.addAttribute("pageMessage", "Cette page sera disponible prochainement.");
        return "public/simple-page";
    }

    @GetMapping("/comment-ca-marche")
    public String commentCaMarche(Model model) {
        model.addAttribute("pageTitle", "Comment ça marche");
        model.addAttribute("pageMessage", "Cette fonctionnalité sera disponible prochainement.");
        return "public/simple-page";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contactez-nous");
        model.addAttribute("pageMessage", "Notre formulaire de contact sera bientôt disponible.");
        return "public/simple-page";
    }

    // === DEBUG / TEST ===

    @GetMapping("/debug/cartes")
    @ResponseBody
    public String listAllCartesDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Liste des cartes (Debug)</h1>");
        sb.append("<table border='1'>");
        sb.append("<tr><th>ID</th><th>Nom</th><th>Type</th><th>Lieu</th><th>Statut</th><th>Téléphone</th><th>Date</th></tr>");

        carteService.getAllCartes().forEach(carte -> {
            sb.append("<tr>");
            sb.append("<td>").append(carte.getId()).append("</td>");
            sb.append("<td>").append(carte.getNomComplet()).append("</td>");
            sb.append("<td>").append(carte.getTypeCarte()).append("</td>");
            sb.append("<td>").append(carte.getLieuTrouve()).append("</td>");
            sb.append("<td>").append(carte.getStatut()).append("</td>");
            sb.append("<td>").append(carte.getTelephoneRamasseur()).append("</td>");
            sb.append("<td>").append(carte.getDatePublication()).append("</td>");
            sb.append("</tr>");
        });

        sb.append("</table>");
        sb.append("<br>Total: ").append(carteService.getAllCartes().size()).append(" cartes");

        return sb.toString();
    }

    @GetMapping("/debug/test-db")
    @ResponseBody
    public String testDatabaseDebug() {
        long count = carteService.getAllCartes().size();
        return "Nombre de cartes en base de données: " + count +
                "<br><a href='/debug/cartes'>Voir toutes les cartes</a>" +
                "<br><a href='/publier'>Retour au formulaire</a>";
    }

    @GetMapping("/debug/request")
    @ResponseBody
    public String debugRequest(@RequestParam java.util.Map<String, String> allParams) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Paramètres reçus</h1>");
        sb.append("<table border='1'>");
        sb.append("<tr><th>Paramètre</th><th>Valeur</th></tr>");

        allParams.forEach((key, value) -> {
            sb.append("<tr>");
            sb.append("<td>").append(key).append("</td>");
            sb.append("<td>").append(value).append("</td>");
            sb.append("</tr>");
        });

        sb.append("</table>");
        sb.append("<br>Nombre de paramètres: ").append(allParams.size());

        return sb.toString();
    }


}