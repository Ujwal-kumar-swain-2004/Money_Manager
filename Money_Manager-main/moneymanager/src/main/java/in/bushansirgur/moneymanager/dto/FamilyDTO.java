package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.util.List;

public class FamilyDTO {
    private Long id;
    private String name;
    private String inviteCode;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalBalance;
    private List<FamilyMemberDTO> members;
    private List<FamilyTransferDTO> transfers;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
    public List<FamilyMemberDTO> getMembers() { return members; }
    public void setMembers(List<FamilyMemberDTO> members) { this.members = members; }
    public List<FamilyTransferDTO> getTransfers() { return transfers; }
    public void setTransfers(List<FamilyTransferDTO> transfers) { this.transfers = transfers; }
}
