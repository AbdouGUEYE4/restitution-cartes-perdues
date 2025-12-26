package sn.cartesperdues.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminDTO {

    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String confirmPassword;

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom complet doit contenir entre 3 et 100 caractères")
    private String nomComplet;

    @Email(message = "Adresse email invalide")
    private String email;

    @Pattern(regexp = "^(77|76|78|70|75)\\d{7}$",
            message = "Numéro de téléphone sénégalais invalide (ex: 77xxxxxxx)")
    private String telephone;

    private String role = "ADMIN";

    // Méthode pour vérifier que les mots de passe correspondent
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    // Getters et Setters générés par Lombok @Data
}