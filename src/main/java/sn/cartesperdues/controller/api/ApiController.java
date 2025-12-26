package sn.cartesperdues.controller.api;

import sn.cartesperdues.dto.*;
import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.service.CarteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private CarteService carteService;

    // API pour rechercher des cartes
    @GetMapping("/cartes")
    public ResponseEntity<ApiResponse<List<Carte>>> rechercherCartes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String lieu) {

        List<Carte> cartes = carteService.rechercherCartes(keyword, type, lieu);
        return ResponseEntity.ok(ApiResponse.success(cartes));
    }

    // API pour obtenir les détails d'une carte
    @GetMapping("/cartes/{id}")
    public ResponseEntity<ApiResponse<CarteAffichageDTO>> getCarteDetails(@PathVariable Long id) {
        Carte carte = carteService.getCarteById(id)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        // Convertir en DTO d'affichage
        CarteAffichageDTO dto = new CarteAffichageDTO();
        dto.setId(carte.getId());
        dto.setTypeCarte(carte.getTypeCarte());
        dto.setNomComplet(carte.getNomComplet());
        dto.setLieuTrouve(carte.getLieuTrouve());
        dto.setDatePublication(carte.getDatePublication());
        dto.setTelephoneMasque(CarteAffichageDTO.masquerTelephone(carte.getTelephoneRamasseur()));
        dto.setImageUrl(carte.getImageUrl());
        dto.setStatut(carte.getStatut().toString());

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // API pour publier une carte (sans image pour l'API)
    @PostMapping("/cartes")
    public ResponseEntity<ApiResponse<Carte>> publierCarte(@Valid @RequestBody CartePublicationDTO carteDTO) {
        try {
            Carte carte = new Carte();
            carte.setTypeCarte(carteDTO.getTypeCarte());
            carte.setNomComplet(carteDTO.getNomComplet());
            carte.setNumeroCarte(carteDTO.getNumeroCarte());
            carte.setDateNaissance(carteDTO.getDateNaissance());
            carte.setLieuTrouve(carteDTO.getLieuTrouve());
            carte.setTelephoneRamasseur(carteDTO.getTelephoneRamasseur());

            Carte savedCarte = carteService.publierCarte(carte, null);
            return ResponseEntity.ok(ApiResponse.success("Carte publiée avec succès", savedCarte));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la publication", e.getMessage()));
        }
    }

    // API pour obtenir le numéro de contact
    @PostMapping("/cartes/{id}/contact")
    public ResponseEntity<ApiResponse<String>> getContact(@PathVariable Long id,
                                                          @RequestBody ContactDTO contactDTO) {
        try {
            // Vérifier et enregistrer le contact
            // contactService.etablirContact(...);

            // Récupérer le numéro complet
            String telephone = carteService.getTelephoneComplet(id);

            // Incrémenter le compteur de contacts
            carteService.incrementerContactCount(id);

            return ResponseEntity.ok(ApiResponse.success("Contact établi", telephone));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors du contact", e.getMessage()));
        }
    }
}