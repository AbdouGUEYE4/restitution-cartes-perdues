package sn.cartesperdues.dto;

import lombok.Data;

@Data
public class CarteRechercheDTO {

    private String keyword;

    private String typeCarte;

    private String lieu;

    // Getters et Setters générés par Lombok @Data
}