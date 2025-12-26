package sn.cartesperdues.service;

import sn.cartesperdues.entity.Carte;
import sn.cartesperdues.entity.Contact;
import sn.cartesperdues.repository.CarteRepository;
import sn.cartesperdues.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private CarteRepository carteRepository;

    @Autowired
    private CarteService carteService;

    // Établir un contact
    @Transactional
    public Contact etablirContact(Contact contact) {
        // Vérifier si la carte existe
        Carte carte = carteRepository.findById(contact.getCarte().getId())
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

        contact.setCarte(carte);
        contact.setDateContact(LocalDateTime.now());

        // Incrémenter le compteur de contacts sur la carte
        carteService.incrementerContactCount(carte.getId());

        return contactRepository.save(contact);
    }

    // Marquer une restitution comme réussie
    @Transactional
    public Contact marquerRestitutionReussie(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact non trouvé"));

        contact.setRestitutionReussie(true);

        // Marquer aussi la carte comme restituée
        carteService.marquerCommeRestituee(contact.getCarte().getId());

        return contactRepository.save(contact);
    }

    // Récupérer tous les contacts pour une carte
    public List<Contact> getContactsPourCarte(Long carteId) {
        return contactRepository.findByCarteId(carteId);
    }

    // Récupérer les contacts ayant mené à une restitution
    public List<Contact> getRestitutionsReussies() {
        return contactRepository.findByRestitutionReussieTrue();
    }

    // Statistiques
    public long countTotalContacts() {
        return contactRepository.count();
    }

    public long countRestitutionsReussies() {
        return contactRepository.countByRestitutionReussieTrue();
    }
}