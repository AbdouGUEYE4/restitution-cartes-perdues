package sn.cartesperdues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
public class CartePublicationDTO {

    @NotBlank(message = "Le type de carte est obligatoire")
    private String typeCarte; // CNI, Permis, Carte scolaire, Badge, etc.

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nomComplet;

    private String numeroCarte; // facultatif

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance; // facultatif

    @NotBlank(message = "Le lieu où la carte a été trouvée est obligatoire")
    @Size(min = 3, max = 200, message = "Le lieu doit contenir entre 3 et 200 caractères")
    private String lieuTrouve;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^(77|76|78|70|75)\\d{7}$",
            message = "Numéro de téléphone sénégalais invalide (ex: 77xxxxxxx)")
    private String telephoneRamasseur;

    private MultipartFile imageFile; // facultatif

    // Getters et Setters générés par Lombok @Data
}