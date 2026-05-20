package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Write an initial test suite based on the specification (Javadoc of ShoppingCart).</li>
 *   <li>Run {@code mvn test} to generate the JaCoCo report:
 *       <pre>  target/site/jacoco/index.html</pre></li>
 *   <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered branches.</li>
 *   <li>Add tests specifically to cover those branches until branch coverage &gt;= 80%.</li>
 *   <li>Take a screenshot of the final JaCoCo summary and put it in {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 *   <li>{@code addItem}: product already in cart vs. new product</li>
 *   <li>{@code removeItem}: product found vs. not found in cart</li>
 *   <li>{@code updateQuantity}: product found vs. not found, quantity valid vs. invalid</li>
 *   <li>{@code applyDiscount}: zero discount, positive discount</li>
 *   <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two surviving mutants,
 * explain why each survived, and describe a test that would kill it. Add this analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // ===== addItem() - Dal Kapsamı: Yeni ürün vs. Mevcut ürün =====

    /**
     * BRANCH: Yeni ürünü sepete ekle (for loop'ta ürün bulunmadı)
     */
    @Test
    void testAddItemNewProduct() {
        // Act: Yeni ürünü sepete ekle
        cart.addItem(apple, 2);

        // Assert: Ürün sepete eklendi
        assertThat(cart.itemCount())
            .as("Yeni ürün eklendikten sonra item sayısı 1 olmalı")
            .isEqualTo(1);
        assertThat(cart.total())
            .as("Toplam fiyat: 2 × 1.50 = 3.00")
            .isCloseTo(3.00, within(0.01));
    }

    /**
     * BRANCH: Mevcut ürünün miktarını arttır (for loop'ta ürün bulundu)
     */
    @Test
    void testAddItemExistingProduct() {
        // Arrange: Ürünü sepete ekle
        cart.addItem(apple, 2);

        // Act: Aynı ürünü tekrar ekle (miktarı arttır)
        cart.addItem(apple, 3);

        // Assert: Ürün sayısı hala 1, miktar 5 olmalı
        assertThat(cart.itemCount())
            .as("Aynı ürün 2 kez eklenince item sayısı 1 kalmalı")
            .isEqualTo(1);
        assertThat(cart.total())
            .as("Toplam fiyat: 5 × 1.50 = 7.50")
            .isCloseTo(7.50, within(0.01));
    }

    /**
     * BRANCH: Farklı ürünler ekle (multiple items)
     */
    @Test
    void testAddMultipleDifferentProducts() {
        // Act: Farklı ürünleri ekle
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);

        // Assert: Her iki ürün de sepette olmalı
        assertThat(cart.itemCount())
            .as("İki farklı ürün eklenince item sayısı 2 olmalı")
            .isEqualTo(2);
        assertThat(cart.total())
            .as("Toplam: (1×1.50) + (2×0.80) = 3.10")
            .isCloseTo(3.10, within(0.01));
    }

    // ===== removeItem() - Dal Kapsamı: Ürün var vs. Ürün yok =====

    /**
     * BRANCH: Sepetteki ürünü sil (product found)
     */
    @Test
    void testRemoveItemProductExists() {
        // Arrange: Ürünü sepete ekle
        cart.addItem(apple, 2);
        cart.addItem(banana, 1);

        // Act: Sepetten ürünü çıkar
        cart.removeItem("P001");

        // Assert: Ürün silinmiş olmalı
        assertThat(cart.itemCount())
            .as("Ürün silinince item sayısı 1 olmalı")
            .isEqualTo(1);
        assertThat(cart.total())
            .as("Toplam: sadece banana kaldı = 0.80")
            .isCloseTo(0.80, within(0.01));
    }

    /**
     * BRANCH: Sepette olmayan ürünü silmeye çalış (product not found)
     */
    @Test
    void testRemoveItemProductNotFound() {
        // Arrange: Sepete ürün ekle (ama P003 eklenmiyor)
        cart.addItem(apple, 2);

        // Act: Olmayan ürünü çıkarmaya çalış (hiçbir şey olmamalı)
        cart.removeItem("P003");

        // Assert: Sepet değişmemeli
        assertThat(cart.itemCount())
            .as("Olmayan ürün silinmeye çalışınca sepet değişmemeli")
            .isEqualTo(1);
        assertThat(cart.total())
            .as("Toplam hala aynı kalmalı")
            .isCloseTo(3.00, within(0.01));
    }

    /**
     * BRANCH: Boş sepetten ürün silinmeye çalışıldığında hiçbir şey olmamalı
     */
    @Test
    void testRemoveItemFromEmptyCart() {
        // Act: Boş sepetten ürün çıkar
        cart.removeItem("P001");

        // Assert: Sepet boş kalmalı
        assertThat(cart.itemCount())
            .as("Boş sepetten ürün silinince item sayısı 0 kalmalı")
            .isEqualTo(0);
    }

    // ===== updateQuantity() - Dal Kapsamı: Ürün var/yok, Geçerli/Geçersiz miktar =====

    /**
     * BRANCH: Sepetteki ürünün miktarını güncelle (product found, valid quantity)
     */
    @Test
    void testUpdateQuantityProductExists() {
        // Arrange: Ürünü sepete ekle
        cart.addItem(apple, 2);

        // Act: Ürünün miktarını güncelle
        cart.updateQuantity("P001", 5);

        // Assert: Miktar güncellenmiş olmalı
        assertThat(cart.total())
            .as("Miktar 5'e güncellendikten sonra toplam: 5 × 1.50 = 7.50")
            .isCloseTo(7.50, within(0.01));
    }

    /**
     * BRANCH: Sepette olmayan ürünün miktarını güncellemeye çalış (product not found)
     */
    @Test
    void testUpdateQuantityProductNotFound() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);

        // Act & Assert: Olmayan ürün güncellenmeye çalışınca hata fırlatmalı
        assertThatThrownBy(() -> cart.updateQuantity("P003", 5))
            .as("Olmayan ürün güncellenmeye çalışınca IllegalArgumentException fırlatmalı")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Product not found");
    }

    /**
     * BRANCH: Geçersiz miktar (0 ve altı) ile güncelleme (invalid quantity)
     */
    @Test
    void testUpdateQuantityZeroQuantity() {
        // Arrange: Ürünü sepete ekle
        cart.addItem(apple, 2);

        // Act & Assert: Sıfır miktar için hata fırlatmalı
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
            .as("Sıfır miktar ile güncelleme IllegalArgumentException fırlatmalı")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    /**
     * BRANCH: Negatif miktar ile güncelleme (invalid quantity)
     */
    @Test
    void testUpdateQuantityNegativeQuantity() {
        // Arrange: Ürünü sepete ekle
        cart.addItem(apple, 2);

        // Act & Assert: Negatif miktar için hata fırlatmalı
        assertThatThrownBy(() -> cart.updateQuantity("P001", -5))
            .as("Negatif miktar ile güncelleme IllegalArgumentException fırlatmalı")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    // ===== applyDiscount() - Dal Kapsamı: %0 indirim vs. pozitif indirim =====

    /**
     * BRANCH: %0 indirim uygulandığında (discountRate = 0)
     */
    @Test
    void testApplyDiscountZeroPercent() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);
        double originalTotal = cart.total(); // 3.00

        // Act: %0 indirim uygula
        double discountedTotal = cart.applyDiscount(0.0);

        // Assert: İndirim uygulanmadığından aynı kalmalı
        assertThat(discountedTotal)
            .as("%0 indirim uygulanınca toplam değişmemeli")
            .isCloseTo(originalTotal, within(0.01));
    }

    /**
     * BRANCH: Pozitif indirim uygulandığında (discountRate > 0)
     */
    @Test
    void testApplyDiscountPositivePercent() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);
        double originalTotal = cart.total(); // 3.00

        // Act: %25 indirim uygula
        double discountedTotal = cart.applyDiscount(25.0);

        // Assert: İndirim tutarı 0.75 olmalı
        assertThat(discountedTotal)
            .as("%25 indirim uygulanırsa: 3.00 × 0.75 = 2.25")
            .isCloseTo(2.25, within(0.01));
    }

    /**
     * BRANCH: %100 indirim uygulandığında (edge case)
     */
    @Test
    void testApplyDiscountFullDiscount() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);

        // Act: %100 indirim uygula
        double discountedTotal = cart.applyDiscount(100.0);

        // Assert: Toplam sıfır olmalı
        assertThat(discountedTotal)
            .as("%100 indirim uygulanırsa toplam 0 olmalı")
            .isCloseTo(0.0, within(0.01));
    }

    /**
     * BRANCH: İndirim orijinal toplam ile sepet toplamını etkilemez
     */
    @Test
    void testApplyDiscountDoesNotPersist() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);

        // Act: İndirim uygula
        cart.applyDiscount(50.0);

        // Assert: Sepet toplam değişmemeli (indirim kalıcı değil)
        assertThat(cart.total())
            .as("İndirim sepet toplamını etkilemez")
            .isCloseTo(3.00, within(0.01));
    }

    // ===== total() - Dal Kapsamı: Boş sepet vs. Dolu sepet =====

    /**
     * BRANCH: Boş sepette total() çağrıldığında (empty cart)
     */
    @Test
    void testTotalEmptyCart() {
        // Act: Boş sepette toplam hesapla
        double total = cart.total();

        // Assert: Toplam sıfır olmalı
        assertThat(total)
            .as("Boş sepette toplam 0 olmalı")
            .isCloseTo(0.0, within(0.01));
    }

    /**
     * BRANCH: Dolu sepette total() çağrıldığında (non-empty cart)
     */
    @Test
    void testTotalNonEmptyCart() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);

        // Act: Toplam hesapla
        double total = cart.total();

        // Assert: Toplam doğru hesaplanmalı
        assertThat(total)
            .as("Toplam: (2 × 1.50) + (3 × 0.80) = 5.40")
            .isCloseTo(5.40, within(0.01));
    }

    /**
     * BRANCH: Ürün silindikten sonra boş sepette total()
     */
    @Test
    void testTotalAfterRemovingAllItems() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);

        // Act: Ürünü sil
        cart.removeItem("P001");
        double total = cart.total();

        // Assert: Toplam sıfır olmalı
        assertThat(total)
            .as("Tüm ürünler silinince toplam 0 olmalı")
            .isCloseTo(0.0, within(0.01));
    }

    // ===== Ek Kombinasyonal Testler (Edge Cases & Invariant) =====

    /**
     * INVARIANT: total() her zaman >= 0 olmalı
     */
    @Test
    void testInvariantTotalAlwaysNonNegative() {
        // Act: Çeşitli işlemler yap
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);
        cart.applyDiscount(50.0);
        cart.removeItem("P001");
        cart.updateQuantity("P002", 5);

        double total = cart.total();

        // Assert: total her zaman >= 0
        assertThat(total)
            .as("POST-CONDITION: total() her zaman >= 0 olmalı")
            .isGreaterThanOrEqualTo(0.0);
    }

    /**
     * Test: itemCount() metodu
     */
    @Test
    void testItemCount() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);

        // Assert: itemCount doğru döndürmeli
        assertThat(cart.itemCount())
            .as("itemCount() 2 farklı ürün için 2 döndürmeli")
            .isEqualTo(2);
    }

    /**
     * Test: clear() metodu
     */
    @Test
    void testClearCart() {
        // Arrange: Sepete ürün ekle
        cart.addItem(apple, 2);

        // Act: Sepeti temizle
        cart.clear();

        // Assert: Sepet boş olmalı
        assertThat(cart.itemCount())
            .as("clear() çağrısı sonrasında itemCount 0 olmalı")
            .isEqualTo(0);
        assertThat(cart.total())
            .as("clear() çağrısı sonrasında total 0 olmalı")
            .isCloseTo(0.0, within(0.01));
    }

}
