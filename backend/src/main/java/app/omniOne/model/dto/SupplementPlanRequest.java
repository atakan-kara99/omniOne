package app.omniOne.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SupplementPlanRequest(

        @NotNull
        @Size(min = 1, message = "At least one supplement entry is required")
        List<@Valid SupplementEntryRequest> entries

) {}
