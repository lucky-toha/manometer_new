package ua.com.manometer.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import ua.com.manometer.model.Customer;
import ua.com.manometer.model.SecuredUser;
import ua.com.manometer.model.Supplier;
import ua.com.manometer.model.User;
import ua.com.manometer.model.address.City;
import ua.com.manometer.model.invoice.*;
import ua.com.manometer.model.invoice.filter.BookingFilter;
import ua.com.manometer.model.invoice.filter.InvoiceFilter;
import ua.com.manometer.service.*;
import ua.com.manometer.service.address.CityService;
import ua.com.manometer.service.invoice.BookingService;
import ua.com.manometer.service.invoice.InvoiceItemService;
import ua.com.manometer.service.invoice.InvoiceService;
import ua.com.manometer.service.invoice.PaymentService;
import ua.com.manometer.util.InvoiceUtils;
import ua.com.manometer.util.amount.JAmount;
import ua.com.manometer.util.amount.JAmountEN;
import ua.com.manometer.util.amount.JAmountRU;
import ua.com.manometer.util.amount.JAmountUA;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {
    private static Logger LOGGER = Logger.getLogger(InvoiceController.class);

    @Autowired
    InvoiceService invoiceService;
    @Autowired
    CustomerService customerService;
    @Autowired
    SupplierService supplierService;
    @Autowired
    InvoiceItemService invoiceItemService;
    @Autowired
    CityService cityService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private CurrencyService currencyService;


    @RequestMapping("/")
    public String populateInvoices(@CookieValue(required = false) String invoice_filter, Map<String, Object> map) {
//        InvoiceFilter filter = null;
//        if (StringUtils.isNotBlank(invoice_filter)) {
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.setDateFormat(new SimpleDateFormat("dd.MM.yyyy"));
//                filter = objectMapper.readValue(invoice_filter, InvoiceFilter.class);
//
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                LOGGER.error(e);
//            }
//
//        }
        SecuredUser securedUser = (SecuredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InvoiceFilter filter = securedUser.getInvoiceFilter();

        map.put("userId", securedUser.getUserId());
//        if (filter != null) {
            map.put("listInvoices", invoiceService.listFilteredInvoice(filter));
//        } else {
//            map.put("listInvoices", invoiceService.listInvoice());
//
//        }
        map.put("currencies", currencyService.listCurrency());
        map.put("userList", userService.listUser());
        map.put("userLevel", securedUser.getPowerLevel());

        return "invoices";
    }

    @RequestMapping("/view")
    public String viewInvoice(@RequestParam("invoice_id") Integer id, Map<String, Object> map) {
        ///   LOGGER.info(getName() + " open invoice id = " + id);


        Invoice invoice = invoiceService.getInvoice(id);
        map.put("invoice", invoice);
        map.put("supplierList", supplierService.listSupplier());


//        final List<Invoice> invoices = invoiceService.listInvoice();
////        final int index = invoices.indexOf(invoice);
//        final int index = getIndex(invoices, invoice);
//        if (index == 0) {
//            invoice.setPrev(null);
//            if (invoices.size() > 1) {
//                invoice.setNext(invoices.get(index + 1).getId());
//            } else {
//                invoice.setNext(null);
//            }
//        } else if (index == invoices.size() - 1) {
//            invoice.setNext(null);
//            invoice.setPrev(invoices.get(index - 1).getId());
//        } else {
//            invoice.setPrev(invoices.get(index - 1).getId());
//            invoice.setNext(invoices.get(index + 1).getId());
//        }


        return "editInvoice";
    }


    // todo для того, чтоб получить индекc следующего и предыдущего надо вытащить все заказы
    private int getIndex(List<Invoice> invoices, Invoice invoice) {
        Integer id = invoice.getId();
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }


    @RequestMapping("/view_shipments")
    public String viewShipments(@RequestParam("invoice_id") Integer id, Map<String, Object> map) {
//        LOGGER.info(getName() + " view shipments for invoice id = " + id);

        Invoice invoice = invoiceService.getInvoice(id);

        Map<Integer, Map<Integer, Integer>> res = new HashMap<Integer, Map<Integer, Integer>>();
        Set<Shipment> shipments = invoice.getShipments();
        for (Shipment shipment : shipments) {
            Map<Integer, Integer> mapSM = new HashMap<Integer, Integer>();
            List<ShipmentMediator> shippingMediators = shipment.getShippingMediators();
            for (ShipmentMediator sm : shippingMediators) {
                mapSM.put(sm.getInvoiceItem().getId(), sm.getCount());
            }
            res.put(shipment.getId(), mapSM);
        }


        map.put("map", res);
        map.put("invoice", invoice);
        map.put("supplierList", supplierService.listSupplier());
        return "shipments";
    }


    @RequestMapping("/delete")
    public String deleteInvoice(@RequestParam("invoice_id") Integer id) {
        LOGGER.info(getName() + " delete invoice id = " + id);
        invoiceService.removeInvoice(id);
        return "redirect:/invoices/";
    }


    @RequestMapping("/add")
    public String addInvoice(HttpServletRequest request, Map<String, Object> map) throws ParseException {
        LOGGER.info(getName() + " add invoice ");
        Invoice invoice = new Invoice();

        String employerShortName = request.getParameter("employer");
        LOGGER.info(" \"employer\" = " + employerShortName);
        Customer employer = customerService.getCustomerByShortName(employerShortName);
        invoice.setEmployer(employer);

        String consumerShortName = request.getParameter("consumer");
        LOGGER.info(" \"consumer\" = " + consumerShortName);
        Customer consumer = customerService.getCustomerByShortName(consumerShortName);
        invoice.setConsumer(consumer);

        invoice.setExecutor(employer.getPerson());
        invoice.setNDS(new BigDecimal("20"));
        invoice.setPurpose(Invoice.PURPOSE_POSTAVKA);
        invoice.setDate(getCurrentDate());
        invoice.setInvoice(new Boolean(request.getParameter("isInvoice")));
        LOGGER.info("\"isInvoice\" = " + request.getParameter("isInvoice"));
        invoice.setNumber(new Integer(request.getParameter("number")));
        LOGGER.info("\"number\" = " + request.getParameter("number"));
        invoice.setNumberModifier(request.getParameter("numberModifier"));
        LOGGER.info("\"numberModifier\" = " + request.getParameter("numberModifier"));

        invoice.setValidity(10);
        invoice.setPaymentOnTheNotice(new BigDecimal(0));
        invoice.setPrepayment(new BigDecimal(0));
        invoice.setPostPay(new BigDecimal(0));
        invoice.setCurrentState(Invoice.STATE_CHERN);
        invoice.setDeliveryTime("45 - 60 днів з моменту передоплати.");

        //todo заказчик по умолчанию нужно сдулать
        List<Supplier> suppliers = supplierService.listSupplier();
        if (!suppliers.isEmpty()) {
            invoice.setSupplier(suppliers.get(0));
            invoice.setExchangeRate(suppliers.get(0).getCurrency().getExchangeRate());
        }
        invoice.setShipments(new HashSet<Shipment>());
        invoice.setPayments(new HashSet<Payment>());
        invoice.setInvoiceItems(new LinkedList<InvoiceItem>());
        invoiceService.saveInvoice(invoice);
        LOGGER.info("Invoice was created. Id = " + invoice.getId());
        return "redirect:/invoices/view?invoice_id=" + invoice.getId();
    }


    @RequestMapping("/copy")
    public String copyInvoice(HttpServletRequest request, Map<String, Object> map) throws ParseException {

        Invoice parent = invoiceService.getInvoice(new Integer(request.getParameter("parent_id")));
        final String employerShortName = request.getParameter("employer");
        final String consumerShortName = request.getParameter("consumer");
        DateFormat f = new SimpleDateFormat("dd.MM.yyyy");
        final Date date = f.parse(request.getParameter("date"));
        final Boolean isInvoice = new Boolean(request.getParameter("isInvoice"));
        final Integer number = new Integer(request.getParameter("number"));
        final String numberModifier = request.getParameter("numberModifier");

        Invoice newInvoice = createCopyOfInvoice(parent, employerShortName, consumerShortName, date, isInvoice, number, numberModifier);

        LOGGER.info("copy of Invoice was created. Id = " + newInvoice.getId());
        return "redirect:/invoices/view?invoice_id=" + newInvoice.getId();
    }


    @RequestMapping("/divideInvoice")
    public String divideInvoice(@RequestParam(required = false) Integer parent_id,
                                @RequestParam(required = false) Boolean isInvoice1,
                                @RequestParam(required = false) Boolean isInvoice2,
                                @RequestParam(required = false) Integer number1,
                                @RequestParam(required = false) Integer number2,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Date date1,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Date date2,
                                @RequestParam(required = false) String numberModifier1,
                                @RequestParam(required = false) String numberModifier2,
                                @RequestParam(required = false) String employer1,
                                @RequestParam(required = false) String employer2,
                                @RequestParam(required = false) String consumer1,
                                @RequestParam(required = false) String consumer2) throws ParseException {
        Invoice parent = invoiceService.getInvoice(parent_id);

        Invoice newInvoice1 = createCopyOfInvoice(parent, employer1, consumer1, date1, isInvoice1, number1, numberModifier1);
        Invoice newInvoice2 = createCopyOfInvoice(parent, employer2, consumer2, date2, isInvoice2, number2, numberModifier2);
        parent.setCurrentState(Invoice.STATE_IZM);
        invoiceService.saveInvoice(parent);


        LOGGER.info("Two copies of Invoice id " + parent.getId() + "  was created. Id 1= " + newInvoice1.getId() + ". Id 2= " + newInvoice2.getId());


        return "redirect:/invoices/view?invoice_id=" + newInvoice1.getId();
    }


    @RequestMapping("/add_shipment")
    public
    @ResponseBody
    Map addShipment(@RequestParam("invoice_id") Integer invoiceId, HttpServletRequest request) throws ParseException {
        //todo эта операция должна выполняться в одной транзакции
        LOGGER.info(getName() + " add Shipment invoice id = " + invoiceId);

        Invoice invoice = invoiceService.getInvoice(invoiceId);
        Shipment shipment = new Shipment();
        Boolean otkaz = false;


        shipment.setDate((new SimpleDateFormat("dd.MM.yyyy")).parse(request.getParameter("date")));
        shipment.setShipmentNum(request.getParameter("shipmentNum"));
        for (InvoiceItem item : invoice.getInvoiceItems()) {
            String countStr = request.getParameter("invItId" + item.getId());

            if (StringUtils.isNotBlank(countStr)) {
                Integer count = new Integer(countStr);
                if (count < 0) {
                    otkaz = true;
                }
                ShipmentMediator sm = new ShipmentMediator(item, count);
                shipment.addShippingMediator(sm);
                item.addShippingMediators(sm);
                invoiceItemService.saveInvoiceItem(item);
            }
        }

        Map result = new HashMap();
        result.put("isStateChanged", false);

        if ((shipment.getShippingMediators() != null) && (!shipment.getShippingMediators().isEmpty())) {

            shipment.setInvoice(invoice);
            invoice.addShipment(shipment);
            if (invoice.getCurrentState() != Invoice.STATE_CH_ISP && invoice.getCurrentState() != Invoice.STATE_OPLACH) {
                result.put("isStateChanged", true);
                result.put("state", "Частично исполнен");
                invoice.setCurrentState(Invoice.STATE_CH_ISP);
            }

            InvoiceUtils.setupInvoice(invoice);
            if (invoice.isDeliveryMade()) {
                result.put("isStateChanged", true);

                invoice.getBooking().setCurrentState(Booking.STATE_OTGR);
                invoice.getBooking().setDateOfDeviveryMade(getCurrentDate());
                invoice.setCurrentState(Invoice.STATE_OTGR);
                result.put("state", "Отгружен");

                if (invoice.isPaymentMade()) {
                    invoice.getBooking().setCurrentState(Booking.STATE_ISP);
                    invoice.setCurrentState(Invoice.STATE_ISP);
                    result.put("state", "Исполнен");
                }
            }
            if (otkaz && Invoice.STATE_OTKAZ != invoice.getCurrentState()) {
                invoice.setCurrentState(Invoice.STATE_OTKAZ);
                result.put("isStateChanged", true);
                result.put("state", "Отказ");
            }


            invoiceService.saveInvoice(invoice);
            bookingService.saveBooking(invoice.getBooking());
        }

        return result;
        //   return "redirect:/invoices/view_shipments?invoice_id=" + invoiceId;
    }

    @RequestMapping("/get_shipment_sum")
    public
    @ResponseBody
    Map editInvoiceItem(@RequestParam("invoice_id") Integer invoiceId, HttpServletRequest request)
            throws Exception {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        BigDecimal sum = new BigDecimal("0");
        BigDecimal count;
        for (InvoiceItem item : invoice.getInvoiceItems()) {
            String param = request.getParameter("invItId" + item.getId());
            if (StringUtils.isNotBlank(param)) {
                count = new BigDecimal(param);
                sum = sum.add(item.getSellingPrice().multiply(count));
            }
        }
        NumberFormat df = NumberFormat.getInstance();
        df.setMinimumFractionDigits(2);
        Map map = new HashMap();
        map.put("sum", sum);
        return map;
    }


    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @RequestMapping("/editInvoiceItem")
    public
    @ResponseBody
    String editInvoiceItem(@RequestParam("invId") Integer invoiceId, @RequestParam("id") final Integer id, @RequestParam("param") String param, @RequestParam("value") String value)
            throws Exception {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        InvoiceItem item = (InvoiceItem) CollectionUtils.find(invoice.getInvoiceItems(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((InvoiceItem) o).getId().equals(id);
            }
        });

        if (param.equalsIgnoreCase("quantity")) {
            item.setQuantity(new Integer(value));
        } else if (param.equalsIgnoreCase("additionalCost")) {
            item.setAdditionalCost(new BigDecimal(value.replace(",", ".")).setScale(2, RoundingMode.HALF_UP));
        } else if (param.equalsIgnoreCase("transportationCost")) {
            item.setTransportationCost(new BigDecimal(value.replace(",", ".").replace(" ", "")).setScale(2, RoundingMode.HALF_UP));
        } else if (param.equalsIgnoreCase("percent")) {
            item.setPercent(new BigDecimal(value.replace(",", ".")).setScale(4, RoundingMode.HALF_UP));
        } else if (param.equalsIgnoreCase("sellingPrice")) {
            BigDecimal t = new BigDecimal(value.replace(",", "."));
            item.setSellingPrice(t.setScale(2, RoundingMode.HALF_UP));
        } else if (param.equalsIgnoreCase("deliveryTime")) {
            item.setDeliveryTime(new Integer(value));
        } else if (param.equalsIgnoreCase("data")) {
            int i = InvoiceItem.daysBetween(item.getInvoice().getBooking().getDate(), (new SimpleDateFormat("dd.MM.yyyy")).parse(value));
            item.setDeliveryTime(i);
        }
        invoiceService.saveInvoice(invoice);

        String res = "{";
        res += "\"total\":\"" + invoice.getTotal().toString() + "\"," +
                "\"sumTot\":\"" + invoice.getSum().toString() + "\"," +
                "\"nds\":\"" + invoice.getNdsPayment().toString() + "\"";
        res += ",\"quantity\":\"" + item.getQuantity() + "\"";
        res += ",\"percent\":\"" + item.calculatePercent().toString() + "\"";
        res += ",\"sellingPrice\":\"" + item.getSellingPrice().toString() + "\"";
        res += ",\"sum\":\"" + item.getSum().toString() + "\"";
        res += ",\"transportationCost\":\"" + item.getTransportationCost().toString() + "\"";
        res += ",\"additionalCost\":\"" + item.getAdditionalCost().toString() + "\"";
        res += ",\"deliveryTime\":\"" + item.getDeliveryTime() + "\"";
        res += "}";

        return res;
    }


    @RequestMapping("/editInvoiceParams")
    public
    @ResponseBody
    Map editInvoiceParams(@RequestParam("id") Integer id, @RequestParam("param") String param, @RequestParam("value") String value) throws ParseException {
        Map<String, Object> map = new HashMap<String, Object>();
        Invoice invoice = invoiceService.getInvoice(id);

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);


        if (invoice == null) {
            //todo      log.error
            return map;
        }
        if (param.equals("date")) {
            invoice.setDate((new SimpleDateFormat("dd.MM.yyyy")).parse(value));
        } else if (param.equals("daysAfterDelivery")) {
            invoice.setDaysAfterDelivery(new Integer(value));
        } else if (param.equals("notes")) {
            invoice.setNotes(value);
        } else if (param.equals("comments")) {
            invoice.setComments(value);
        } else if (param.equals("deliveryTime")) {
            invoice.setDeliveryTime(value);
        } else if (param.equals("purpose")) {
            invoice.setPurpose(new Integer(value));
        } else if (param.equals("prepayment")) {
            invoice.setPrepayment(new BigDecimal(value.replace(",", ".")));
        } else if (param.equals("paymentOnTheNotice")) {
            invoice.setPaymentOnTheNotice(new BigDecimal(value.replace(",", ".")));
        } else if (param.equals("postPay")) {
            invoice.setPostPay(new BigDecimal(value.replace(",", ".")));
        } else if (param.equals("probabilityOfPayment")) {
            invoice.setProbabilityOfPayment(new Integer(value));
        } else if (param.equals("awaitingPayment")) {
            invoice.setAwaitingPayment((new SimpleDateFormat("dd.MM.yyyy")).parse(value));
        } else if (param.equals("awaitingDelivery")) {
            invoice.setAwaitingDelivery((new SimpleDateFormat("dd.MM.yyyy")).parse(value));
        } else if (param.equals("controlDate")) {
            invoice.setControlDate((new SimpleDateFormat("dd.MM.yyyy")).parse(value));
        } else if (param.equals("validity")) {
            invoice.setValidity(new Integer(value));
        } else if (param.equals("currentState")) {
            invoice.setCurrentState(new Integer(value));
        } else if (param.equals("employer")) {
            if (customerService.isCustomerPresent(value)) {
                map.put("res", "ok");
                map.put("param", "employer");
                Customer employer = customerService.getCustomerByShortName(value);
                invoice.setEmployer(employer);
            } else {
                map.put("res", "error");
                map.put("param", "employer");
            }
        } else if (param.equals("consumer")) {
            if (customerService.isCustomerPresent(value)) {
                map.put("res", "ok");
                map.put("param", "consumer");
                Customer consumer = customerService.getCustomerByShortName(value);
                invoice.setConsumer(consumer);
            } else {
                map.put("res", "error");
                map.put("param", "consumer");
            }
        } else if (param.equals("supplier")) {
            Supplier supplier = supplierService.getSupplier(new Integer(value));
            BigDecimal oldExchangeRate = invoice.getExchangeRate();
            invoice.setSupplier(supplier);
            invoice.setExchangeRate(supplier.getCurrency().getExchangeRate());
            map.put("currency", supplier.getCurrency().getName());
            map.put("exchangeRate", supplier.getCurrency().getExchangeRate().toString());
            map.put("isNative", (supplier.getCurrency().getId() == 1)); //todo безобразие

            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                BigDecimal sellingPrice = invoiceItem.getSellingPrice().multiply(oldExchangeRate).divide(invoice.getExchangeRate(), 2, RoundingMode.HALF_UP);
                invoiceItem.setSellingPrice(sellingPrice.setScale(2, RoundingMode.HALF_UP));
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        } else if (param.equals("commonPercent")) {
            BigDecimal percent = new BigDecimal(value.replace(",", ".").trim());
            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                invoiceItem.setPercent(percent);
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        } else if (param.equals("commonDelivery")) {
            Integer deliveryTime = Integer.parseInt(value);
            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                invoiceItem.setDeliveryTime(deliveryTime);
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }
            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        } else if (param.equals("commonTransportCost")) {
            BigDecimal transportationCost = new BigDecimal(value.replace(",", ".").trim());

            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                invoiceItem.setTransportationCost(transportationCost);
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        } else if (param.equals("roundPrice")) {
            Integer roundValue = new Integer(value);
            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                BigDecimal price = invoiceItem.getSellingPrice();
                price = price.movePointLeft(3);
                price = price.setScale(roundValue, BigDecimal.ROUND_HALF_DOWN);
                price = price.movePointRight(3);
                invoiceItem.setSellingPrice(price);
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));

        } else if (param.equals("NDS")) {
            invoice.setNDS(new BigDecimal(value.replace(",", ".")));
            List<Map> items = new LinkedList<Map>();

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        } else if (param.equals("exchangeRate")) {
            BigDecimal exchangeRate = new BigDecimal(value.replace(",", "."));
            BigDecimal oldExchangeRate = invoice.getExchangeRate();
            invoice.setExchangeRate(exchangeRate);
            List<Map> items = new LinkedList<Map>();
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                BigDecimal sellingPrice = invoiceItem.getSellingPrice().multiply(oldExchangeRate).divide(invoice.getExchangeRate(), 2, RoundingMode.HALF_UP);
                invoiceItem.setSellingPrice(sellingPrice.setScale(2, RoundingMode.HALF_UP));
                invoiceItemService.saveInvoiceItem(invoiceItem);
                items.add(invoiceItemToMap(invoiceItem));
            }

            invoiceService.saveInvoice(invoice);
            map.put("items", items);
            map.put("total", numberFormat.format(invoice.getTotal()));
            map.put("sum", numberFormat.format(invoice.getSum()));
            map.put("nds", numberFormat.format(invoice.getNdsPayment()));
        }
        //#######################################
        invoiceService.saveInvoice(invoice);
        return map;
    }


    @RequestMapping("/verifyInvoicePresence")
    public
    @ResponseBody
    Map verifyInvoicePresence(

            @RequestParam(required = false) Boolean isInvoice,
            @RequestParam(required = false) Integer number,

            @RequestParam(required = false) String numberModifier,
            @RequestParam(required = false) String employer,
            @RequestParam(required = false) String consumer) throws ParseException {


        Boolean isAlreadyPresent = false;
        Map map = new HashMap();
        Boolean correct = true;

        List<String> mes = new LinkedList<String>();

        if (number == null) {
            correct = false;
            mes.add("Поле номер не должно быть пустым.");
            map.put("number", false);
        } else {
            isAlreadyPresent = invoiceService.checkPresence(number, numberModifier, isInvoice, new Date());

        }


        if (isAlreadyPresent) {
            correct = false;
            mes.add(((isInvoice) ? "Cчет" : "Кп") + " уже существует.");
        }

        map.put("presence", isAlreadyPresent);


        Boolean employerCorrect = customerService.isCustomerPresent(employer);
        map.put("employer", employerCorrect);
        if (!employerCorrect) {
            correct = false;
            mes.add("Неверно указан заказчик.");
        }


        if (StringUtils.isNotBlank(consumer)) {
            Boolean consumerCorrect = customerService.isCustomerPresent(consumer);
            map.put("consumer", !consumerCorrect);
            if (!consumerCorrect) {
                correct = false;
                mes.add("Неверно указан конечный потребитель. \\u0D ");
            }
        } else {
            map.put("consumer", true);
        }


        map.put("correct", correct);
        map.put("mes", mes);

        return map;
    }


    @RequestMapping("/verifyDivideInvoiceForm")
    public
    @ResponseBody
    Map verifyDivideInvoiceForm(

            @RequestParam(required = false) Boolean isInvoice1,
            @RequestParam(required = false) Boolean isInvoice2,
            @RequestParam(required = false) Integer number1,
            @RequestParam(required = false) Integer number2,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Date date1,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Date date2,
            @RequestParam(required = false) String numberModifier1,
            @RequestParam(required = false) String numberModifier2,
            @RequestParam(required = false) String employer1,
            @RequestParam(required = false) String employer2,
            @RequestParam(required = false) String consumer1,
            @RequestParam(required = false) String consumer2
    ) throws ParseException {


        Boolean isAlreadyPresent1 = false;
        Boolean isAlreadyPresent2 = false;
        Map map = new HashMap();
        Boolean correct = true;

        List<String> mes = new LinkedList<String>();

        if (number1 == null) {
            correct = false;
            mes.add("Поле номер 1 не должно быть пустым.");
            map.put("number1", false);
        } else {
            map.put("number1", true);
            isAlreadyPresent1 = invoiceService.checkPresence(number1, numberModifier1, isInvoice1, date1);

        }


        if (isAlreadyPresent1) {
            correct = false;
            mes.add(((isInvoice1) ? "Cчет" : "Кп") + " уже существует.");
        }

        map.put("presence1", isAlreadyPresent1);


        Boolean employerCorrect1 = customerService.isCustomerPresent(employer1);
        map.put("employer1", employerCorrect1);
        if (!employerCorrect1) {
            correct = false;
            mes.add("Неверно указан заказчик 1.");
        }


        if (StringUtils.isNotBlank(consumer1)) {
            Boolean consumerCorrect1 = customerService.isCustomerPresent(consumer1);
            map.put("consumer1", consumerCorrect1);
            if (!consumerCorrect1) {
                correct = false;
                mes.add("Неверно указан конечный потребитель 1. ");
            }
        } else {
            map.put("consumer1", true);
        }


        if (number2 == null) {
            correct = false;
            mes.add("Поле номер 2 не должно быть пустым.");
            map.put("number2", false);
        } else {
            map.put("number2", true);
            isAlreadyPresent2 = invoiceService.checkPresence(number2, numberModifier2, isInvoice2, date2);

        }


        if (isAlreadyPresent2) {
            correct = false;
            mes.add(((isInvoice2) ? "Cчет" : "Кп") + " уже существует.");
        }

        map.put("presence2", isAlreadyPresent2);


        Boolean employerCorrect2 = customerService.isCustomerPresent(employer2);
        map.put("employer2", employerCorrect2);
        if (!employerCorrect2) {
            correct = false;
            mes.add("Неверно указан заказчик 2.");
        }


        if (StringUtils.isNotBlank(consumer2)) {
            Boolean consumerCorrect2 = customerService.isCustomerPresent(consumer2);
            map.put("consumer2", consumerCorrect2);
            if (!consumerCorrect2) {
                correct = false;
                mes.add("Неверно указан конечный потребитель 2.");
            }
        } else {
            map.put("consumer2", true);
        }


        map.put("correct", correct);
        map.put("mes", mes);

        return map;
    }


    @RequestMapping(value = "/export_report", method = RequestMethod.GET)
    public String exportReport(@RequestParam("invoice_id") Integer invoiceId, @RequestParam("type") String type, ModelMap model, HttpServletRequest request) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        String language = invoice.getSupplier().getLanguage();


        JRDataSource dataSource = new JRBeanCollectionDataSource(invoice.getInvoiceItems());

        Customer employer = invoice.getEmployer();
        City city = cityService.getCity(employer.getCity());

        //  String orgForm = "";
        String cityName = "";
        JAmount jAmount = null;
        Locale locale = null;
        String executorName = "";
        if (language.equals("ru")) {
            //orgForm = employer.getOrgForm().getName();
            cityName = Customer.localityTypeAlias[employer.getLocalityType().intValue()];
            cityName += " " + city.getName();
            jAmount = new JAmountRU();
            locale = new Locale("ru", "RU");
            executorName = invoice.getExecutor().getLastName() + " " + invoice.getExecutor().getName() + " " + invoice.getExecutor().getPatronymic();
        } else if (language.equals("ua")) {
            //     orgForm = employer.getOrgForm().getNameUkr();
            cityName = Customer.localityTypeAliasUkr[employer.getLocalityType().intValue()];
            cityName += " " + city.getNameUkr();
            jAmount = new JAmountUA();
            locale = new Locale("ua", "UA");
            executorName = invoice.getExecutor().getFioUkr();

        } else if (language.equals("en")) {
            //   orgForm = employer.getOrgForm().getNameEng();
            cityName = Customer.localityTypeAliasEn[employer.getLocalityType().intValue()];
            cityName += " " + city.getNameEn();
            jAmount = new JAmountEN();
            locale = new Locale("en", "EN");
        }

        //  model.addAttribute("orgForm", orgForm);
        model.addAttribute("city", cityName);
        model.addAttribute("executorName", executorName);

        model.addAttribute("dataSource", dataSource);
        // Add the report format
        model.addAttribute("format", type);
        model.addAttribute("invoice", invoice);
        final int currencyId = invoice.getSupplier().getCurrency().getId().intValue();
        model.addAttribute("strTotal", jAmount.getAmount(currencyId, invoice.getTotal()));
        String fileName = "invoice_" + invoice.getNumber() + ((StringUtils.isBlank(invoice.getNumberModifier())) ? "" : ("_" + invoice.getNumberModifier()));
        model.addAttribute("name", fileName);

        //String path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        //todo надо разобраться: локалхот нехорошо
        String path = "http://localhost:8080";
        path += request.getContextPath() + "/images/reportImages/header_" + language + ".png";
        LOGGER.info("ImagePath = " + path);
        model.addAttribute("path", path);

        model.addAttribute(JRParameter.REPORT_LOCALE, locale);
