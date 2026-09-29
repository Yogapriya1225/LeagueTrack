package com.example.leaguetrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI leagueTrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LeagueTrack REST API")
                        .description("Intramural Sports Tournament Fixture and Standings Management System - Sri Eshwar College of Engineering Project Leap")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LeagueTrack Tournament Committee")
                                .email("leaguetrack@sece.ac.in"))
                        .license(new License().name("Educational Use Only")));
    }
}
