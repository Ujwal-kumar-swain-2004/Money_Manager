package in.bushansirgur.moneymanager.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_shared_expense_splits", indexes = {
        @Index(name = "idx_shared_split_expense", columnList = "expense_id"),
        @Index(name = "idx_shared_split_friend", columnList = "friend_id")
})
public class SharedExpenseSplitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private BigDecimal percentValue;
    private BigDecimal shares;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private SharedExpenseEntity expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private FriendEntity friend;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPercentValue() { return percentValue; }
    public void setPercentValue(BigDecimal percentValue) { this.percentValue = percentValue; }
    public BigDecimal getShares() { return shares; }
    public void setShares(BigDecimal shares) { this.shares = shares; }
    public SharedExpenseEntity getExpense() { return expense; }
    public void setExpense(SharedExpenseEntity expense) { this.expense = expense; }
    public FriendEntity getFriend() { return friend; }
    public void setFriend(FriendEntity friend) { this.friend = friend; }
}
