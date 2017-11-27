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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "dimension")
@NamedQueries({
    @NamedQuery(name = "Dimension.findAll", query = "SELECT d FROM Dimension d")})
public class Dimension implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DimensionPK dimensionPK;
    @Column(name = "size")
    private Integer size;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "a", precision = 22)
    private Double a;
    @Column(name = "b", precision = 22)
    private Double b;
    @Column(name = "delta", precision = 22)
    private Double delta;
    @Column(name = "units", length = 45)
    private String units;
    @Column(name = "aDate")
    @Temporal(TemporalType.DATE)
    private Date aDate;
    @Column(name = "bDate")
    @Temporal(TemporalType.DATE)
    private Date bDate;
    @Column(name = "deltaDate", precision = 22)
    private Double deltaDate;
    @Column(name = "unitsDeltaDate", length = 45)
    private String unitsDeltaDate;
    @JoinColumns({
        @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "dataId", referencedColumnName = "dataId", nullable = false, insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Data data;

    public Dimension() {
    }

    public Dimension(DimensionPK dimensionPK) {
        this.dimensionPK = dimensionPK;
    }

    public Dimension(int datasetId, int dataId, String name) {
        this.dimensionPK = new DimensionPK(datasetId, dataId, name);
    }

    public DimensionPK getDimensionPK() {
        return dimensionPK;
    }

    public void setDimensionPK(DimensionPK dimensionPK) {
        this.dimensionPK = dimensionPK;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public Double getB() {
        return b;
    }

    public void setB(Double b) {
        this.b = b;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Date getADate() {
        return aDate;
    }

    public void setADate(Date aDate) {
        this.aDate = aDate;
    }

    public Date getBDate() {
        return bDate;
    }

    public void setBDate(Date bDate) {
        this.bDate = bDate;
    }

    public Double getDeltaDate() {
        return deltaDate;
    }

    public void setDeltaDate(Double deltaDate) {
        this.deltaDate = deltaDate;
    }

    public String getUnitsDeltaDate() {
        return unitsDeltaDate;
    }

    public void setUnitsDeltaDate(String unitsDeltaDate) {
        this.unitsDeltaDate = unitsDeltaDate;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dimensionPK != null ? dimensionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dimension)) {
            return false;
        }
        Dimension other = (Dimension) object;
        if ((this.dimensionPK == null && other.dimensionPK != null) || (this.dimensionPK != null && !this.dimensionPK.equals(other.dimensionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Dimension[ dimensionPK=" + dimensionPK + " ]";
    }
    
}
