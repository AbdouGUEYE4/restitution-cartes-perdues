package sn.cartesperdues.repository;

import sn.cartesperdues.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Trouver tous les contacts pour une carte
    List<Contact> findByCarteId(Long carteId);

    // Trouver les contacts qui ont mené à une restitution réussie
    List<Contact> findByRestitutionReussieTrue();

    // Trouver les contacts par type de contacteur
    List<Contact> findByTypeContacteur(String typeContacteur);

    // Compter les contacts pour une carte
    long countByCarteId(Long carteId);

    // Compter les restitutions réussies
    long countByRestitutionReussieTrue();
}