# PriceCalculator Domain Testing - Test Senaryoları Açıklaması

## 📋 Genel Bakış

`PriceCalculatorDomainTest` sınıfı, **Bölüm 2: Domain Testing** tekniklerini kullanarak `PriceCalculator.calculate()` metodu için **15+ kapsamlı test senaryosu** içerir.

### Kullanılan Teknikler:
1. **Eşdeğerlik Bölgesi (Equivalence Partitioning)**: Parametreleri mantıksal gruplara ayırmak
2. **Sınır Değer Analizi (Boundary Value Analysis - BVA)**: Sınır noktalarında test etmek

---

## 🎯 Test Metodları ve Senaryoları

### 1️⃣ **testCalculateWithDomainPartitions()** - Ana Parametreli Test
**15 test senaryosu içerir - Eşdeğerlik Bölgeleri:**

| # | basePrice | discountRate | taxRate | Beklenen Sonuç | Test Alanı |
|---|-----------|--------------|---------|--------|-----------|
| 1 | 0.0 | 0.0 | 0.0 | 0.0 | ✓ Tüm sınır değerleri (Zero Boundaries) |
| 2 | 100.0 | 0.0 | 0.0 | 100.0 | ✓ Temel durum (No Discount/Tax) |
| 3 | 100.0 | 50.0 | 0.0 | 50.0 | ✓ Orta değer indirim |
| 4 | 100.0 | 0.0 | 20.0 | 120.0 | ✓ Orta değer vergi |
| 5 | 100.0 | 50.0 | 20.0 | 60.0 | ✓ Kombinasyon (50% indirim + 20% vergi) |
| 6 | 100.0 | 100.0 | 0.0 | 0.0 | ✓ Maksimum indirim (%100) |
| 7 | 100.0 | 100.0 | 50.0 | 0.0 | ✓ 100% indirim (vergi etkisiz) |
| 8 | 100.0 | 0.0 | 100.0 | 200.0 | ✓ Maksimum vergi (%100) |
| 9 | 1,000,000.0 | 10.0 | 10.0 | 990,000.0 | ✓ Büyük sayı işleme |
| 10 | 0.01 | 50.0 | 10.0 | 0.0055 | ✓ Çok küçük sayı (ondalık kesinlik) |
| 11 | 100.0 | 1.0 | 0.0 | 99.0 | ✓ Minimum indirim (%1) |
| 12 | 100.0 | 99.0 | 0.0 | 1.0 | ✓ Maksimuma yakın indirim (%99) |
| 13 | 100.0 | 0.0 | 1.0 | 101.0 | ✓ Minimum vergi (%1) |
| 14 | 100.0 | 0.0 | 99.0 | 199.0 | ✓ Maksimuma yakın vergi (%99) |
| 15 | 50.0 | 25.0 | 15.0 | 43.125 | ✓ İdeal kombinasyon |

**Formül Doğrulama (Test 9):**
- Discounted = 1,000,000 × (1 - 10/100) = 900,000
- Final = 900,000 × (1 + 10/100) = 990,000 ✓

**Formül Doğrulama (Test 15):**
- Discounted = 50 × (1 - 25/100) = 37.5
- Final = 37.5 × (1 + 15/100) = 43.125 ✓

---

### 2️⃣ **testDiscountBoundaryValues()** - İndirim Sınır Değerleri
**5 test senaryosu - discountRate sınırlarında:**

| discountRate | Beklenen Sonuç | Test Amacı |
|--------------|---|-----------|
| 0.0% | 100.0 | Alt sınır (İndirim yok) |
| 0.1% | 99.9 | Minimum indirim değeri |
| 50.0% | 50.0 | Orta değer |
| 99.9% | 0.1 | Maksimuma yakın |
| 100.0% | 0.0 | Üst sınır (Tam indirim) |

---

### 3️⃣ **testTaxBoundaryValues()** - Vergi Sınır Değerleri
**5 test senaryosu - taxRate sınırlarında:**

| taxRate | Beklenen Sonuç | Test Amacı |
|---------|---|-----------|
| 0.0% | 100.0 | Alt sınır (Vergi yok) |
| 0.1% | 100.1 | Minimum vergi değeri |
| 50.0% | 150.0 | Orta değer |
| 99.9% | 199.9 | Maksimuma yakın |
| 100.0% | 200.0 | Üst sınır (Tam vergi) |

---

### 4️⃣ **testBasePriceBoundaryValues()** - Fiyat Sınır Değerleri
**4 test senaryosu - basePrice sınırlarında:**

| basePrice | Beklenen Sonuç | Test Amacı |
|-----------|---|-----------|
| 0.0 | 0.0 | Alt sınır |
| 0.01 | 0.01 | Çok küçük (kesinlik testi) |
| 100.0 | 100.0 | Normal değer |
| 1,000,000.0 | 1,000,000.0 | Çok büyük sayı |

---

### 5️⃣ **testCombinedBoundaryValues()** - Kombinasyonal Sınır Değer Analizi
**5 test senaryosu - İndirim ve Vergi kombinasyonları:**

| discountRate | taxRate | Beklenen Sonuç | Test Amacı |
|--------------|---------|---|-----------|
| 0.0% | 0.0% | 100.0 | Her iki parametre de sıfır |
| 100.0% | 100.0% | 0.0 | Her iki parametre de maksimum |
| 100.0% | 0.0% | 0.0 | İndirim max, vergi min |
| 0.0% | 100.0% | 200.0 | İndirim min, vergi max |
| 50.0% | 50.0% | 75.0 | Her ikisi de orta değer |

