package sn.cartesperdues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignalementDTO {

    @NotNull(message = "L'ID de la carte est requis")
    private Long carteId;

    @NotBlank(message = "La raison du signalement est obligatoire")
    private String raison;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String emailSignaleur; // Optionnel
}