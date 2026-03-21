package app.omniOne.model.dto;

public record SupplementEntryResponse(

        Long id,
        String supplementName,
        String brand,
        String dosage,
        String timing,
        String notes

) {}
