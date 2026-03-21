package app.omniOne.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SupplementPlanResponse(

        Long id,
        List<SupplementEntryResponse> entries,
        LocalDateTime createdAt

) {}