//        ResourceBundle bundle = ResourceBundle .getBundle("i18n", new UTF8Control());
//        model.addAttribute(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
        return "invoiceReport";
    }

    @RequestMapping("/view_payments")
    public String viewPayments(@RequestParam("invoice_id") Integer invoiceId, Map<String, Object> map) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        map.put("invoice", invoice);
        return "payments";
    }

    @RequestMapping("/add_payment")
    public
    @ResponseBody
    Map addPayment(@RequestParam("invoice_id") Integer invoiceId, HttpServletRequest request)
            throws Exception {

        Invoice invoice = invoiceService.getInvoice(invoiceId);
        Payment payment = new Payment();
//
        payment.setExchangeRate(new BigDecimal(request.getParameter("exchangeRate").replaceAll("[^0-9,.]", "").replace(",", ".")));
        BigDecimal paymentSum = new BigDecimal(request.getParameter("paymentSum").replaceAll("[^0-9,.-]", "").replace(",", "."));
        Boolean otkaz = paymentSum.compareTo(BigDecimal.ZERO) < 0;

        payment.setPaymentSum(paymentSum);
        payment.setPurpose(new Integer(request.getParameter("purpose")));
        payment.setDate((new SimpleDateFormat("dd.MM.yyyy")).parse(request.getParameter("date")));
        // payment.setInvoice(invoice);
        paymentService.addPayment(payment);

        invoice.addPayment(payment);
        InvoiceUtils.setupInvoice(invoice);
        Map result = new HashMap();
        result.put("isStateChanged", false);
        if (invoice.isPaymentMade() && invoice.isDeliveryMade()) {

            invoice.getBooking().setCurrentState(Booking.STATE_ISP);
            invoice.setCurrentState(Invoice.STATE_ISP);
            result.put("isStateChanged", true);
            result.put("state", "Исполнен");
        }


        if ((invoice.getCurrentState() != Invoice.STATE_CH_ISP) &&
                (invoice.getCurrentState() != Invoice.STATE_OTGR)) {
            invoice.setCurrentState(Invoice.STATE_CH_ISP);
            result.put("isStateChanged", true);
            result.put("state", "Частично исполнен");
        }

        if (invoice.isPaymentMade() && !invoice.isDeliveryMade()) {

            invoice.setCurrentState(Invoice.STATE_OPLACH);
            result.put("isStateChanged", true);
            result.put("state", "Оплачен");
        }

        if (otkaz && Invoice.STATE_OTKAZ != invoice.getCurrentState()) {
            invoice.setCurrentState(Invoice.STATE_OTKAZ);
            result.put("isStateChanged", true);
            result.put("state", "Отказ");
        }


        invoiceService.saveInvoice(invoice);


        return result;
    }


    private Map invoiceItemToMap(InvoiceItem item) {
        Map result = new HashMap();
        result.put("quantity", item.getQuantity());
        result.put("percent", item.calculatePercent().toString());
        result.put("sellingPrice", item.getSellingPrice().toString());
        result.put("sum", item.getSum().toString());
        result.put("transportationCost", item.getTransportationCost().toString());
        result.put("additionalCost", item.getAdditionalCost().toString());
        result.put("deliveryTime", item.getDeliveryTime());
        result.put("id", item.getId());
        return result;
    }

    /*
        @RequestMapping(value = "/new", method = RequestMethod.GET)
        public void doSalesMultiRep(@RequestParam("invoice_id") Integer invoiceId, @RequestParam("lang") String language, HttpServletResponse response, HttpServletRequest request) throws JRException, IOException {
            Invoice invoice = invoiceService.getInvoice(invoiceId);

            JasperReport report = JasperCompileManager.compileReport("D:\\projects\\~MANOMETR\\SHM\\src\\main\\resources\\invoice_ru.jrxml");
            Map<String, Object> parameters = new HashMap<String, Object>();
            Customer employer = invoice.getEmployer();
            City city = cityService.getCity(employer.getCity());
            String cityName = "";
            JAmount jAmount = null;
            Locale locale = null;

            if (language.equals("ru")) {
                cityName = Customer.localityTypeAlias[employer.getLocalityType().intValue()];
                cityName += " " + city.getName();
                jAmount = new JAmountRU();
                locale = new Locale("ru", "RU");
            } else if (language.equals("ua")) {
                cityName = Customer.localityTypeAliasUkr[employer.getLocalityType().intValue()];
                cityName += " " + city.getNameUkr();
                jAmount = new JAmountUA();
                locale = new Locale("ua", "UA");
            } else if (language.equals("en")) {
                cityName = Customer.localityTypeAliasEn[employer.getLocalityType().intValue()];
                cityName += " " + city.getNameEn();
                jAmount = new JAmountEN();
                locale = new Locale("en", "EN");
            }
            parameters.put(JRParameter.REPORT_LOCALE, locale);


    //        ResourceBundle bundle = ResourceBundle.getBundle("i18n", new UTF8Control());
    //        parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);


            //  Customer employer = customerService.getCustomerByShortName(invoice.getEmployer());
            // String orgForm = employer.getOrgForm().getName();
           // parameters.put("orgForm", orgForm);

            //  City city = cityService.getCity(employer.getCity());
            //String cityName = Customer.localityTypeAlias[employer.getLocalityType().intValue()];
            //cityName += " " + city.getName();
            parameters.put("city", cityName);

            parameters.put("invoice", invoice);
            final int currencyId = invoice.getSupplier().getCurrency().getId();
            //  JAmount jAmount = new JAmountRU();
            parameters.put("strTotal", jAmount.getAmount(currencyId, invoice.getTotal().divide(invoice.getExchangeRate(), 2, RoundingMode.HALF_UP)));

            String path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            path += request.getContextPath() + "/images/reportImages/header_" + language + ".png";
            parameters.put("path", path);


            List<InvoiceItem> invoiceItems = invoice.getInvoiceItems();
            JRDataSource dataSource = new JRBeanCollectionDataSource(invoiceItems);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);


            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();


            // JasperViewer.viewReport(jasperPrint);

    //        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        }

    */
    private String getName() {
        String name = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            name = "[" + authentication.getName() + "] ";
        }
        return name;
    }


    private Invoice createCopyOfInvoice(Invoice parent, String employerShortName, String consumerShortName, Date date, Boolean invoice, Integer number, String numberModifier) {
        Invoice newInvoice = new Invoice();
        Customer employer = customerService.getCustomerByShortName(employerShortName);
        Customer consumer = customerService.getCustomerByShortName(consumerShortName);
        newInvoice.setEmployer(employer);
        newInvoice.setConsumer(consumer);
        newInvoice.setExecutor(employer.getPerson());
        newInvoice.setNDS(new BigDecimal("20"));
        newInvoice.setPurpose(Invoice.PURPOSE_POSTAVKA);
        newInvoice.setDate(date);
        newInvoice.setInvoice(invoice);
        newInvoice.setNumber(number);
        newInvoice.setNumberModifier(numberModifier);
        newInvoice.setValidity(10);
        newInvoice.setPaymentOnTheNotice(parent.getPaymentOnTheNotice());
        newInvoice.setPrepayment(parent.getPrepayment());
        newInvoice.setPostPay(parent.getPostPay());
        newInvoice.setDaysAfterDelivery(parent.getDaysAfterDelivery());

        newInvoice.setCurrentState(Invoice.STATE_CHERN);
        newInvoice.setDeliveryTime("45 - 60 днів з моменту передоплати.");


        newInvoice.setSupplier(parent.getSupplier());
        newInvoice.setExchangeRate(parent.getExchangeRate());


        BigDecimal oldExchangeRate = parent.getExchangeRate();
        // BigDecimal newExchangeRate = newInvoice.getExchangeRate();
        newInvoice.setInvoiceItems(new LinkedList<InvoiceItem>());


        newInvoice.setNotes(parent.getNotes());
        invoiceService.saveInvoice(newInvoice);
        for (InvoiceItem item : parent.getInvoiceItems()) {
            InvoiceItem clone = item.getClone(oldExchangeRate, oldExchangeRate);
            clone.setInvoice(newInvoice);
            invoiceItemService.saveInvoiceItem(clone);
            newInvoice.addInvoiceItems(clone);
        }
        invoiceService.saveInvoice(newInvoice);
        return newInvoice;
    }


}