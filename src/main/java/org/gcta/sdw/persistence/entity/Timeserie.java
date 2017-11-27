/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "timeserie")
@NamedQueries({
    @NamedQuery(name = "Timeserie.findAll", query = "SELECT t FROM Timeserie t")})
public class Timeserie implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TimeseriePK timeseriePK;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "value", precision = 22)
    private Double value;
    @JoinColumns({
        @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "dataId", referencedColumnName = "dataId", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "variableId", referencedColumnName = "variableId", nullable = false, insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Datavariable datavariable;

    public Timeserie() {
    }

    public Timeserie(TimeseriePK timeseriePK) {
        this.timeseriePK = timeseriePK;
    }

    public Timeserie(int datasetId, int dataId, int variableId, Date dateTime) {
        this.timeseriePK = new TimeseriePK(datasetId, dataId, variableId, dateTime);
    }

    public TimeseriePK getTimeseriePK() {
        return timeseriePK;
    }

    public void setTimeseriePK(TimeseriePK timeseriePK) {
        this.timeseriePK = timeseriePK;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Datavariable getDatavariable() {
        return datavariable;
    }

    public void setDatavariable(Datavariable datavariable) {
        this.datavariable = datavariable;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (timeseriePK != null ? timeseriePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Timeserie)) {
            return false;
        }
        Timeserie other = (Timeserie) object;
        if ((this.timeseriePK == null && other.timeseriePK != null) || (this.timeseriePK != null && !this.timeseriePK.equals(other.timeseriePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Timeserie[ timeseriePK=" + timeseriePK + " ]";
    }
    
}
