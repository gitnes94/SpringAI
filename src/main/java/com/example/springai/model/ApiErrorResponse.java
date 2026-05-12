package com.example.springai.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standardiserat felsvar från API:et")
public record ApiErrorResponse(

        @Schema(description = "HTTP-statuskod", example = "503")
        int status,

        @Schema(description = "Kortfattad feltyp", example = "Service Unavailable")
        String error,

        @Schema(description = "Detaljerat felmeddelande")
        String message,

        @Schema(description = "Tidpunkt då felet inträffade")
        Instant timestamp

) {}