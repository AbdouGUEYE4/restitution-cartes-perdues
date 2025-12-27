package sn.cartesperdues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CartePublicationDTO {

    @NotBlank(message = "Le type de carte est obligatoire")
    private String typeCarte; // CNI, Permis, Carte scolaire, Badge, etc.

    @NotBlank(message = "Le nom complet est obligatoire")
    private String nomComplet;

    private String numeroCarte; // facultatif

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance; // facultatif

    @NotBlank(message = "Le lieu où la carte a été trouvée est obligatoire")
    private String lieuTrouve;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^(\\+221|221)?[0-9]{9}$",
            message = "Format de téléphone invalide. Ex: +221771234567 ou 771234567")
    private String telephoneRamasseur;

    private String imageUrl; // facultatif

    // Méthode utilitaire pour convertir en entité Carte
    public sn.cartesperdues.entity.Carte toEntity() {
        sn.cartesperdues.entity.Carte carte = new sn.cartesperdues.entity.Carte();
        carte.setTypeCarte(this.typeCarte);
        carte.setNomComplet(this.nomComplet);
        carte.setNumeroCarte(this.numeroCarte);
        carte.setDateNaissance(this.dateNaissance);
        carte.setLieuTrouve(this.lieuTrouve);
        carte.setTelephoneRamasseur(this.telephoneRamasseur);
        carte.setImageUrl(this.imageUrl);
        return carte;
    }
}