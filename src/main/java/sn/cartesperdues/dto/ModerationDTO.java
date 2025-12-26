package sn.cartesperdues.dto;

import lombok.Data;

@Data
public class ModerationDTO {

    private Long carteId;

    private String action; // "VALIDER", "SUPPRIMER", "RESTITUER"

    private String raison; // Pour les suppressions

    private Long adminId; // ID de l'admin qui effectue l'action

    // Getters et Setters générés par Lombok @Data
}