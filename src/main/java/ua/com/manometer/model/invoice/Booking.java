package ua.com.manometer.model.invoice;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Date;

@Entity
public class Booking {
    public static final int STATE_CHERN = 0;
    public static final int STATE_PROIZV = 1;
    public static final int STATE_SKLAD = 2;
    public static final int STATE_CH_OTGR = 3;
    public static final int STATE_OTGR = 4;
    public static final int STATE_ISP = 5;
    public static final int STATE_PRIOST = 6;
    public static final int STATE_ANN = 7;
    public static final String[] curStateAlias = {"черн", "про", "склад", "ч.отгр", "отгр", "исп", "приос", "анн"};
    @Id
    @GeneratedValue()

    private Integer id;
    @OneToOne(mappedBy = "booking")
    private Invoice invoice;

    private Integer number;
    private String numberModifier;
    private Date date;
    private Integer currentState;
    private Date supplierObligations1;
    private Date supplierObligations2;

    private String notes;  //примечания
    private String comments;  //комментарий

    private Date dateOfNoticeOpening;
    private Date dateOfDeviveryMade;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getNumberModifier() {
        return numberModifier;
    }

    public void setNumberModifier(String numberModifier) {
        this.numberModifier = numberModifier;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
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

    public Date getSupplierObligations1() {
        return supplierObligations1;
    }

    public void setSupplierObligations1(Date supplierObligations1) {
        this.supplierObligations1 = supplierObligations1;
    }

    public Date getSupplierObligations2() {
        return supplierObligations2;
    }

    public void setSupplierObligations2(Date supplierObligations2) {
        this.supplierObligations2 = supplierObligations2;
    }

    public Date getDateOfNoticeOpening() {
        return dateOfNoticeOpening;
    }

    public void setDateOfNoticeOpening(Date dateOfNoticeOpening) {
        this.dateOfNoticeOpening = dateOfNoticeOpening;
    }

    public Date getDateOfDeviveryMade() {
        return dateOfDeviveryMade;
    }

    public void setDateOfDeviveryMade(Date dateOfDeviveryMade) {
        this.dateOfDeviveryMade = dateOfDeviveryMade;
    }

    public String getCurStateStr(){
        return curStateAlias[currentState];
    }

    public String getPurposeStr(){
        return Invoice.purposeAlias[invoice.getPurpose()];
    }

    public String getPaymentPercentStr(){
        return invoice.getPaymentPercent().toString();
    }


}
