package ua.com.manometer.model;

import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.Locale;


@Entity

public class Customer {
    public static String[] localityTypeAlias = {"г.", "пгт", "пос.", "с."};
    public static String[] localityTypeAliasUkr = {"м.", "смт", "с.", "с."};
    public static String[] localityTypeAliasEn = {"м.", "смт", "с.", "с."};

    public static String[] branchValues = {"", "про", "аэс", "энр", "мет", "доб", "нгу", "хим", "маш", "стр", "ком"};


    @Override
    public String toString() {
        return shortName;
    }

    private Integer id;
    private OrgForm orgForm;
    private String name;
    private String nameUkr;
    private String shortName;
    private String stateProperty;
    private Integer branch;

    private boolean purposeForItself;
    private boolean purposeIntermediary;
    private boolean purposeSupply;
    private boolean purposeInstallation;

    private boolean applicationProcess;
    private boolean applicationProduction;
    private boolean applicationProject;
    private boolean applicationEngineering;

    private Integer nomList;
    private boolean actual;
    private Integer prospect;


    //todo почему не хранятся сущности?
    private Integer country;
    private Integer city;
    private Integer area;
    private Integer region;
    private Integer localityType;


    private String address1;
    private String address2;
    private String address3;

    private Customer headCustomer;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date mergeData;
    private String questionnaire;
    private boolean status = true;
    private Customer oldRecord;
    private User person;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date dateOfRecord;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date dateLastCorrection;
    private String codeOKPO;
    private String requisite;
    private String registrationNumber;
    private String site;
    private String moreInformation;

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Integer getBranch() {
        return branch;
    }


    @ManyToOne(fetch = FetchType.EAGER)
    public OrgForm getOrgForm() {
        return orgForm;
    }

    public String getStateProperty() {

        return stateProperty;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isPurposeForItself() {
        return purposeForItself;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isPurposeIntermediary() {
        return purposeIntermediary;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isPurposeSupply() {
        return purposeSupply;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isPurposeInstallation() {
        return purposeInstallation;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isApplicationProcess() {
        return applicationProcess;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isApplicationProduction() {
        return applicationProduction;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isApplicationProject() {
        return applicationProject;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isApplicationEngineering() {
        return applicationEngineering;
    }

    public Integer getNomList() {
        return nomList;
    }

    @Column(nullable = false, length = 1, name = "new")
    @Type(type = "yes_no")
    public boolean isActual() {
        return actual;
    }


    public Integer getProspect() {
        return prospect;
    }

    public Integer getCountry() {
        return country;
    }

    public Integer getCity() {
        return city;
    }

    public Integer getArea() {
        return area;
    }

    public Integer getRegion() {
        return region;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    @OneToOne(cascade = CascadeType.PERSIST)
    public Customer getHeadCustomer() {
        return headCustomer;
    }

    public Date getMergeData() {
        return mergeData;
    }

    public String getQuestionnaire() {
        return questionnaire;
    }

    @Column(nullable = false, length = 1)
    @Type(type = "yes_no")
    public boolean isStatus() {
        return status;
    }

    @OneToOne(cascade = CascadeType.PERSIST)
    public Customer getOldRecord() {
        return oldRecord;
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})

    public User getPerson() {
        return person;
    }

    public Date getDateOfRecord() {
        return dateOfRecord;
    }

    public Date getDateLastCorrection() {
        return dateLastCorrection;
    }

    public String getCodeOKPO() {
        return codeOKPO;
    }

    public String getRequisite() {
        return requisite;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getSite() {
        return site;
    }

    public String getMoreInformation() {
        return moreInformation;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setBranch(Integer branch) {
        this.branch = branch;
    }

    public void setOrgForm(OrgForm orgForm) {
        this.orgForm = orgForm;
    }

    public void setStateProperty(String stateProperty) {
        this.stateProperty = stateProperty;
    }

    public void setPurposeForItself(boolean purposeForItself) {
        this.purposeForItself = purposeForItself;
    }

    public void setPurposeIntermediary(boolean purposeIntermediary) {
        this.purposeIntermediary = purposeIntermediary;
    }

    public void setPurposeSupply(boolean purposeSupply) {
        this.purposeSupply = purposeSupply;
    }

    public void setPurposeInstallation(boolean purposeInstallation) {
        this.purposeInstallation = purposeInstallation;
    }

    public void setApplicationProcess(boolean applicationProcess) {
        this.applicationProcess = applicationProcess;
    }

    public void setApplicationProduction(boolean applicationProduction) {
        this.applicationProduction = applicationProduction;
    }

    public void setApplicationProject(boolean applicationProject) {
        this.applicationProject = applicationProject;
    }

    public void setApplicationEngineering(boolean applicationEngineering) {
        this.applicationEngineering = applicationEngineering;
    }

    public void setNomList(Integer nomList) {
        this.nomList = nomList;
    }

    public void setActual(boolean new1) {
        actual = new1;
    }

    public void setProspect(Integer prospect) {
        this.prospect = prospect;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public void setCity(Integer city) {
        this.city = city;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public void setRegion(Integer region) {
        this.region = region;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public void setHeadCustomer(Customer headCustomer) {
        this.headCustomer = headCustomer;
    }

    public void setMergeData(Date mergeData) {
        this.mergeData = mergeData;
    }

    public void setQuestionnaire(String questionnaire) {
        this.questionnaire = questionnaire;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setOldRecord(Customer oldRecord) {
        this.oldRecord = oldRecord;
    }

    public void setPerson(User person) {
        this.person = person;
    }

    public void setDateOfRecord(Date dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    public void setDateLastCorrection(Date dateLastCorrection) {
        this.dateLastCorrection = dateLastCorrection;
    }

    public void setCodeOKPO(String codeOKPO) {
        this.codeOKPO = codeOKPO;
    }

    public void setRequisite(String requisite) {
        this.requisite = requisite;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setMoreInformation(String moreInformation) {
        this.moreInformation = moreInformation;
    }

    public Integer getLocalityType() {
        return localityType;
    }

    public void setLocalityType(Integer localityType) {
        this.localityType = localityType;
    }

    public String getNameUkr() {
        return nameUkr;
    }

    public void setNameUkr(String nameUkr) {
        this.nameUkr = nameUkr;
    }

    @Transient
    public String getBranchString() {

        try {

            return branchValues[branch];
        } catch (Throwable t) {
            t.printStackTrace();

        }

        return "";
    }


    public String getLocalizedName(Locale locale) {

        if ((new Locale("ru", "RU")).equals(locale)) {
            return name;
        } else if ((new Locale("ua", "UA")).equals(locale)) {
            return nameUkr;
        } else if ((new Locale("en", "EN")).equals(locale)) {
            return name;
        } else {
            return "error. Locale not found!!!";
        }

    }
}
