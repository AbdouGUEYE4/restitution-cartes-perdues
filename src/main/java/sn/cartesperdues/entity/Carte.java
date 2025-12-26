package sn.cartesperdues.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartes")
@Data
public class Carte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_carte", nullable = false)
    private String typeCarte; // CNI, Permis, Carte scolaire, Badge, etc.

    @Column(name = "nom_complet", nullable = false)
    private String nomComplet;

    @Column(name = "numero_carte")
    private String numeroCarte; // facultatif

    @Column(name = "date_naissance")
    private LocalDate dateNaissance; // facultatif

    @Column(name = "lieu_trouve", nullable = false)
    private String lieuTrouve;

    @Column(name = "date_publication")
    private LocalDateTime datePublication = LocalDateTime.now();

    @Column(name = "telephone_ramasseur", nullable = false)
    private String telephoneRamasseur;

    @Column(name = "image_url")
    private String imageUrl; // facultatif

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCarte statut = StatutCarte.EN_ATTENTE;

    @Column(name = "contact_count")
    private int contactCount = 0; // compteur de contacts

    @Column(name = "raison_suppression")
    private String raisonSuppression; // si supprimé par admin

    // Constructeurs
    public Carte() {}

    public Carte(String typeCarte, String nomComplet, String lieuTrouve, String telephoneRamasseur) {
        this.typeCarte = typeCarte;
        this.nomComplet = nomComplet;
        this.lieuTrouve = lieuTrouve;
        this.telephoneRamasseur = telephoneRamasseur;
    }
}