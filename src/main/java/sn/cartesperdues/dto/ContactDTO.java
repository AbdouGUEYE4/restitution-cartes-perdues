package sn.cartesperdues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ContactDTO {

    private Long carteId;

    @NotBlank(message = "Votre nom est obligatoire")
    private String nomContacteur;

    @NotBlank(message = "Votre numéro de téléphone est obligatoire")
    @Pattern(regexp = "^(77|76|78|70|75)\\d{7}$",
            message = "Numéro de téléphone sénégalais invalide (ex: 77xxxxxxx)")
    private String telephoneContacteur;

    private String typeContacteur; // "Propriétaire", "Tiers", "Inconnu"

    private String message;

    // Getters et Setters générés par Lombok @Data
}