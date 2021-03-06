package ua.com.manometer.model;

import com.fasterxml.jackson.annotation.JsonFormat;

//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.format.annotation.DateTimeFormat;
import ua.com.manometer.model.invoice.filter.BookingFilter;
import ua.com.manometer.model.invoice.filter.InvoiceFilter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user")
@JsonIgnoreProperties({"invoiceFilter", "bookingFilter"})
public class User {
    public static String[] POWER_LEVELS = {"пользователь", "менеджер", "экономист", "администратор"};
    public static Integer LEVEL_USER = 1;
    public static Integer LEVEL_MANAGER = 2;
    public static Integer LEVEL_ECONOMIST = 3;
    public static Integer LEVEL_ADMINISTRATOR = 4;

    @Id
    @GeneratedValue
    private Integer id;
    private String name; // имя
    private String patronymic; // отчество
    private String lastName; // фамилия
    private String position; // должность
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "EET")
    private Date receptionOnWorkDate; // дата приёма на работу
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "EET")
    private Date dischargingDate; // дата увольнения
    private String tel;// телефон
    private String telMob; // телефон мобильный
    private Integer powersLevel; // уровень полномочий
    private String login; // логин
    private String pass;// пароль


    @OneToOne(optional = false,fetch = FetchType.LAZY)
//    @Fetch(FetchMode.JOIN)
    @PrimaryKeyJoinColumn
    private InvoiceFilter invoiceFilter;

    @OneToOne(optional = false,fetch = FetchType.LAZY)
//    @Fetch(FetchMode.JOIN)
    @PrimaryKeyJoinColumn
    private BookingFilter bookingFilter;


    private String fioUkr;

    public InvoiceFilter getInvoiceFilter() {
        return invoiceFilter;
    }

    public void setInvoiceFilter(InvoiceFilter invoiceFilter) {
        this.invoiceFilter = invoiceFilter;
    }

    public Integer getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getPatronymic() {
        return patronymic;
    }


    public String getLastName() {
        return lastName;
    }


    public String getPosition() {
        return position;
    }


    public Date getReceptionOnWorkDate() {
        return receptionOnWorkDate;
    }


    public Date getDischargingDate() {
        return dischargingDate;
    }


    public String getTel() {
        return tel;
    }


    public String getTelMob() {
        return telMob;
    }


    public Integer getPowersLevel() {
        return powersLevel;
    }

    public String getLogin() {
        return login;
    }


    public String getPass() {
        return pass;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setPosition(String position) {
        this.position = position;
    }


    public void setReceptionOnWorkDate(Date receptionOnWorkDate) {
        this.receptionOnWorkDate = receptionOnWorkDate;
    }


    public void setDischargingDate(Date dischargingDate) {
        this.dischargingDate = dischargingDate;
    }


    public void setTel(String tel) {
        this.tel = tel;
    }


    public void setTelMob(String telMob) {
        this.telMob = telMob;
    }


    public void setPowersLevel(Integer powersLevel) {
        this.powersLevel = powersLevel;
    }


    public void setLogin(String login) {
        this.login = login;
    }


    public void setPass(String pass) {
        this.pass = pass;
    }


    public String getFioUkr() {
        return fioUkr;
    }

    public void setFioUkr(String fioUkr) {
        this.fioUkr = fioUkr;
    }

    public BookingFilter getBookingFilter() {
        return bookingFilter;
    }

    public void setBookingFilter(BookingFilter bookingFilter) {
        this.bookingFilter = bookingFilter;
    }

    @Transient
    public String getPowLevStr() {

        if (powersLevel == 1) {
            return "Пользователь";
        }
        if (powersLevel == 2) {
            return "Менеджер";
        }
        if (powersLevel == 3) {
            return "Экономист";
        }
        return "Администратор";
    }

    @Transient
    public boolean isUser() {
        return 1 == powersLevel;
    }

    @Transient
    public boolean isManager() {
        return 2 == powersLevel;
    }

    @Transient
    public boolean isEconomist() {
        return 3 == powersLevel;
    }

    @Transient
    public boolean isAdmin() {
        return 4 == powersLevel;
    }

    public String toString() {
        return login;
    }
}

