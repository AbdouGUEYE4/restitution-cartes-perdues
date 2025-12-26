package sn.cartesperdues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class CartesPerduesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartesPerduesApplication.class, args);
        System.out.println("\n==========================================");
        System.out.println("🚀 APPLICATION CARTES PERDUES DÉMARRÉE");
        System.out.println("==========================================");
        System.out.println("📊 API Publique: http://localhost:8080/api/cartes");
        System.out.println("📝 Signaler: http://localhost:8080/api/signaler");
        System.out.println("🔐 Admin: http://localhost:8080/api/admin/login");
        System.out.println("==========================================\n");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            System.out.println("✅ Backend Cartes Perdues prêt!");
            System.out.println("📞 Types de cartes disponibles:");
            System.out.println("   • Carte d'identité");
            System.out.println("   • Permis de conduire");
            System.out.println("   • Passeport");
            System.out.println("   • Carte grise véhicule");
            System.out.println("   • Carte grise moto");
            System.out.println("   • Carte de séjour");
            System.out.println("   • Carte étudiante (Gratuit)");
            System.out.println("   • Carte de santé (Gratuit)");
        };
    }
}