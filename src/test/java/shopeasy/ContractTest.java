package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 3 – Design by Contract (Chapter 4)
 *
 * <p>This task has two parts:
 *
 * <h3>Part A – Add contracts to production code</h3>
 * Open {@link ShoppingCart} and {@link PriceCalculator} and add {@code assert}
 * statements for the pre-conditions and post-conditions described in their Javadoc.
 * Note: assertions are enabled via {@code -ea} in Maven Surefire (already configured
 * in {@code pom.xml}).
 *
 * <p>Contracts to implement:
 * <ul>
 *   <li><b>ShoppingCart.addItem</b>: pre — {@code product != null}, {@code quantity > 0};
 *       post — {@code itemCount()} increased or product quantity updated.</li>
 *   <li><b>ShoppingCart.applyDiscount</b>: pre — {@code 0 <= discountRate <= 100};
 *       post — result &lt;= {@code total()} when {@code discountRate > 0}.</li>
 *   <li><b>PriceCalculator.calculate</b>: pre — {@code basePrice >= 0},
 *       {@code 0 <= discountRate <= 100}, {@code 0 <= taxRate <= 100};
 *       post — result {@code >= 0}.</li>
 *   <li><b>ShoppingCart invariant</b>: {@code total() >= 0} after any operation.</li>
 * </ul>
 *
 * <h3>Part B – Write contract tests</h3>
 * Write tests below that:
 * <ol>
 *   <li>Verify contracts hold for valid inputs (positive tests).</li>
 *   <li>Verify contracts are violated ({@code AssertionError}) for invalid inputs (negative tests).</li>
 * </ol>
 *
 * <p>Use {@code assertThatThrownBy(...).isInstanceOf(AssertionError.class)} to test violations.
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // ===== ShoppingCart.addItem() - Pre-Condition Tests =====

    /**
     * POSITIVE: Valid inputs (product != null, quantity > 0)
     */
    @Test
    void addItem_validInputs_shouldNotThrow() {
        assertThatCode(() -> cart.addItem(product, 5))
            .as("addItem with valid inputs should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * NEGATIVE: Null product violates pre-condition
     */
    @Test
    void addItem_nullProduct_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(null, 1))
            .as("addItem with null product should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Product must not be null");
    }

    /**
     * NEGATIVE: Zero quantity violates pre-condition
     */
    @Test
    void addItem_zeroQuantity_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, 0))
            .as("addItem with zero quantity should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    /**
     * NEGATIVE: Negative quantity violates pre-condition
     */
    @Test
    void addItem_negativeQuantity_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, -5))
            .as("addItem with negative quantity should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    /**
     * POSITIVE: Post-condition — product exists in cart after adding
     */
    @Test
    void addItem_afterAddition_shouldHaveProductInCart() {
        cart.addItem(product, 3);
        
        assertThat(cart.itemCount())
            .as("Cart should have 1 item after adding product")
            .isEqualTo(1);
    }

    /**
     * POSITIVE: Post-condition — adding duplicate product updates quantity
     */
    @Test
    void addItem_duplicateProduct_shouldUpdateQuantity() {
        cart.addItem(product, 2);
        cart.addItem(product, 3);

        assertThat(cart.itemCount())
            .as("Cart should still have 1 item (quantity was updated)")
            .isEqualTo(1);
        assertThat(cart.total())
            .as("Total should be (2+3) * 10.0 = 50.0")
            .isCloseTo(50.0, within(0.01));
    }

    // ===== ShoppingCart.applyDiscount() - Pre-Condition Tests =====

    /**
     * POSITIVE: Valid discount rate (0%)
     */
    @Test
    void applyDiscount_zeroPercent_shouldNotThrow() {
        cart.addItem(product, 1);
        
        assertThatCode(() -> cart.applyDiscount(0.0))
            .as("applyDiscount with 0% should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * POSITIVE: Valid discount rate (50%)
     */
    @Test
    void applyDiscount_fiftyPercent_shouldNotThrow() {
        cart.addItem(product, 1);
        
        assertThatCode(() -> cart.applyDiscount(50.0))
            .as("applyDiscount with 50% should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * POSITIVE: Valid discount rate (100%)
     */
    @Test
    void applyDiscount_hundredPercent_shouldNotThrow() {
        cart.addItem(product, 1);
        
        assertThatCode(() -> cart.applyDiscount(100.0))
            .as("applyDiscount with 100% should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * NEGATIVE: Negative discount rate violates pre-condition
     */
    @Test
    void applyDiscount_negativeRate_shouldViolatePreCondition() {
        cart.addItem(product, 1);
        
        assertThatThrownBy(() -> cart.applyDiscount(-10.0))
            .as("applyDiscount with negative rate should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Discount rate must be in [0, 100]");
    }

    /**
     * NEGATIVE: Discount rate > 100 violates pre-condition
     */
    @Test
    void applyDiscount_rateAbove100_shouldViolatePreCondition() {
        cart.addItem(product, 1);
        
        assertThatThrownBy(() -> cart.applyDiscount(150.0))
            .as("applyDiscount with rate > 100 should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Discount rate must be in [0, 100]");
    }

    /**
     * POSITIVE: Post-condition — discounted total <= original total
     */
    @Test
    void applyDiscount_positiveRate_shouldReturnLessThanOriginal() {
        cart.addItem(product, 1); // total = 10.0
        double discounted = cart.applyDiscount(25.0);

        assertThat(discounted)
            .as("25% discount on 10.0 should be 7.5")
            .isCloseTo(7.5, within(0.01));
        assertThat(discounted)
            .as("Discounted total must be <= original total")
            .isLessThanOrEqualTo(cart.total());
    }

    /**
     * POSITIVE: Invariant — total >= 0 after applyDiscount
     */
    @Test
    void applyDiscount_afterDiscount_invariantHolds() {
        cart.addItem(product, 1);
        cart.applyDiscount(100.0);

        assertThat(cart.total())
            .as("Invariant: total() must be >= 0 after any operation")
            .isGreaterThanOrEqualTo(0.0);
    }

    // ===== PriceCalculator.calculate() - Pre-Condition Tests =====

    /**
     * POSITIVE: All valid parameters
     */
    @Test
    void calculate_validInputs_shouldNotThrow() {
        assertThatCode(() -> calculator.calculate(100.0, 10.0, 20.0))
            .as("calculate with valid inputs should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * POSITIVE: Boundary values (0% discount, 0% tax)
     */
    @Test
    void calculate_minDiscountMinTax_shouldNotThrow() {
        assertThatCode(() -> calculator.calculate(50.0, 0.0, 0.0))
            .as("calculate with 0% discount and 0% tax should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * POSITIVE: Boundary values (100% discount, 100% tax)
     */
    @Test
    void calculate_maxDiscountMaxTax_shouldNotThrow() {
        assertThatCode(() -> calculator.calculate(100.0, 100.0, 100.0))
            .as("calculate with 100% discount and 100% tax should not throw")
            .doesNotThrowAnyException();
    }

    /**
     * NEGATIVE: Negative basePrice violates pre-condition
     */
    @Test
    void calculate_negativeBasePrice_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(-10.0, 10.0, 20.0))
            .as("calculate with negative basePrice should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Base price must be >= 0");
    }

    /**
     * NEGATIVE: Negative discountRate violates pre-condition
     */
    @Test
    void calculate_negativeDiscount_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100.0, -5.0, 20.0))
            .as("calculate with negative discountRate should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Discount rate must be in [0, 100]");
    }

    /**
     * NEGATIVE: DiscountRate > 100 violates pre-condition
     */
    @Test
    void calculate_discountAbove100_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 150.0, 20.0))
            .as("calculate with discountRate > 100 should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Discount rate must be in [0, 100]");
    }

    /**
     * NEGATIVE: Negative taxRate violates pre-condition
     */
    @Test
    void calculate_negativeTax_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 10.0, -5.0))
            .as("calculate with negative taxRate should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Tax rate must be in [0, 100]");
    }

    /**
     * NEGATIVE: TaxRate > 100 violates pre-condition
     */
    @Test
    void calculate_taxAbove100_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 10.0, 150.0))
            .as("calculate with taxRate > 100 should throw AssertionError")
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Tax rate must be in [0, 100]");
    }

    /**
     * POSITIVE: Post-condition — result >= 0
     */
    @Test
    void calculate_resultIsNonNegative() {
        double result = calculator.calculate(100.0, 50.0, 25.0);

        assertThat(result)
            .as("Post-condition: result must be >= 0")
            .isGreaterThanOrEqualTo(0.0);
        // Verify: 100 * (1 - 50/100) * (1 + 25/100) = 50 * 1.25 = 62.5
        assertThat(result)
            .as("Calculation: 100 * 0.5 * 1.25 = 62.5")
            .isCloseTo(62.5, within(0.01));
    }

    /**
     * POSITIVE: Post-condition — result >= 0 even with 100% discount
     */
    @Test
    void calculate_maxDiscountResultIsZero() {
        double result = calculator.calculate(100.0, 100.0, 50.0);

        assertThat(result)
            .as("100% discount results in 0 (tax doesn't apply)")
            .isCloseTo(0.0, within(0.01));
        assertThat(result)
            .as("Post-condition: result must be >= 0")
            .isGreaterThanOrEqualTo(0.0);
    }

    /**
     * POSITIVE: Edge case — zero basePrice
     */
    @Test
    void calculate_zeroBasePrice_shouldReturnZero() {
        double result = calculator.calculate(0.0, 50.0, 100.0);

        assertThat(result)
            .as("Zero basePrice always results in 0")
            .isCloseTo(0.0, within(0.01));
        assertThat(result)
            .as("Post-condition: result must be >= 0")
            .isGreaterThanOrEqualTo(0.0);
    }
}
