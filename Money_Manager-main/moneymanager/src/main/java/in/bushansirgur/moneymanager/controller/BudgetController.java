package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.BudgetDTO;
import in.bushansirgur.moneymanager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {
    @Autowired private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> list(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(budgetService.list(month == null ? now.getMonthValue() : month, year == null ? now.getYear() : year));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> save(@RequestBody BudgetDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
