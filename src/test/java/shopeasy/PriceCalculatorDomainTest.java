package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Domain Testing (Bölüm 2) - PriceCalculator.calculate() için test senaryoları
 *
 * Teknik: Eşdeğerlik Bölgesi (Equivalence Partitioning) + Sınır Değer Analizi (BVA)
 *
 * İncelenen parametreler:
 * - basePrice: [0, ∞)
 * - discountRate: [0, 100]
 * - taxRate: [0, 100]
 *
 * Sınırlar:
 * - basePrice: 0 (alt sınır), çok büyük sayı (üst sınır)
 * - discountRate: 0 (alt sınır), 50 (orta), 100 (üst sınır)
 * - taxRate: 0 (alt sınır), 50 (orta), 100 (üst sınır)
 */
@DisplayName("PriceCalculator - Domain Testing (Eşdeğerlik Bölgesi & Sınır Değer Analizi)")
public class PriceCalculatorDomainTest {

    private final PriceCalculator calculator = new PriceCalculator();

    /**
     * Parametreli testler: Eşdeğerlik Bölgeleri ve Sınır Değer Analizi
     *
     * CSV Formatı: basePrice, discountRate, taxRate, expectedResult, testDescription
     */
    @ParameterizedTest(name = "{5}")
    @CsvSource({
        // ===== SENARYO 1: Tüm Parametreler Sıfır (Sınır Değer: Alt Sınırlar) =====
        "0.0, 0.0, 0.0, 0.0, Sıfır fiyat/indirim/vergi",

        // ===== SENARYO 2: Pozitif Fiyat, İndirim Yok, Vergi Yok (Eşdeğerlik Bölgesi: Temel Durum) =====
        "100.0, 0.0, 0.0, 100.0, Basit fiyat (indirim/vergi yok)",

        // ===== SENARYO 3: İndirim Sınır Değeri - %50 (Orta Değer) =====
        "100.0, 50.0, 0.0, 50.0, %50 indirim, vergi yok",

        // ===== SENARYO 4: Vergi Sınır Değeri - %20 (Orta Değer) =====
        "100.0, 0.0, 20.0, 120.0, İndirim yok, %20 vergi",

        // ===== SENARYO 5: İndirim ve Vergi Birlikte (Orta Değerler) =====
        "100.0, 50.0, 20.0, 60.0, %50 indirim ve %20 vergi",

        // ===== SENARYO 6: İndirim Sınır Değeri - %100 (Maksimum İndirim) =====
        "100.0, 100.0, 0.0, 0.0, Maksimum indirim (%100)",

        // ===== SENARYO 7: %100 İndirim + Vergi (Vergi Hesaplanmaz) =====
        "100.0, 100.0, 50.0, 0.0, %100 indirim sıfır sonuç verir, vergi etkin değil",

        // ===== SENARYO 8: Vergi Sınır Değeri - %100 (Maksimum Vergi) =====
        "100.0, 0.0, 100.0, 200.0, İndirim yok, maksimum vergi (%100)",

        // ===== SENARYO 9: Çok Büyük Fiyat (Büyük Sayı Testi) =====
        "1000000.0, 10.0, 10.0, 990000.0, Büyük fiyat: 1M, %10 indirim, %10 vergi",

        // ===== SENARYO 10: Çok Küçük Fiyat (Ondalık Kesinlik) =====
        "0.01, 50.0, 10.0, 0.0055, Çok küçük fiyat, hassasiyet testi",

        // ===== SENARYO 11: Minimum İndirim (%1 - Sınır Değeri) =====
        "100.0, 1.0, 0.0, 99.0, Minimum indirim %1",

        // ===== SENARYO 12: Maksimum İndirime Yakın (%99 - Sınır Değeri) =====
        "100.0, 99.0, 0.0, 1.0, Maksimuma yakın indirim %99",

        // ===== SENARYO 13: Minimum Vergi (%1 - Sınır Değeri) =====
        "100.0, 0.0, 1.0, 101.0, Minimum vergi %1",

        // ===== SENARYO 14: Maksimum Vergie Yakın (%99 - Sınır Değeri) =====
        "100.0, 0.0, 99.0, 199.0, Maksimuma yakın vergi %99",

        // ===== SENARYO 15: Sınır Değerleri Kombinasyonu (Geçerli Aralık İçi) =====
        "50.0, 25.0, 15.0, 43.125, İdeal kombinasyon: 50 fiyat, %25 indirim, %15 vergi"
    })
    @DisplayName("Parametreli Test: Domain Testing Senaryoları")
    void testCalculateWithDomainPartitions(
            double basePrice,
            double discountRate,
            double taxRate,
            double expectedResult,
            String description) {

        // Act: Hesapla
        double result = calculator.calculate(basePrice, discountRate, taxRate);

        // Assert: AssertJ ile fluent assertion
        assertThat(result)
            .as("Test: %s", description)
            .isCloseTo(expectedResult, within(0.0001));
    }

