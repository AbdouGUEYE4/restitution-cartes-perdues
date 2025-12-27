package sn.cartesperdues.config;

import sn.cartesperdues.entity.Administrateur;
import sn.cartesperdues.repository.AdministrateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    // Définition des valeurs par défaut pour l'Admin
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NOM_COMPLET = "Administrateur Principal";
    private static final String ADMIN_EMAIL = "admin@cartes-perdues.sn";
    private static final String ADMIN_TELEPHONE = "+221771234567";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final boolean ADMIN_ACTIF = true;

    public AdminInitializer(AdministrateurRepository administrateurRepository,
                            PasswordEncoder passwordEncoder) {
        this.administrateurRepository = administrateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Vérifie si l'administrateur existe déjà par username
        if (administrateurRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {

            // Utilise l'encodeur de mot de passe du contexte Spring
            String motDePasseHache = passwordEncoder.encode(ADMIN_PASSWORD);

            Administrateur admin = new Administrateur();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPasswordHash(motDePasseHache);
            admin.setNomComplet(ADMIN_NOM_COMPLET);
            admin.setEmail(ADMIN_EMAIL);
            admin.setTelephone(ADMIN_TELEPHONE);
            admin.setRole(ADMIN_ROLE);
            admin.setActif(ADMIN_ACTIF);
            admin.setLoginAttempts(0);

            administrateurRepository.save(admin);

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ COMPTE ADMINISTRATEUR CRÉÉ AVEC SUCCÈS !");
            System.out.println("=".repeat(60));
            System.out.println("👤 Username: " + ADMIN_USERNAME);
            System.out.println("🔑 Password: " + ADMIN_PASSWORD);
            System.out.println("📧 Email: " + ADMIN_EMAIL);
            System.out.println("=".repeat(60));
            System.out.println("⚠️  IMPORTANT : Changez le mot de passe après la première connexion !");
            System.out.println("=".repeat(60) + "\n");
        } else {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ Compte administrateur déjà existant");
            System.out.println("=".repeat(50) + "\n");
        }
    }
}