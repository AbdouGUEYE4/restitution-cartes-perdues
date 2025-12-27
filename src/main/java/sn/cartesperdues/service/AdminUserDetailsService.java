package sn.cartesperdues.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.cartesperdues.entity.Administrateur;
import sn.cartesperdues.repository.AdministrateurRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class AdminUserDetailsService implements UserDetailsService {

    private final AdministrateurRepository administrateurRepository;

    public AdminUserDetailsService(AdministrateurRepository administrateurRepository) {
        this.administrateurRepository = administrateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("\n=== DEBUG AdminUserDetailsService ===");
        System.out.println("Username reçu: '" + username + "'");

        // Éviter de traiter anonymousUser
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            System.out.println("DEBUG: Username invalide ou anonymousUser");
            throw new UsernameNotFoundException("Username non valide");
        }

        // Rechercher l'administrateur
        System.out.println("DEBUG: Recherche de l'admin dans la base...");
        Administrateur admin = administrateurRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("DEBUG: Admin NON trouvé pour username: " + username);
                    return new UsernameNotFoundException("Administrateur non trouvé: " + username);
                });

        System.out.println("DEBUG: Admin trouvé!");
        System.out.println("  - ID: " + admin.getId());
        System.out.println("  - Username: " + admin.getUsername());
        System.out.println("  - Role: " + admin.getRole());
        System.out.println("  - Actif: " + admin.getActif());
        System.out.println("  - Password hash présent: " +
                (admin.getPasswordHash() != null && !admin.getPasswordHash().isEmpty()));
        System.out.println("  - Longueur hash: " +
                (admin.getPasswordHash() != null ? admin.getPasswordHash().length() : 0));

        // DEBUG: Tester le mot de passe
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder testEncoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        boolean passwordMatches = testEncoder.matches("admin123", admin.getPasswordHash());
        System.out.println("  - Password 'admin123' matches: " + passwordMatches);

        // Créer l'autorité (ROLE_ADMIN)
        String roleWithPrefix = "ROLE_" + admin.getRole();
        System.out.println("  - Role avec préfixe: " + roleWithPrefix);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(roleWithPrefix)
        );

        // Créer UserDetails
        UserDetails userDetails = new User(
                admin.getUsername(),
                admin.getPasswordHash(),
                admin.getActif() != null ? admin.getActif() : true,   // enabled
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                authorities
        );

        System.out.println("DEBUG: UserDetails créé avec succès!");
        System.out.println("  - Username: " + userDetails.getUsername());
        System.out.println("  - Authorities: " + userDetails.getAuthorities());
        System.out.println("  - Enabled: " + userDetails.isEnabled());
        System.out.println("=== FIN DEBUG ===\n");

        return userDetails;
    }
}