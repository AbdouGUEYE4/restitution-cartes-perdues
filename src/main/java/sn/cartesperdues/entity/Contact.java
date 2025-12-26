package sn.cartesperdues.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carte_id", nullable = false)
    private Carte carte;

    @Column(name = "type_contacteur")
    private String typeContacteur; // "Propriétaire", "Tiers", "Inconnu"

    @Column(name = "telephone_contacteur")
    private String telephoneContacteur;

    @Column(name = "message")
    private String message;

    @Column(name = "date_contact")
    private LocalDateTime dateContact = LocalDateTime.now();

    @Column(name = "restitution_reussie")
    private Boolean restitutionReussie = false;

    // Constructeurs
    public Contact() {}

    public Contact(Carte carte, String telephoneContacteur) {
        this.carte = carte;
        this.telephoneContacteur = telephoneContacteur;
    }
}