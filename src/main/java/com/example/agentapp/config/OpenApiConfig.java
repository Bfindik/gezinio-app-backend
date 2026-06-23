package com.example.agentapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI tourismAgencyOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Tourism Agency Management API")
                        .description("""
                                REST API for the Tourism Agency Management System.
                                
                                ## Authentication
                                Most endpoints require a JWT Bearer token.
                                1. Call **POST /api/auth/register** or **POST /api/auth/login**
                                2. Copy the `accessToken` from the response
                                3. Click the **Authorize** button and enter: `Bearer <your-token>`
                                
                                ## Roles
                                - **SUPER_ADMIN** — Full system access, role management
                                - **ADMIN** — User management, reservations, tours, dashboard
                                - **AGENT** — Tour/hotel/transfer CRUD, reservations, payments
                                - **USER** — Browse tours, make reservations, reviews, favorites
                                
                                ## Modules
                                | Module | Description |
                                |--------|-------------|
                                | Auth | Registration, login, token refresh, password management |
                                | Tours | Tour packages with hotels, transfers, pricing |
                                | Hotels & Rooms | Hotel management with room types |
                                | Transfers | Airport/city transfers linked to tours |
                                | Reservations | Individual and group bookings |
                                | Payments | Payment processing and refunds |
                                | Reviews | Tour reviews with admin moderation |
                                | Favorites | User favorite tours |
                                | Profile | User profile management |
                                | Notifications | Email notification logs |
                                | Admin | Dashboard stats, user/reservation management |
                                """)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("AgentApp Dev Team")
                                .email("dev@agentapp.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.agentapp.com").description("Production")))
                // Tag ordering — controls the section order in Swagger UI
                .tags(List.of(
                        new Tag().name("Users").description("User CRUD and current user operations"),
                        new Tag().name("Tours").description("Tour package management and search"),
                        new Tag().name("Hotels").description("Hotel management"),
                        new Tag().name("Rooms").description("Hotel room management"),
                        new Tag().name("Transfers").description("Transfer/transport management"),
                        new Tag().name("Reservations").description("Individual reservation booking"),
                        new Tag().name("Group Reservations").description("Group/bulk reservation booking"),
                        new Tag().name("Payments").description("Payment processing and refunds"),
                        new Tag().name("Reviews").description("Tour reviews and moderation"),
                        new Tag().name("Favorites").description("User favorite tours"),
                        new Tag().name("Profile").description("User profile management"),
                        new Tag().name("Notifications").description("Notification logs"),
                        new Tag().name("Admin — Dashboard").description("Admin dashboard statistics"),
                        new Tag().name("Admin — Users").description("Admin user management"),
                        new Tag().name("Admin — Reservations").description("Admin reservation management"),
                        new Tag().name("Admin — Tours").description("Admin tour statistics"),
                        new Tag().name("Admin — Customer Groups").description("Customer group (household/company/travel-party) management"),
                        new Tag().name("Admin — Roles").description("Super admin role/permission management")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token from /api/auth/login or /api/auth/register")));
    }
}
