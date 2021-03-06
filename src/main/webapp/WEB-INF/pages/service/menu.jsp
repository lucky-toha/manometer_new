<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>


<div id="menu">
    <ul id="sidebarmenu1">
        <li><a href="../invoices/">Счета и к.п.</a></li>
        <li><a href="../bookings/">Заказ-наряды</a></li>

        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <li><a href="../suppliers/">Поставщики</a></li>


            <LI><A href="#">Прайс</A>
                <UL>
                    <LI><A href="../matrix_price/">Матричный</A></LI>
                    <LI><A href="../list_price/">Список</A></LI>
                    <LI><A href="../option_price/">Опции</A></LI>
                </UL>
            </LI>

            <LI><A href="#">Таб. совм.</A>
                <UL>
                    <LI><A href="../compatibility/smart">Смарт</A></LI>
                    <LI><A href="../compatibility/co">ЦО</A></LI>
                    <LI><A href="../compatibility/op">ОП</A></LI>
                    <LI><A href="../compatibility/ao">АО</A></LI>
                    <LI><A href="../compatibility/compatibilityTable" target="_blank">Таблица</A></LI>
                </UL>
            </LI>
        </sec:authorize>

        <LI><A href="#">Статистика</A>
            <UL>
                <LI><A href="statisticAction.do?method=viewPaymentArrears">Задолженности по оплате</A></LI>

            </UL>
        </LI>


        <%--<li><a href="priceAction.do?method=redactMatrixPriceForward">Ред матр. прайс.</a></li>--%>
        <%--<li><a href="priceAction.do?method=redactListPriceForward">Ред сп. прайс.</a>--%>
        <%--<li><a href="priceAction.do?method=redactOptionPriceForward">Ред оп. прайс.</a></li>--%>


        <%--<li><a href="testAction.do?method=redactCoForward">Таб. цо совм.</a></li>--%>
        <%--<li><a href="testAction.do?method=redactOpForward">Таб. oп совм.</a></li>--%>
        <%--<li><a href="testAction.do?method=redactAoForward">Таб. аo совм.</a></li>--%>


        <li><a href="../customers/">Предприятия</a></li>
        <li><a href="../contacts/">Конт. лица</a></li>


            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <li><a href="../users/">Пользователи</a></li>
            </sec:authorize>


        <li><a href="../logout">Выход</a></li>
    </ul>
</div>

