package sn.cartesperdues.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "administrateurs")
@Data
public class Administrateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "ADMIN";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "nom_complet")
    private String nomComplet;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone")
    private String telephone;

    // NOUVEAUX CHAMPS AJOUTÉS
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;  // Par défaut actif

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;  // Tentatives de connexion échouées

    // Constructeur par défaut
    public Administrateur() {}

    // Constructeur avec paramètres
    public Administrateur(String username, String passwordHash, String nomComplet) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nomComplet = nomComplet;
        this.actif = true;  // Initialisé à true
        this.loginAttempts = 0;  // Initialisé à 0
    }
}