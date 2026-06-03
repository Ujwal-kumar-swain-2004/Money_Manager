package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.FamilyDTO;
import in.bushansirgur.moneymanager.dto.FamilyMemberDTO;
import in.bushansirgur.moneymanager.dto.FamilyTransferDTO;
import in.bushansirgur.moneymanager.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/families")
public class FamilyController {
    @Autowired
    private FamilyService familyService;

    @GetMapping
    public List<FamilyDTO> getFamilies() {
        return familyService.getFamilies();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyDTO createFamily(@RequestBody FamilyDTO dto) {
        return familyService.createFamily(dto);
    }

    @GetMapping("/{familyId}/dashboard")
    public FamilyDTO getDashboard(@PathVariable Long familyId) {
        return familyService.getDashboard(familyId);
    }

    @GetMapping("/{familyId}/members")
    public List<FamilyMemberDTO> getMembers(@PathVariable Long familyId) {
        return familyService.getMembers(familyId);
    }

    @PostMapping("/{familyId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyMemberDTO addMember(@PathVariable Long familyId, @RequestBody FamilyMemberDTO dto) {
        return familyService.addMember(familyId, dto);
    }

    @GetMapping("/{familyId}/transfers")
    public List<FamilyTransferDTO> getTransfers(@PathVariable Long familyId) {
        return familyService.getTransfers(familyId);
    }

    @PostMapping("/{familyId}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyTransferDTO addTransfer(@PathVariable Long familyId, @RequestBody FamilyTransferDTO dto) {
        return familyService.addTransfer(familyId, dto);
    }
}
