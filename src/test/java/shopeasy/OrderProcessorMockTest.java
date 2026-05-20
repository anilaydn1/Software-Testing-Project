package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Task 5 – Mocks &amp; Stubs (Chapter 6)
 *
 * <p>Target class: {@link OrderProcessor}
 *
 * <p>Use Mockito to mock {@link InventoryService} and {@link PaymentGateway},
 * then test {@link OrderProcessor#process(String, ShoppingCart)} in isolation.
 *
 * <h3>Required scenarios (at least 4)</h3>
 * <ol>
 *   <li><b>Happy path</b> — inventory available, payment succeeds → non-null {@link Order} returned.</li>
 *   <li><b>Inventory failure</b> — {@code isAvailable()} returns {@code false} for at least one item
 *       → method returns {@code null} AND {@code charge()} is <em>never</em> called.</li>
 *   <li><b>Payment failure</b> — inventory OK, {@code charge()} returns {@code false}
 *       → method returns {@code null}.</li>
 *   <li><b>Partial quantity</b> — define the expected behaviour when only some items
 *       pass the inventory check, and write a test for it.</li>
 * </ol>
 *
 * <h3>Verification</h3>
 * Use {@code verify(paymentGateway, never()).charge(...)} to assert that
 * payment is never attempted when inventory is insufficient.
 *
 * <h3>Reflection (add to your report)</h3>
 * Answer: What does mocking allow you to test that you could not test otherwise?
 * What does it prevent you from testing? When is mocking a bad idea?
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    private static final String CUSTOMER = "customer-1";

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 40.0, 50);
    }

    // --- 1) Happy path -------------------------------------------------------

    @Test
    @DisplayName("Envanter ve ödeme uygunsa geçerli bir Order döner")
    void process_happyPath_returnsOrder() {
        cart.addItem(widget, 2);

        // Stub: ürün stokta var, ödeme de başarılı
        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge(CUSTOMER, 50.0)).thenReturn(true);

        Order order = orderProcessor.process(CUSTOMER, cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(CUSTOMER);
        assertThat(order.getTotal()).isEqualTo(50.0);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getOrderId()).isNotBlank();

        // Her iki bağımlılık da beklenen şekilde çağrılmalı
        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway).charge(CUSTOMER, 50.0);
    }

    @Test
    @DisplayName("Birden çok ürün varsa tüm kalemler için envanter sorgulanır")
    void process_multipleItems_checksEachLine() {
        cart.addItem(widget, 1);   // 25.0
        cart.addItem(gadget, 2);   // 80.0  → toplam 105.0

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 2)).thenReturn(true);
        when(paymentGateway.charge(CUSTOMER, 105.0)).thenReturn(true);

        Order order = orderProcessor.process(CUSTOMER, cart);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(105.0);
        verify(inventoryService).isAvailable(widget, 1);
        verify(inventoryService).isAvailable(gadget, 2);
        verify(paymentGateway).charge(CUSTOMER, 105.0);
    }

    // --- 2) Inventory failure ------------------------------------------------

    @Test
    @DisplayName("Stok yetersizse null döner ve ödeme hiç çağrılmaz")
    void process_inventoryUnavailable_skipsPayment() {
        cart.addItem(widget, 3);

        // Stok yok — işlem burada durmalı
        when(inventoryService.isAvailable(widget, 3)).thenReturn(false);

        Order order = orderProcessor.process(CUSTOMER, cart);

        assertThat(order).isNull();

        // Asıl sözleşme: stok yoksa ödeme sistemine asla dokunulmaz
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // --- 3) Payment failure --------------------------------------------------

    @Test
    @DisplayName("Stok uygun ama ödeme reddedilirse sipariş oluşmaz")
    void process_paymentDeclined_returnsNull() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge(CUSTOMER, 50.0)).thenReturn(false);

        Order order = orderProcessor.process(CUSTOMER, cart);

        assertThat(order).isNull();

        // Ödeme tam olarak bir kez denenmiş olmalı
        verify(paymentGateway, times(1)).charge(CUSTOMER, 50.0);
    }

    // --- 4) Partial quantity -------------------------------------------------
    //
    // Senaryo: sepette iki kalem var. İlk ürün stokta yeterli, ikinci üründen
    // ise istenen miktarın tamamı bulunmuyor. Beklenen davranış: ilk kontrol
    // geçse bile ikinci kalem `false` döndüğü an süreç durur — ne ödeme alınır
    // ne de yarım sipariş oluşturulur (all-or-nothing).

    @Test
    @DisplayName("Bir kalem kısmen mevcutsa sipariş bütünüyle reddedilir")
    void process_partialQuantityAvailable_rejectsWholeOrder() {
        cart.addItem(widget, 1);
        cart.addItem(gadget, 5);   // 5 isteniyor ama deponun elinde sadece bir kaçı var

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 5)).thenReturn(false);

        Order order = orderProcessor.process(CUSTOMER, cart);

        assertThat(order).isNull();

        // İlk kalem kontrol edilmiş olmalı, eksik olan kalem de sorgulanmış olmalı
        verify(inventoryService).isAvailable(widget, 1);
        verify(inventoryService).isAvailable(gadget, 5);
        // Yetersiz stok varsa ödeme adımına geçilmez
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // --- Ek doğrulamalar: girdi sözleşmeleri ---------------------------------

    @Test
    @DisplayName("Boş sepet işlenmeye çalışılırsa IllegalArgumentException fırlatılır")
    void process_emptyCart_throws() {
        assertThatThrownBy(() -> orderProcessor.process(CUSTOMER, cart))
            .isInstanceOf(IllegalArgumentException.class);

        // Hiçbir bağımlılık çağrılmamış olmalı
        verifyNoInteractions(inventoryService, paymentGateway);
    }
}
