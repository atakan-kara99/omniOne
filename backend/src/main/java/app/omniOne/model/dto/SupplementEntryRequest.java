package app.omniOne.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplementEntryRequest(

        @NotBlank
        @Size(max = 255)
        String supplementName,

        @Size(max = 255)
        String brand,

        @NotBlank
        @Size(max = 100)
        String dosage,

        @Size(max = 255)
        String timing,

        @Size(max = 2000)
        String notes

) {}
