import java.math.BigDecimal;

public abstract class Deduction {
    private final String deductionName;
    private String description;

    public Deduction(String deductionName, String description) {
        if (deductionName == null || description == null) {
            throw new IllegalArgumentException("Deduction name and description cannot be null");
        }
        this.deductionName = deductionName.trim();
        this.description = description;
    }

    public String getDeductionName() {
        return deductionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract BigDecimal computeDeduction();
}
