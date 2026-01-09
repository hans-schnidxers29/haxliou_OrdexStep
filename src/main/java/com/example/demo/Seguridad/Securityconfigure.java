package com.example.demo.Seguridad;

import com.example.demo.Login.Servicio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class Securityconfigure {

    @Autowired
    private ServicioUsuario usuarioServicio;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(usuarioServicio);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/StyleLogin/**",
                                "/IMG/**",
                                "/js/**",
                                "/images/**",
                                "/adminlte/**",
                                "/plugins/**"
                        ).permitAll()
                        .requestMatchers(
                                "/registro**",
                                "/registro/nuevo",
                                "/login**"
                        ).permitAll().
                        requestMatchers(
                              "/listarproductos", "/crearproducto/nuevo",
                                "/categoria/crear","/producto/actualizar/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // Configuración del login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/Home", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Configuración del logout - SIN AntPathRequestMatcher
                .logout(logout -> logout
                        .logoutUrl("/logout")  // ✅ Usa logoutUrl() directamente
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}