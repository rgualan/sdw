/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "datavariable")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Datavariable.findAll", query = "SELECT d FROM Datavariable d"),
    @NamedQuery(name = "Datavariable.findByDatasetId", query = "SELECT d FROM Datavariable d WHERE d.datavariablePK.datasetId = :datasetId"),
    @NamedQuery(name = "Datavariable.findByDataId", query = "SELECT d FROM Datavariable d WHERE d.datavariablePK.dataId = :dataId"),
    @NamedQuery(name = "Datavariable.findByVariableId", query = "SELECT d FROM Datavariable d WHERE d.datavariablePK.variableId = :variableId"),
    @NamedQuery(name = "Datavariable.findByShortName", query = "SELECT d FROM Datavariable d WHERE d.shortName = :shortName"),
    @NamedQuery(name = "Datavariable.findByFullName", query = "SELECT d FROM Datavariable d WHERE d.fullName = :fullName"),
    @NamedQuery(name = "Datavariable.findByDescription", query = "SELECT d FROM Datavariable d WHERE d.description = :description"),
    @NamedQuery(name = "Datavariable.findByDataType", query = "SELECT d FROM Datavariable d WHERE d.dataType = :dataType"),
    @NamedQuery(name = "Datavariable.findByUnits", query = "SELECT d FROM Datavariable d WHERE d.units = :units"),
    @NamedQuery(name = "Datavariable.findByLevels", query = "SELECT d FROM Datavariable d WHERE d.levels = :levels"),
    @NamedQuery(name = "Datavariable.findByLevelUnits", query = "SELECT d FROM Datavariable d WHERE d.levelUnits = :levelUnits"),
    @NamedQuery(name = "Datavariable.findByFromDate", query = "SELECT d FROM Datavariable d WHERE d.fromDate = :fromDate"),
    @NamedQuery(name = "Datavariable.findByToDate", query = "SELECT d FROM Datavariable d WHERE d.toDate = :toDate")})
public class Datavariable implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DatavariablePK datavariablePK;
    @Column(name = "shortName")
    private String shortName;
    @Column(name = "fullName")
    private String fullName;
    @Column(name = "description")
    private String description;
    @Column(name = "dataType")
    private String dataType;
    @Column(name = "units")
    private String units;
    @Column(name = "levels")
    private String levels;
    @Column(name = "levelUnits")
    private String levelUnits;
    @Column(name = "fromDate")
    @Temporal(TemporalType.DATE)
    private Date fromDate;
    @Column(name = "toDate")
    @Temporal(TemporalType.DATE)
    private Date toDate;
    @JoinColumn(name = "timePeriodicityId", referencedColumnName = "timeId")
    @ManyToOne
    private Timeperiodicity timeperiodicity;
    @JoinColumn(name = "stdVarId", referencedColumnName = "stdVarId")
    @ManyToOne
    private Metavariableenum metavariableenum;
    @JoinColumns({
        @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", insertable = false, updatable = false),
        @JoinColumn(name = "dataId", referencedColumnName = "dataId", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Data data;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datavariable")
    private List<Timeserie> timeserieList;

    public Datavariable() {
    }

    public Datavariable(DatavariablePK datavariablePK) {
        this.datavariablePK = datavariablePK;
    }

    public Datavariable(int datasetId, int dataId, int variableId) {
        this.datavariablePK = new DatavariablePK(datasetId, dataId, variableId);
    }

    public DatavariablePK getDatavariablePK() {
        return datavariablePK;
    }

    public void setDatavariablePK(DatavariablePK datavariablePK) {
        this.datavariablePK = datavariablePK;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getLevels() {
        return levels;
    }

    public void setLevels(String levels) {
        this.levels = levels;
    }

    public String getLevelUnits() {
        return levelUnits;
    }

    public void setLevelUnits(String levelUnits) {
        this.levelUnits = levelUnits;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Timeperiodicity getTimeperiodicity() {
        return timeperiodicity;
    }

    public void setTimeperiodicity(Timeperiodicity timeperiodicity) {
        this.timeperiodicity = timeperiodicity;
    }

    public Metavariableenum getMetavariableenum() {
        return metavariableenum;
    }

    public void setMetavariableenum(Metavariableenum metavariableenum) {
        this.metavariableenum = metavariableenum;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @XmlTransient
    public List<Timeserie> getTimeserieList() {
        return timeserieList;
    }

    public void setTimeserieList(List<Timeserie> timeserieList) {
        this.timeserieList = timeserieList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (datavariablePK != null ? datavariablePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Datavariable)) {
            return false;
        }
        Datavariable other = (Datavariable) object;
        if ((this.datavariablePK == null && other.datavariablePK != null) || (this.datavariablePK != null && !this.datavariablePK.equals(other.datavariablePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Datavariable[ datavariablePK=" + datavariablePK + " ]";
    }
    
}
