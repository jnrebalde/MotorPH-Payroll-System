import java.math.BigDecimal;

public class LoanDeduction extends Deduction {
    private final BigDecimal loanAmount;
    private final BigDecimal deductionPerPayroll;
    private BigDecimal remainingBalance;

    public LoanDeduction(String deductionName, String description,
                         BigDecimal loanAmount, BigDecimal deductionPerPayroll) {
        super(deductionName, description);

        if (loanAmount == null || deductionPerPayroll == null ||
                loanAmount.compareTo(deductionPerPayroll) <= 0 ||
                deductionPerPayroll.compareTo(loanAmount) <= 0) {
            throw new IllegalArgumentException("Loan amount and deduction must be greater than 0.");
        }

        if (deductionPerPayroll.compareTo(loanAmount) > 0) {
            throw new IllegalArgumentException(
                    "Deduction cannot exceed loan amount.");
        }
        this.loanAmount = loanAmount;
        this.deductionPerPayroll = deductionPerPayroll;
        this.remainingBalance = loanAmount;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public BigDecimal getDeductionPerPayroll() {
        return deductionPerPayroll;
    }
    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    @Override
    public BigDecimal computeDeduction() {
        return deductionPerPayroll.min(remainingBalance);
    }

    public void recordDeduction() {
        BigDecimal deduction = computeDeduction();
        remainingBalance = remainingBalance.subtract(deduction);
    }

}
