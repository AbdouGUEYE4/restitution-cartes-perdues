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
                .csrf(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.disable()) // Ajoutez cette ligne
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/debug/**", "/temp/**").permitAll()  // Pour le debug
                        .requestMatchers("/admin/login", "/admin/logout", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/signaler/**",  // Autoriser les pages de signalement
                                "/cartes/**"
                        ).permitAll()
                        .anyRequest().permitAll()  // Temporairement

                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")  // Doit correspondre au formulaire
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .usernameParameter("username")  // Doit correspondre au formulaire
                        .passwordParameter("password")  // Doit correspondre au formulaire
                        .successHandler(debugSuccessHandler())  // Handler de debug
                        .failureHandler(debugFailureHandler())  // Handler de debug
                        .permitAll()

                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
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

                System.out.println("\n✅✅✅ CONNEXION RÉUSSIE ✅✅✅");
                System.out.println("Username: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());
                System.out.println("Authenticated: " + authentication.isAuthenticated());
                System.out.println("Details: " + authentication.getDetails());

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

                System.out.println("\n❌❌❌ ÉCHEC DE CONNEXION ❌❌❌");
                System.out.println("Username tenté: " + request.getParameter("username"));
                System.out.println("Exception: " + exception.getClass().getName());
                System.out.println("Message: " + exception.getMessage());
                System.out.println("Cause: " + (exception.getCause() != null ? exception.getCause().getMessage() : "null"));

                // Log stack trace pour debug
                exception.printStackTrace();

                // Continuer vers la redirection normale
                super.onAuthenticationFailure(request, response, exception);
            }
        };
    }
}