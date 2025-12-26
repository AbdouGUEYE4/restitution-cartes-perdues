package sn.cartesperdues.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CarteAffichageDTO {

    private Long id;
    private String typeCarte;
    private String nomComplet;
    private String lieuTrouve;
    private LocalDateTime datePublication;
    private String telephoneMasque; // Ex: "77***123"
    private String imageUrl;
    private String statut;

    // Méthode pour masquer le téléphone
    public static String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 4) {
            return "***";
        }

        int longueur = telephone.length();
        String debut = telephone.substring(0, 2);
        String fin = telephone.substring(longueur - 2);

        return debut + "***" + fin;
    }
}