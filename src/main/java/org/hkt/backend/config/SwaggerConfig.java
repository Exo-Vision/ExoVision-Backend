package org.hkt.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NASA Exoplanet Detection System API")
                        .description("Advanced Machine Learning API for Exoplanet Detection and Analysis")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("NASA Exoplanet Team")
                                .email("exoplanet@nasa.gov")
                                .url("https://exoplanets.nasa.gov"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.exoplanet.nasa.gov")
                                .description("Production Server")
                ));
    }
}
