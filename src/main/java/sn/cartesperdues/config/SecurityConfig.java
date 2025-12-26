package sn.cartesperdues.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                .authorizeHttpRequests(authorize -> authorize
                        // Pages publiques
                        .requestMatchers(
                                "/",
                                "/rechercher",
                                "/publier",
                                "/cartes/**",
                                "/contact/**",
                                "/signaler/**",
                                "/a-propos",
                                "/comment-ca-marche",
                                "/contactez-nous",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/api/**"
                        ).permitAll()

                        // Pages admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Désactiver CSRF pour faciliter le développement

        return http.build();
    }
}