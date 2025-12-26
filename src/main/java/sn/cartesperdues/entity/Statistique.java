package sn.cartesperdues.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "statistiques")
@Data
public class Statistique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_jour", nullable = false, unique = true)
    private LocalDate dateJour = LocalDate.now();

    @Column(name = "cartes_publiees")
    private int cartesPubliees = 0;

    @Column(name = "cartes_validees")
    private int cartesValidees = 0;

    @Column(name = "cartes_restituées")
    private int cartesRestituees = 0;

    @Column(name = "signalements_recus")
    private int signalementsRecus = 0;

    @Column(name = "contacts_etablis")
    private int contactsEtablis = 0;

    @Column(name = "nouveaux_utilisateurs")
    private int nouveauxUtilisateurs = 0;

    // Constructeurs
    public Statistique() {}

    public Statistique(LocalDate dateJour) {
        this.dateJour = dateJour;
    }
}