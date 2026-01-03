package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.RecurringTransactionDTO;
import in.bushansirgur.moneymanager.service.RecurringTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring-transactions")
public class RecurringTransactionController {
    @Autowired private RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<List<RecurringTransactionDTO>> list() {
        return ResponseEntity.ok(recurringTransactionService.list());
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionDTO> save(@RequestBody RecurringTransactionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransactionService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/process-due")
    public ResponseEntity<List<RecurringTransactionDTO>> processDue() {
        return ResponseEntity.ok(recurringTransactionService.processDue());
    }
}
