package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.AnalyticsDTO;
import in.bushansirgur.moneymanager.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    @Autowired private AnalyticsService analyticsService;

    @GetMapping("/monthly")
    public ResponseEntity<AnalyticsDTO> monthly(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getMonthlyAnalytics(month == null ? now.getMonthValue() : month, year == null ? now.getYear() : year));
    }
}
