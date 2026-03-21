package app.omniOne.model.mapper;

import app.omniOne.model.dto.SupplementEntryRequest;
import app.omniOne.model.dto.SupplementEntryResponse;
import app.omniOne.model.entity.SupplementEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplementEntryMapper {

    SupplementEntryResponse map(SupplementEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplementPlan", ignore = true)
    SupplementEntry map(SupplementEntryRequest request);

}
