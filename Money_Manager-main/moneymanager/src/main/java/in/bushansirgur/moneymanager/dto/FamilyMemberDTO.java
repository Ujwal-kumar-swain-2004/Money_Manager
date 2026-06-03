package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;

public class FamilyMemberDTO {
    private Long id;
    private Long familyId;
    private String name;
    private String role;
    private String avatar;
    private BigDecimal monthlyAllowance;
    private BigDecimal monthSpent;
    private BigDecimal monthReceived;
    private BigDecimal allowanceRemaining;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public BigDecimal getMonthlyAllowance() { return monthlyAllowance; }
    public void setMonthlyAllowance(BigDecimal monthlyAllowance) { this.monthlyAllowance = monthlyAllowance; }
    public BigDecimal getMonthSpent() { return monthSpent; }
    public void setMonthSpent(BigDecimal monthSpent) { this.monthSpent = monthSpent; }
    public BigDecimal getMonthReceived() { return monthReceived; }
    public void setMonthReceived(BigDecimal monthReceived) { this.monthReceived = monthReceived; }
    public BigDecimal getAllowanceRemaining() { return allowanceRemaining; }
    public void setAllowanceRemaining(BigDecimal allowanceRemaining) { this.allowanceRemaining = allowanceRemaining; }
}
