/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 * @author LuisAugusto
 *
 *
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("true")
public class MeuPlano implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Column(allowsNull = "false", name = "owner_perfil")
    @Persistent
    private Long owner;

    @Column(allowsNull = "false", name = "invoice")
    @Persistent
    private String invoice;

    @Column(allowsNull = "false", name = "transaction_id")
    @Persistent
    private String transaction_id;

    @Persistent(mappedBy = "plano")
    private List<PaypalLog> logs;

    @Column(allowsNull = "false", name = "plano")
    @Persistent
    private TipoPlano plano;
    @Column(allowsNull = "false", name = "productName")
    @Persistent
    private String productName;
    @Column(allowsNull = "false", name = "quantity")
    @Persistent
    private String quantity;
    @Column(allowsNull = "false", name = "amount")
    @Persistent
    private String amount;
    @Column(allowsNull = "false", name = "payer_f_name")
    @Persistent
    private String payer_f_name;
    @Column(allowsNull = "false", name = "payer_l_name")
    @Persistent
    private String payer_l_name;
    @Column(allowsNull = "false", name = "payer_address")
    @Persistent
    private String payer_address;
    @Column(allowsNull = "false", name = "payer_city")
    @Persistent
    private String payer_city;
    @Column(allowsNull = "false", name = "payer_state")
    @Persistent
    private String payer_state;
    @Column(allowsNull = "false", name = "payer_zip")
    @Persistent
    private String payer_zip;
    @Column(allowsNull = "false", name = "payer_country")
    @Persistent
    private String payer_country;
    @Column(allowsNull = "false", name = "payer_email")
    @Persistent
    private String payer_email;
    @Column(allowsNull = "false", name = "payment_status")
    @Persistent
    private String payment_status;
    @Column(allowsNull = "false", name = "posted_date")
    @Persistent
    private Date posted_date;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public List<PaypalLog> getLogs() {
        return logs;
    }

    public void setLogs(List<PaypalLog> logs) {
        this.logs = logs;
    }

    public TipoPlano getPlano() {
        return plano;
    }

    public void setPlano(TipoPlano plano) {
        this.plano = plano;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPayer_f_name() {
        return payer_f_name;
    }

    public void setPayer_f_name(String payer_f_name) {
        this.payer_f_name = payer_f_name;
    }

    public String getPayer_l_name() {
        return payer_l_name;
    }

    public void setPayer_l_name(String payer_l_name) {
        this.payer_l_name = payer_l_name;
    }

    public String getPayer_address() {
        return payer_address;
    }

    public void setPayer_address(String payer_address) {
        this.payer_address = payer_address;
    }

    public String getPayer_city() {
        return payer_city;
    }

    public void setPayer_city(String payer_city) {
        this.payer_city = payer_city;
    }

    public String getPayer_state() {
        return payer_state;
    }

    public void setPayer_state(String payer_state) {
        this.payer_state = payer_state;
    }

    public String getPayer_zip() {
        return payer_zip;
    }

    public void setPayer_zip(String payer_zip) {
        this.payer_zip = payer_zip;
    }

    public String getPayer_country() {
        return payer_country;
    }

    public void setPayer_country(String payer_country) {
        this.payer_country = payer_country;
    }

    public String getPayer_email() {
        return payer_email;
    }

    public void setPayer_email(String payer_email) {
        this.payer_email = payer_email;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public Date getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(Date posted_date) {
        this.posted_date = posted_date;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.key);
        hash = 67 * hash + Objects.hashCode(this.owner);
        hash = 67 * hash + Objects.hashCode(this.invoice);
        hash = 67 * hash + Objects.hashCode(this.transaction_id);
        hash = 67 * hash + Objects.hashCode(this.posted_date);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MeuPlano other = (MeuPlano) obj;
        if (!Objects.equals(this.transaction_id, other.transaction_id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "key:" + key + ", owner:" + owner + ", invoice:" + invoice + ", transaction_id:" + transaction_id + ", logs:" + logs + ", plano:" + plano + 
                ", productName:" + productName + ", quantity:" + quantity + ", amount:" + amount + ", payer_f_name:" + payer_f_name + ", payer_l_name:" + payer_l_name + 
                ", payer_address=" + payer_address + ", payer_city:" + payer_city + ", payer_state:" + payer_state + ", payer_zip:" + payer_zip + 
                ", payer_country:" + payer_country + ", payer_email:" + payer_email + ", payment_status:" + payment_status + 
                ", posted_date:" + posted_date + '}';
    }

}
