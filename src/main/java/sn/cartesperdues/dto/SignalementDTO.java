package sn.cartesperdues.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignalementDTO {

    private Long carteId;

    @NotBlank(message = "La raison du signalement est obligatoire")
    private String raison; // "Fraude", "Information erronée", "Publication abusive"

    private String description;

    @Email(message = "Adresse email invalide")
    private String emailSignaleur; // facultatif

    // Getters et Setters générés par Lombok @Data
}