**Formül Doğrulama (Son satır):**
- Discounted = 100 × (1 - 50/100) = 50
- Final = 50 × (1 + 50/100) = 75.0 ✓

---

### 6️⃣ **testResultAlwaysNonNegative()** - Post-Condition Kontrolü
**3 test senaryosu - Sonuç her zaman >= 0 olmalı:**

Test alanı: Fonksiyonun post-condition'ı (result >= 0) sağladığını doğrulama
- Sıfır fiyat, sıfır parametreler → 0.0
- Maksimum indirim → 0.0
- Çok küçük fiyat → >= 0.0

---

## 🔍 Test Kapsamı Matrisi

| Parametre | Sınır Değerleri | Eşdeğerlik Bölgeleri | Kapsama |
|-----------|---|---|---|
| **basePrice** | 0, 0.01, 100, 1M | Zero, Small, Normal, Large | ✅ 100% |
| **discountRate** | 0, 0.1, 50, 99.9, 100 | [0], (0,50), [50], (50,100), [100] | ✅ 100% |
| **taxRate** | 0, 0.1, 50, 99.9, 100 | [0], (0,50), [50], (50,100), [100] | ✅ 100% |

---

## 🛠️ Kullanılan Teknolojiler

- **JUnit 5**: Parametreli testler (@ParameterizedTest)
- **@CsvSource**: Test verilerini CSV formatında sağlama
- **AssertJ**: Fluent assertion API
  - `isCloseTo(expected, within(0.0001))`: Floating-point karşılaştırma
  - `isGreaterThanOrEqualTo()`: Post-condition kontrolü

---

## 📌 Önemli Noktalar

### ✅ Domain Testing Prensipleri Uygulanmıştır:

1. **Eşdeğerlik Bölgeleri**: 
   - basePrice: [0], (0, ∞)
   - discountRate: [0], (0, 100), [100]
   - taxRate: [0], (0, 100), [100]

2. **Sınır Değer Analizi**:
   - Her parametre için min, min+1, max-1, max değerleri test edilmiş

3. **Kombinasyonal Test**:
   - Multiple parameters'ın sınır değerlerinde birlikte test edilmesi

### ✅ AssertJ Kullanım Özellikleri:

```java
// Fluent assertion with custom description
assertThat(result)
    .as("Test: %s", description)
    .isCloseTo(expectedResult, within(0.0001));

// Post-condition assertion
assertThat(result)
    .as("Post-condition kontrol: %s - Sonuç >= 0 olmalı", description)
    .isGreaterThanOrEqualTo(0.0);
```

### ✅ Açıklayıcı Test Adları:

```java
@DisplayName("PriceCalculator - Domain Testing (Eşdeğerlik Bölgesi & Sınır Değer Analizi)")
@ParameterizedTest(name = "{5}") // Test açıklaması ekranında gösterilir
```

---

## 🚀 Testleri Çalıştırma

```bash
# Tüm Domain testlerini çalıştır
mvn test -Dtest=PriceCalculatorDomainTest

# Belirli bir test metodunu çalıştır
mvn test -Dtest=PriceCalculatorDomainTest#testCalculateWithDomainPartitions

# Code Coverage raporu oluştur
mvn test jacoco:report
```

---

## 📊 Beklenen Test Sonuçları

```
PriceCalculatorDomainTest
├── testCalculateWithDomainPartitions()
│   ├── [1] Tüm parametreler sıfır ✓
│   ├── [2] Basit fiyat (indirim/vergi yok) ✓
│   ├── [3] %50 indirim, vergi yok ✓
│   ├── [4] İndirim yok, %20 vergi ✓
│   ├── [5] %50 indirim ve %20 vergi ✓
│   ├── [6] Maksimum indirim (%100) ✓
│   ├── [7] %100 indirim sıfır sonuç verir ✓
│   ├── [8] Maksimum vergi (%100) ✓
│   ├── [9] Büyük fiyat (1M, %10, %10) ✓
│   ├── [10] Çok küçük fiyat (0.01, %50, %10) ✓
│   ├── [11] Minimum indirim %1 ✓
│   ├── [12] Maksimuma yakın indirim %99 ✓
│   ├── [13] Minimum vergi %1 ✓
│   ├── [14] Maksimuma yakın vergi %99 ✓
│   └── [15] İdeal kombinasyon ✓
├── testDiscountBoundaryValues() [5 senaryo] ✓
├── testTaxBoundaryValues() [5 senaryo] ✓
├── testBasePriceBoundaryValues() [4 senaryo] ✓
├── testCombinedBoundaryValues() [5 senaryo] ✓
└── testResultAlwaysNonNegative() [3 senaryo] ✓

TOTAL: 37 test senaryosu - Tümü geçmelidir ✓
```

---

## 🎓 Öğrenilen Teknikler

✅ **Domain Testing**: Eşdeğerlik Bölgeleri + Sınır Değer Analizi  
✅ **Parametreli Testler**: @ParameterizedTest ve @CsvSource  
✅ **Assertion Frameworks**: AssertJ kullanımı  
✅ **Floating-Point Karşılaştırması**: `within()` tolerance kullanımı  
✅ **Test Açıklamaları**: @DisplayName ve test açıklamaları  
