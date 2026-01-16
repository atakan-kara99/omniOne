package app.omniOne.service;

import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.Coaching;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static app.omniOne.TestFixtures.client;
import static app.omniOne.TestFixtures.coach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class CoachingServiceTest {

    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private CoachingRepo coachingRepo;
    @InjectMocks private CoachingService coachingService;

    private UUID coachId;
    private UUID clientId;
    private Coach coach;
    private Client client;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        coach = coach(coachId);
        client = client(clientId);
    }

    @Test void startCoaching_linksCoachAndClientAndPersistsCoaching() {
        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(coach);

        coachingService.startCoaching(coachId, clientId);

        assertSame(coach, client.getCoach());

        ArgumentCaptor<Coaching> coachingCaptor = ArgumentCaptor.forClass(Coaching.class);
        verify(coachingRepo).save(coachingCaptor.capture());
        Coaching savedCoaching = coachingCaptor.getValue();
        assertSame(coach, savedCoaching.getCoach());
        assertSame(client, savedCoaching.getClient());

        verify(clientRepo).save(client);
        verify(clientRepo).findByIdOrThrow(clientId);
        verify(coachRepo).findByIdOrThrow(coachId);
    }

    @Test void endCoaching_detachesCoachAndSetsEndDate() {
        client.setCoach(coach);

        Coaching coaching = new Coaching();
        coaching.setCoach(coach);
        coaching.setClient(client);

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(coachingRepo.findByCoachIdAndClientIdOrThrow(coachId, clientId)).thenReturn(coaching);

        coachingService.endCoaching(clientId);

        assertNull(client.getCoach());
        assertEquals(LocalDate.now(), coaching.getEndDate().toLocalDate());

        verify(clientRepo).findByIdOrThrow(clientId);
        verify(coachingRepo).findByCoachIdAndClientIdOrThrow(coachId, clientId);
    }
}
