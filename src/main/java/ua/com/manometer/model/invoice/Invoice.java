package ua.com.manometer.model.invoice;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import ua.com.manometer.model.Customer;
import ua.com.manometer.model.Supplier;
import ua.com.manometer.model.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Entity

public class Invoice {
    public static final String[] purposeAlias = {"пос", "исп", "гар", "рез", "прч"};
    public static final String[] purposeAliasFull = {"поставка", "испытания", "гарантия", "резерв", "прочее"};
    public static final String[] curStateAlias = {"черн", "акт", "мод", "отл", "изм", "анн", "зак", "исп", "оплач", "отгр", "ч.исп", "отказ"};


    public static final Integer STATE_CHERN = 0;
    public static final Integer STATE_ACT = 1;
    public static final Integer STATE_MOD = 2;
    public static final Integer STATE_OTL = 3;
    public static final Integer STATE_IZM = 4;
    public static final Integer STATE_ANN = 5;
    public static final Integer STATE_ZAK = 6;
    public static final Integer STATE_ISP = 7;

    public static final Integer STATE_OPLACH = 8;
    public static final Integer STATE_OTGR = 9;
    public static final Integer STATE_CH_ISP = 10;
    public static final Integer STATE_OTKAZ = 11;


    public static final Integer PURPOSE_POSTAVKA = 0;
    public static final Integer PURPOSE_ISPIT = 1;


    @Id
    @GeneratedValue()
    private Integer id;


    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    private boolean isInvoice;
    private Integer number;
    private String numberModifier;
    private Date date;
    private Date changeDate;
    private Date controlDate;

    private Integer validity; //    срок действия
    @ManyToOne
    private Supplier supplier;


    @ManyToOne
    private Customer employer;
    @ManyToOne
    private Customer consumer;

    private String notice;
    @Column(length = 5000)
    private String notes;  //примечания
    @Column(length = 5000)
    private String comments;  //комментарий
    private String deliveryTime;  //срок поставки


    @ManyToOne
    private User responsible;
    @ManyToOne
    private User executor;


    private Integer purpose; //назначение
    private BigDecimal prepayment; //предоплата
    private BigDecimal paymentOnTheNotice; //по извещению
    private BigDecimal postPay; //по факту


    private Integer daysAfterDelivery;
    private Integer currentState;
    private Date awaitingPayment;  //ож. оплаты
    private Date awaitingDelivery; //ож. поставки

    private Integer probabilityOfPayment;//вероятность оплаты
    private BigDecimal NDS;
    private BigDecimal exchangeRate;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INVOICE_ID")
    @IndexColumn(name = "orders_index", base = 0)
    private List<InvoiceItem> invoiceItems = new LinkedList<InvoiceItem>();


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INVOICE_ID")
    private Set<Shipment> shipments = new HashSet<Shipment>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INVOICE_ID")
    private Set<Payment> payments = new HashSet<Payment>();


    private Integer t0;
    private Integer t1;
    private Integer t2;
    private Integer t3;
    private Integer t4;
    private Integer t5;

    @Transient
    Integer prev;

    @Transient
    Integer next;

    @Transient
    private BigDecimal debt;

    @Transient
    private BigDecimal debtPercent;

