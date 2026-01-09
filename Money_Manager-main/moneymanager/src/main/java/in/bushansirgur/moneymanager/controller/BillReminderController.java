package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.BillReminderDTO;
import in.bushansirgur.moneymanager.service.BillReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bill-reminders")
public class BillReminderController {
    @Autowired private BillReminderService billReminderService;

    @GetMapping
    public ResponseEntity<List<BillReminderDTO>> list() {
        return ResponseEntity.ok(billReminderService.list());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<BillReminderDTO>> upcoming(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(billReminderService.upcoming(days));
    }

    @PostMapping
    public ResponseEntity<BillReminderDTO> save(@RequestBody BillReminderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billReminderService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billReminderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
