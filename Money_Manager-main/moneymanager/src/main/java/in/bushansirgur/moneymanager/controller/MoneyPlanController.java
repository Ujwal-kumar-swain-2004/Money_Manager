package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.MoneyPlanSummaryDTO;
import in.bushansirgur.moneymanager.service.MoneyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/money-plan")
public class MoneyPlanController {
    @Autowired private MoneyPlanService moneyPlanService;

    @GetMapping("/summary")
    public ResponseEntity<MoneyPlanSummaryDTO> summary(@RequestParam(required = false) Integer month,
                                                       @RequestParam(required = false) Integer year,
                                                       @RequestParam(defaultValue = "30") Integer forecastDays) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(moneyPlanService.summary(month == null ? now.getMonthValue() : month, year == null ? now.getYear() : year, forecastDays));
    }
}
