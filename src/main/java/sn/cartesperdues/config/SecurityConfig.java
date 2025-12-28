package sn.cartesperdues.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF avec une seule méthode
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authz -> authz
                        // Pages publiques
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/debug/**", "/temp/**").permitAll()  // Pour le debug

                        // IMPORTANT: Permettre l'accès à /admin/login pour tous
                        .requestMatchers("/admin/login", "/error").permitAll()

                        // Pages admin nécessitent le rôle ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Pages publiques de signalement
                        .requestMatchers("/signaler/**", "/cartes/**").permitAll()

                        // Temporairement permettre tout le reste
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        // Page de login (GET) - gérée par votre AdminController
                        .loginPage("/login")

                        // URL de traitement (POST) - gérée par Spring Security
                        .loginProcessingUrl("/login")

                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(debugSuccessHandler())  // Handler de debug
                        .failureHandler(debugFailureHandler())  // Handler de debug
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/admin/access-denied")
                );

        return http.build();
    }

    // Handler pour les connexions réussies (debug)
    @Bean
    public AuthenticationSuccessHandler debugSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.security.core.Authentication authentication)
                    throws IOException, ServletException {

                System.out.println("\n" + "=".repeat(50));
                System.out.println("✅ CONNEXION RÉUSSIE ✅");
                System.out.println("=".repeat(50));
                System.out.println("Username: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());
                System.out.println("Authenticated: " + authentication.isAuthenticated());

                // Log supplémentaire
                if (authentication.getPrincipal() != null) {
                    System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
                }

                System.out.println("Redirection vers: /admin/dashboard");
                System.out.println("=".repeat(50) + "\n");

                // Continuer vers la redirection normale
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }

    // Handler pour les échecs de connexion (debug)
    @Bean
    public AuthenticationFailureHandler debugFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.security.core.AuthenticationException exception)
                    throws IOException, ServletException {

                System.out.println("\n" + "=".repeat(50));
                System.out.println("❌ ÉCHEC DE CONNEXION ❌");
                System.out.println("=".repeat(50));
                System.out.println("Username tenté: " + request.getParameter("username"));
                System.out.println("Exception: " + exception.getClass().getSimpleName());
                System.out.println("Message: " + exception.getMessage());

                // Log des paramètres de la requête
                System.out.println("\nParamètres de la requête:");
                request.getParameterMap().forEach((key, values) -> {
                    System.out.println("  " + key + " = " + String.join(", ", values));
                });

                System.out.println("Redirection vers: /login?error=true");
                System.out.println("=".repeat(50) + "\n");

                // Log stack trace pour debug
                exception.printStackTrace();

                // Continuer vers la redirection normale
                super.onAuthenticationFailure(request, response, exception);
            }
        };
    }
}