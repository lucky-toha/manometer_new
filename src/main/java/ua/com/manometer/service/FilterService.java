package ua.com.manometer.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.manometer.dao.BaseDAO;
import ua.com.manometer.model.invoice.filter.InvoiceFilter;


@Service
public class FilterService {

    @Autowired
    private BaseDAO baseDAO;

    @Transactional
    public void saveFilter(Object filter){
        baseDAO.saveOrUpdate(filter);
    }
}
