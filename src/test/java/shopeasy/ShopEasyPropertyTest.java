package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    private final PriceCalculator calculator = new PriceCalculator();

    // ===== ÖZELLIK 1: Monotonicity (Tekdüzelik) =====

    /**
     * ÖZELLİK: Tekdüzelik (Monotonicity)
     * Düz metinle: Sabit bir taban fiyat ve vergi oranı için, indirim oranı arttıkça
     * son fiyat hiçbir zaman artamaz; yani discount1 < discount2 ise, fiyat1 >= fiyat2 olur.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - İndirim hesaplamasındaki logic hataları (mesela discount yerine tersi)
     * - Operatör hataları (- yerine + gibi)
     * - Negatif indirim uygulaması
     */
    @Property
    void priceMonotonicity_increaseInDiscountDecreasesPrice(
            @ForAll @DoubleRange(min = 0.01, max = 1000.0) double basePrice,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double taxRate,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount1,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount2) {

        // discount1 < discount2 koşulunu sağlamak
        double smallerDiscount = Math.min(discount1, discount2);
        double largerDiscount = Math.max(discount1, discount2);

        // Aynı basePrice ve taxRate ile farklı indirimler uygula
        double priceWithSmallerDiscount = calculator.calculate(basePrice, smallerDiscount, taxRate);
        double priceWithLargerDiscount = calculator.calculate(basePrice, largerDiscount, taxRate);

        // AssertJ: İndirim arttığında fiyat azalmalı veya aynı kalmalı
        assertThat(priceWithLargerDiscount)
            .as("Artan indirim oranı fiyatı düşürmelidir veya aynı tutmalıdır")
            .isLessThanOrEqualTo(priceWithSmallerDiscount + 0.01); // float precision tolerance
    }

    // ===== ÖZELLIK 2: Identity (Birim Eleman) =====

    /**
     * ÖZELLİK: Birim Eleman (Identity)
     * Düz metinle: %0 indirim ve %0 vergi uygulandığında, hesaplanan son fiyat
     * tam olarak taban fiyatına eşit olmalıdır. Başka bir deyişle, hiçbir işlem
     * yapılmamışsa giriş = çıkış.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - Yanlış default değerleri (0 yerine 1 gibi)
     * - Vergi veya indirim formülündeki sabitler yanlışsa
     * - Operatör önceliği hataları
     */
    @Property
    void priceIdentity_zeroDiscountAndTaxReturnsBasePrice(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double basePrice) {

        // %0 indirim + %0 vergi
        double result = calculator.calculate(basePrice, 0.0, 0.0);

        // AssertJ: Sonuç taban fiyata eşit olmalı
        assertThat(result)
            .as("Birim eleman: %0 indirim ve %0 vergi = taban fiyat")
            .isCloseTo(basePrice, within(0.0001));
    }

    // ===== ÖZELLIK 3: Boundedness (Sınırlılık) =====

    /**
     * ÖZELLİK: Sınırlılık (Boundedness)
     * Düz metinle: Tüm geçerli girdiler (basePrice >= 0, 0 <= discount <= 100, 0 <= tax <= 100)
     * için hesaplanan fiyat her zaman negatif olmayan (>= 0) bir sayı olmalıdır.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - Yanlış formül implementasyonu (mesela negatif sabit ekleme)
     * - İşaret hataları (- yerine +, veya tam tersi)
     * - Edge case'lerde kütüphane hataları
     */
    @Property
    void priceBoundedness_resultIsAlwaysNonNegative(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double basePrice,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discountRate,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double taxRate) {

        double result = calculator.calculate(basePrice, discountRate, taxRate);

        // AssertJ: Sonuç her zaman >= 0 olmalı
        assertThat(result)
            .as("Sınırlılık: hesaplanan fiyat her zaman >= 0 olmalıdır")
            .isGreaterThanOrEqualTo(-0.0001); // küçük tolerance
    }

    // ===== ÖZELLIK 4: ShoppingCart Commutativity (Değişmelik) =====

    /**
     * ÖZELLİK: Değişmelik (Commutativity)
     * Düz metinle: Sepete ürün ekleme sırası önemli değildir. A'yı sonra B'yi ekleme,
     * B'yi sonra A'yı eklemekle aynı toplam tutarını verir.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - Sıra bağımlı state (order-dependent state)
     * - Hash table collision hataları
     * - Ürün ID karşılaştırmasında hata
     */
    @Property
    void cartCommutativity_orderOfAdditionDoesNotMatter(
            @ForAll("validProducts") Product product1,
            @ForAll("validProducts") Product product2,
            @ForAll @IntRange(min = 1, max = 100) int qty1,
            @ForAll @IntRange(min = 1, max = 100) int qty2) {

        // Pre-condition: aynı ID'ye sahip iki farklı Product nesnesi sepette tek bir
        // satır olarak birleştirildiği için commutativity ancak farklı ürünler için
        // tanımlıdır. jqwik'in shrink ettiği "same-id different-price" counterexample'ı
        // bu varsayımla dışlıyoruz; bulgu rapora ayrıca işlenmiştir.
        Assume.that(!product1.getId().equals(product2.getId()));

        // Scenario 1: A then B
        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(product1, qty1);
        cart1.addItem(product2, qty2);
        double total1 = cart1.total();

        // Scenario 2: B then A
        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(product2, qty2);
        cart2.addItem(product1, qty1);
        double total2 = cart2.total();

        // AssertJ: İki scenario'nun toplamı aynı olmalı
        assertThat(total2)
            .as("Değişmelik: ürün ekleme sırası toplam tutarını etkilemez")
            .isCloseTo(total1, within(0.01));
    }

    // ===== ÖZELLIK 5: ShoppingCart Discount Idempotence (İdempotence) =====

    /**
     * ÖZELLİK: İdempotence (Tekrarlama Özellikleri)
     * Düz metinle: İndirim uygulanmış sepet toplamında tekrar %0 indirim uygulamak,
     * sonucu değiştirmez. Yani applyDiscount(0) çağrısı safe bir operasyondur.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - State mutation hataları (sepet içi gizli değişimler)
     * - Floating-point accumulation hataları
     */
    @Property
    void cartDiscountIdempotence_applyingZeroDiscountTwice(
            @ForAll("validProducts") Product product,
            @ForAll @IntRange(min = 1, max = 50) int qty) {

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, qty);

        double originalTotal = cart.total();
        double afterDiscount1 = cart.applyDiscount(0.0);
        double afterDiscount2 = cart.applyDiscount(0.0);

        // AssertJ: Tüm değerler eşit olmalı
        assertThat(afterDiscount2)
            .as("İdempotence: %0 indirim iki kere uygulansa da sonuç değişmez")
            .isCloseTo(afterDiscount1, within(0.01))
            .isCloseTo(originalTotal, within(0.01));
    }

    // ===== ÖZELLIK 6: ShoppingCart Total Non-Negative (Post-condition) =====

    /**
     * ÖZELLİK: Post-Condition Kontrolü
     * Düz metinle: Sepet üzerinde herhangi bir işlem (ekleme, silme, güncelleme) yapıldıktan sonra
     * total() metodu her zaman negatif olmayan bir değer döndürmelidir.
     *
     * Yakalayabileceği hata türleri (Bug Classes):
     * - Arithmetic overflow / underflow
     * - Başlatılmamış değişkenler
     * - Condition logic hataları
     */
    @Property
    void cartTotalIsAlwaysNonNegative(
            @ForAll("validProducts") Product p1,
            @ForAll("validProducts") Product p2,
            @ForAll @IntRange(min = 1, max = 50) int qty1,
            @ForAll @IntRange(min = 1, max = 50) int qty2,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount) {

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(p1, qty1);
        cart.addItem(p2, qty2);
        cart.applyDiscount(discount);

        double total = cart.total();

        // AssertJ: Her zaman >= 0
        assertThat(total)
            .as("Post-condition: sepet toplamı her zaman >= 0 olmalı")
            .isGreaterThanOrEqualTo(0.0);
    }

    // ===== Custom Arbitrary Provider =====

    /**
     * Özel veri üretici: rastgele Product nesneleri oluşturur.
     * Her Property testi için geçerli, rastgele ürün nesneleri sağlar.
     */
    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                // Product ID: "P" + 3-5 haneli rakam
                Arbitraries.strings()
                    .numeric()
                    .ofLength(3)
                    .map(id -> "P-" + id),
                // Product name: 2-20 karakter alfabetik
                Arbitraries.strings()
                    .alpha()
                    .ofMinLength(2)
                    .ofMaxLength(20),
                // Price: 0.01 to 5000.0
                Arbitraries.doubles()
                    .between(0.01, 5000.0),
                // Stock: 1 to 10000
                Arbitraries.integers()
                    .between(1, 10000)
        ).as((id, name, price, stock) -> new Product(id, name, price, stock));
    }
}
