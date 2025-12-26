package sn.cartesperdues.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {

    private long totalCartes;
    private long cartesEnAttente;
    private long cartesValidees;
    private long cartesRestituees;
    private long nouveauxSignalements;
    private long totalContacts;
    private long restitutionsReussies;

    // Pourcentage de restitution
    public double getTauxRestitution() {
        if (totalCartes == 0) return 0.0;
        return (cartesRestituees * 100.0) / totalCartes;
    }

    // Pourcentage de validation
    public double getTauxValidation() {
        if (totalCartes == 0) return 0.0;
        return (cartesValidees * 100.0) / totalCartes;
    }
}