    @Column(name = "addition_to_price", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal additionToPrice = BigDecimal.ZERO;

    @Column(name = "nds_percent", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal ndsPayment = BigDecimal.ZERO;


    @Column(name = "payment_percent", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal paymentPercent = BigDecimal.ZERO;

    @Column(name = "sum", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal sum = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "total_payments", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal totalPayments = BigDecimal.ZERO;


    @Column(name = "shipment_percent", nullable = false, columnDefinition = "DECIMAL(19,2) NOT NULL DEFAULT 00,00")
    private BigDecimal shipmentPercent = BigDecimal.ZERO;

    @Column(name = "is_payment_made", nullable = false, columnDefinition = "CHAR(1) NOT NULL DEFAULT N")
    @Type(type = "yes_no")
    private boolean paymentMade;

    @Column(name = "is_delivery_made", nullable = false, columnDefinition = "CHAR(1) NOT NULL DEFAULT N")
    @Type(type = "yes_no")
    private boolean deliveryMade;

    @Column(name = "is_any_goods_shipped", nullable = false, columnDefinition = "CHAR(1) NOT NULL DEFAULT N")
    @Type(type = "yes_no")
    private boolean anyGoodsShipped;


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }

    public Set<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }

    public void addShipment(Shipment shipping) {
        if (shipments == null) shipments = new HashSet<Shipment>();
        shipments.add(shipping);
    }

    public void addPayment(Payment payment) {
        if (payments == null) payments = new HashSet<Payment>();
        payments.add(payment);
    }


    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isInvoice() {
        return isInvoice;
    }

    public void setInvoice(boolean invoice) {
        isInvoice = invoice;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }


    public Customer getEmployer() {
        return employer;
    }

    public void setEmployer(Customer employer) {
        this.employer = employer;
    }

    public Customer getConsumer() {
        return consumer;
    }

    public void setConsumer(Customer consumer) {
        this.consumer = consumer;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }

    public User getExecutor() {
        return executor;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public List<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public void addInvoiceItems(InvoiceItem invoiceItem) {
        if (invoiceItems == null)
            invoiceItems = new LinkedList<InvoiceItem>();

        invoiceItems.add(invoiceItem);
    }


    public Integer getPurpose() {
        return purpose;
    }

    public void setPurpose(Integer purpose) {
        this.purpose = purpose;
    }


    public BigDecimal getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(BigDecimal prepayment) {
        this.prepayment = prepayment;
    }

    public BigDecimal getPaymentOnTheNotice() {
        return paymentOnTheNotice;
    }

    public void setPaymentOnTheNotice(BigDecimal paymentOnTheNotice) {
        this.paymentOnTheNotice = paymentOnTheNotice;
    }

    public BigDecimal getPostPay() {
        return postPay;
    }

    public void setPostPay(BigDecimal postPay) {
        this.postPay = postPay;
    }

    public Integer getDaysAfterDelivery() {
        return daysAfterDelivery;
    }

    public void setDaysAfterDelivery(Integer daysAfterDelivery) {
        this.daysAfterDelivery = daysAfterDelivery;
    }

    public Date getAwaitingPayment() {
        return awaitingPayment;
    }

    public void setAwaitingPayment(Date awaitingPayment) {
        this.awaitingPayment = awaitingPayment;
    }

    public Date getAwaitingDelivery() {
        return awaitingDelivery;
    }

    public void setAwaitingDelivery(Date awaitingDelivery) {
        this.awaitingDelivery = awaitingDelivery;
    }

    public Integer getProbabilityOfPayment() {
        return probabilityOfPayment;
    }

    public void setProbabilityOfPayment(Integer probabilityOfPayment) {
        this.probabilityOfPayment = probabilityOfPayment;
    }

    public BigDecimal getNDS() {
        return NDS;
    }

    public void setNDS(BigDecimal NDS) {
        this.NDS = NDS;
    }


    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }


    public String getNumberModifier() {
        return numberModifier;
    }

    public void setNumberModifier(String numberModifier) {
        this.numberModifier = numberModifier;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public Date getControlDate() {
        return controlDate;
    }

    public void setControlDate(Date controlDate) {
        this.controlDate = controlDate;
    }


    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Integer getT0() {
        return t0;
    }

    public void setT0(Integer t0) {
        this.t0 = t0;
    }

    public Integer getT1() {
        return t1;
    }

    public void setT1(Integer t1) {
        this.t1 = t1;
    }

    public Integer getT2() {
        return t2;
    }

    public void setT2(Integer t2) {
        this.t2 = t2;
    }

    public Integer getT3() {
        return t3;
    }

    public void setT3(Integer t3) {
        this.t3 = t3;
    }

    public Integer getT4() {
        return t4;
    }

    public void setT4(Integer t4) {
        this.t4 = t4;
    }

    public Integer getT5() {
        return t5;
    }

    public void setT5(Integer t5) {
        this.t5 = t5;
    }

    public BigDecimal getDebt() {
        return debt;
    }

    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }


    public BigDecimal getDebtPercent() {
        return debtPercent;
    }

    public void setDebtPercent(BigDecimal debtPercent) {
        this.debtPercent = debtPercent;
    }


    // **** computed fields


    public BigDecimal getAdditionToPrice() {
        return additionToPrice;
    }

    public void setAdditionToPrice(BigDecimal additionToPrice) {
        this.additionToPrice = additionToPrice;
    }

    public BigDecimal getNdsPayment() {
        return ndsPayment;
    }

    public void setNdsPayment(BigDecimal ndsPayment) {
        this.ndsPayment = ndsPayment;
    }

    public BigDecimal getPaymentPercent() {
        return paymentPercent;
    }

    public void setPaymentPercent(BigDecimal paymentPercent) {
        this.paymentPercent = paymentPercent;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(BigDecimal totalPayments) {
        this.totalPayments = totalPayments;
    }

    public boolean isPaymentMade() {
        return paymentMade;
    }

    public void setPaymentMade(boolean paymentMade) {
        this.paymentMade = paymentMade;
    }

    public boolean isDeliveryMade() {
        return deliveryMade;
    }

    public void setDeliveryMade(boolean deliveryMade) {
        this.deliveryMade = deliveryMade;
    }

    public boolean isAnyGoodsShipped() {
        return anyGoodsShipped;
    }

    public void setAnyGoodsShipped(boolean anyGoodsShipped) {
        this.anyGoodsShipped = anyGoodsShipped;
    }

    public Integer getPrev() {
        return prev;
    }

    public void setPrev(Integer prev) {
        this.prev = prev;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public BigDecimal getShipmentPercent() {
        return shipmentPercent;
    }

    public void setShipmentPercent(BigDecimal shipmentPercent) {
        this.shipmentPercent = shipmentPercent;
    }


    @Override
    public String toString() {
        return "i_" + id;    //To change body of overridden methods use File | Settings | File Templates.
    }

}
