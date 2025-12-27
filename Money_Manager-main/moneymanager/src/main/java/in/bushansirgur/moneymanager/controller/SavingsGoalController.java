package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.SavingsGoalDTO;
import in.bushansirgur.moneymanager.dto.SavingsContributionDTO;
import in.bushansirgur.moneymanager.service.SavingsGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/savings-goals")
public class SavingsGoalController {
    @Autowired private SavingsGoalService savingsGoalService;

    @GetMapping
    public ResponseEntity<List<SavingsGoalDTO>> list() {
        return ResponseEntity.ok(savingsGoalService.list());
    }

    @PostMapping
    public ResponseEntity<SavingsGoalDTO> save(@RequestBody SavingsGoalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savingsGoalService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        savingsGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<SavingsContributionDTO>> contributions(@PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.listContributions(id));
    }

    @PostMapping("/{id}/contributions")
    public ResponseEntity<SavingsContributionDTO> addContribution(@PathVariable Long id, @RequestBody SavingsContributionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savingsGoalService.addContribution(id, dto));
    }
}
