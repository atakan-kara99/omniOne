package app.omniOne.service;

import app.omniOne.model.dto.SupplementPlanRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.SupplementEntry;
import app.omniOne.model.entity.SupplementPlan;
import app.omniOne.model.mapper.SupplementEntryMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.SupplementPlanRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplementPlanService {

    private final ClientRepo clientRepo;
    private final SupplementPlanRepo supplementPlanRepo;
    private final SupplementEntryMapper supplementEntryMapper;

    @Transactional
    public SupplementPlan addSupplementPlan(UUID clientId, SupplementPlanRequest request) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        SupplementPlan plan = new SupplementPlan();
        plan.setClient(client);
        List<SupplementEntry> entries = request.entries().stream()
                .map(entryRequest -> {
                    SupplementEntry entry = supplementEntryMapper.map(entryRequest);
                    entry.setSupplementPlan(plan);
                    return entry;
                })
                .toList();
        plan.getSupplementEntries().addAll(entries);
        SupplementPlan saved = supplementPlanRepo.save(plan);
        log.info("SupplementPlan added (clientId={}, planId={})", clientId, saved.getId());
        return saved;
    }

    public SupplementPlan getActiveSupplementPlan(UUID clientId) {
        return supplementPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId);
    }

    public List<SupplementPlan> getSupplementPlans(UUID clientId) {
        clientRepo.findByIdOrThrow(clientId);
        return supplementPlanRepo.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public SupplementPlan correctSupplementPlan(UUID clientId, Long planId, SupplementPlanRequest request) {
        clientRepo.findByIdOrThrow(clientId);
        SupplementPlan plan = supplementPlanRepo.findByIdAndClientIdOrThrow(planId, clientId);
        plan.getSupplementEntries().clear();
        List<SupplementEntry> newEntries = request.entries().stream()
                .map(entryRequest -> {
                    SupplementEntry entry = supplementEntryMapper.map(entryRequest);
                    entry.setSupplementPlan(plan);
                    return entry;
                })
                .toList();
        plan.getSupplementEntries().addAll(newEntries);
        SupplementPlan saved = supplementPlanRepo.save(plan);
        log.info("SupplementPlan corrected (clientId={}, planId={})", clientId, saved.getId());
        return saved;
    }

    public void deleteSupplementPlan(UUID clientId, Long planId) {
        clientRepo.findByIdOrThrow(clientId);
        SupplementPlan plan = supplementPlanRepo.findByIdAndClientIdOrThrow(planId, clientId);
        supplementPlanRepo.delete(plan);
        log.info("SupplementPlan deleted (clientId={}, planId={})", clientId, planId);
    }

}
