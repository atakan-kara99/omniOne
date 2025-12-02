package app.omniOne.models.mappers;

import app.omniOne.models.entities.Coach;
import app.omniOne.models.dtos.CoachDto;
import org.springframework.stereotype.Component;

@Component
public class CoachMapper {

    public CoachDto toCoachDto(Coach coach) {
        return new CoachDto(coach.getId(), coach.getEmail());
    }

}