    /**
     * İndirim Sınır Değerleri (Boundary Value Analysis)
     * discountRate = 0, 0.1, 50, 99.9, 100
     */
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "100.0, 0.0, 100.0, İndirim alt sınırı: 0%",
        "100.0, 0.1, 99.9, İndirim: 0.1%",
        "100.0, 50.0, 50.0, İndirim: 50% (orta değer)",
        "100.0, 99.9, 0.1, İndirim: 99.9%",
        "100.0, 100.0, 0.0, İndirim üst sınırı: 100%"
    })
    @DisplayName("Sınır Değer Analizi: İndirim Oranı")
    void testDiscountBoundaryValues(
            double basePrice,
            double discountRate,
            double expectedAfterDiscount,
            String description) {

        // Act: İndirim hesapla (vergi = 0)
        double result = calculator.calculate(basePrice, discountRate, 0.0);

        // Assert
        assertThat(result)
            .as("Test: %s", description)
            .isCloseTo(expectedAfterDiscount, within(0.0001));
    }

    /**
     * Vergi Sınır Değerleri (Boundary Value Analysis)
     * taxRate = 0, 0.1, 50, 99.9, 100
     */
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "100.0, 0.0, 100.0, Vergi alt sınırı: 0%",
        "100.0, 0.1, 100.1, Vergi: 0.1%",
        "100.0, 50.0, 150.0, Vergi: 50% (orta değer)",
        "100.0, 99.9, 199.9, Vergi: 99.9%",
        "100.0, 100.0, 200.0, Vergi üst sınırı: 100%"
    })
    @DisplayName("Sınır Değer Analizi: Vergi Oranı")
    void testTaxBoundaryValues(
            double basePrice,
            double taxRate,
            double expectedWithTax,
            String description) {

        // Act: Vergi hesapla (indirim = 0)
        double result = calculator.calculate(basePrice, 0.0, taxRate);

        // Assert
        assertThat(result)
            .as("Test: %s", description)
            .isCloseTo(expectedWithTax, within(0.0001));
    }

    /**
     * Fiyat Sınır Değerleri (Boundary Value Analysis)
     * basePrice = 0, 0.01, 100, 1000000
     */
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "0.0, 0.0, Fiyat alt sınırı: 0 (sıfır)",
        "0.01, 0.01, Fiyat: 0.01 (çok küçük)",
        "100.0, 100.0, Fiyat: 100 (normal)",
        "1000000.0, 1000000.0, Fiyat: 1.000.000 (çok büyük)"
    })
    @DisplayName("Sınır Değer Analizi: Taban Fiyat")
    void testBasePriceBoundaryValues(
            double basePrice,
            double expectedResult,
            String description) {

        // Act: Hiç indirim/vergi olmadan
        double result = calculator.calculate(basePrice, 0.0, 0.0);

        // Assert
        assertThat(result)
            .as("Test: %s", description)
            .isCloseTo(expectedResult, within(0.0001));
    }

    /**
     * Kombinasyonal Sınır Değer Analizi
     * İndirim ve Vergi'nin aynı anda sınır değerlerinde olduğu durumlar
     */
    @ParameterizedTest(name = "{3}")
    @CsvSource({
        // Her iki parametre de sıfır
        "100.0, 0.0, 0.0, 100.0, Her iki sınır zero: İndirim 0%, Vergi 0%",
        // Her iki parametre de maksimum
        "100.0, 100.0, 100.0, 0.0, Her iki sınır max: İndirim 100%, Vergi 100% (sonuç sıfır)",
        // İndirim maksimum, vergi minimum
        "100.0, 100.0, 0.0, 0.0, İndirim max, vergi min",
        // İndirim minimum, vergi maksimum
        "100.0, 0.0, 100.0, 200.0, İndirim min, vergi max",
        // Her ikisi de orta değer
        "100.0, 50.0, 50.0, 75.0, Her ikisi de orta: %50 indirim, %50 vergi"
    })
    @DisplayName("Kombinasyonal Sınır Değer Analizi")
    void testCombinedBoundaryValues(
            double basePrice,
            double discountRate,
            double taxRate,
            double expectedResult,
            String description) {

        // Act
        double result = calculator.calculate(basePrice, discountRate, taxRate);

        // Assert
        assertThat(result)
            .as("Test: %s", description)
            .isCloseTo(expectedResult, within(0.0001));
    }

    /**
     * Matematiksel İlişkiler Testi
     * Negatif olmayan sonuç garantisi (Post-condition)
     */
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "0.0, 0.0, 0.0, Tüm parametreler sıfır",
        "100.0, 100.0, 100.0, Maksimum indirim (vergi etkin değil)",
        "0.01, 99.9, 99.9, Çok küçük fiyat, maksimuma yakın indirim"
    })
    @DisplayName("Post-Condition: Sonuç her zaman >= 0")
    void testResultAlwaysNonNegative(
            double basePrice,
            double discountRate,
            double taxRate,
            String description) {

        // Act
        double result = calculator.calculate(basePrice, discountRate, taxRate);

        // Assert: Post-condition kontrolü
        assertThat(result)
            .as("Post-condition kontrol: %s - Sonuç >= 0 olmalı", description)
            .isGreaterThanOrEqualTo(0.0);
    }
}
