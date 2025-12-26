package sn.cartesperdues.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalements")
@Data
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carte_id", nullable = false)
    private Carte carte;

    @Column(name = "raison", nullable = false)
    private String raison; // "Fraude", "Information erronée", "Publication abusive"

    @Column(name = "description")
    private String description;

    @Column(name = "email_signaleur")
    private String emailSignaleur; // facultatif

    @Column(name = "date_signalement")
    private LocalDateTime dateSignalement = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutSignalement statut = StatutSignalement.NOUVEAU;

    // Constructeurs
    public Signalement() {}

    public Signalement(Carte carte, String raison) {
        this.carte = carte;
        this.raison = raison;
    }
}