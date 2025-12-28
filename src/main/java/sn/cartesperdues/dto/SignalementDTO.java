package sn.cartesperdues.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignalementDTO {

    private Long carteId;

    @NotBlank(message = "Veuillez sélectionner une raison")
    private String raison;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String description;

    @Email(message = "Veuillez entrer un email valide")
    private String emailSignaleur;
}