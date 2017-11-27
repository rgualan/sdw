/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "dataset")
@NamedQueries({
    @NamedQuery(name = "Dataset.findAll", query = "SELECT d FROM Dataset d")})
public class Dataset implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "datasetId", nullable = false)
    private Integer datasetId;
    @Column(name = "shortName", length = 50)
    private String shortName;
    @Column(name = "longName", length = 250)
    private String longName;
    @Column(name = "description", length = 5000)
    private String description;
    @Column(name = "reference", length = 200)
    private String reference;
    @Column(name = "dateA")
    @Temporal(TemporalType.DATE)
    private Date dateA;
    @Column(name = "dateB")
    @Temporal(TemporalType.DATE)
    private Date dateB;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "longA", precision = 22)
    private Double longA;
    @Column(name = "longB", precision = 22)
    private Double longB;
    @Column(name = "longDelta", precision = 22)
    private Double longDelta;
    @Column(name = "latA", precision = 22)
    private Double latA;
    @Column(name = "latB", precision = 22)
    private Double latB;
    @Column(name = "latDelta", precision = 22)
    private Double latDelta;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private List<Predictor> predictorList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private List<Data> dataList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private List<Predictand> predictandList;
    @JoinColumn(name = "scenarioId", referencedColumnName = "scenarioId")
    @ManyToOne
    private Scenario scenario;
    @JoinColumn(name = "timeId", referencedColumnName = "timeId")
    @ManyToOne
    private Timeperiodicity timeperiodicity;
    @JoinColumn(name = "centerId", referencedColumnName = "centerId")
    @ManyToOne
    private Center center;
    @JoinColumn(name = "dsTypeId", referencedColumnName = "dsTypeId", nullable = false)
    @ManyToOne(optional = false)
    private Datasettype datasettype;

    public Dataset() {
    }

    public Dataset(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getDateA() {
        return dateA;
    }

    public void setDateA(Date dateA) {
        this.dateA = dateA;
    }

    public Date getDateB() {
        return dateB;
    }

    public void setDateB(Date dateB) {
        this.dateB = dateB;
    }

    public Double getLongA() {
        return longA;
    }

    public void setLongA(Double longA) {
        this.longA = longA;
    }

    public Double getLongB() {
        return longB;
    }

    public void setLongB(Double longB) {
        this.longB = longB;
    }

    public Double getLongDelta() {
        return longDelta;
    }

    public void setLongDelta(Double longDelta) {
        this.longDelta = longDelta;
    }

    public Double getLatA() {
        return latA;
    }

    public void setLatA(Double latA) {
        this.latA = latA;
    }

    public Double getLatB() {
        return latB;
    }

    public void setLatB(Double latB) {
        this.latB = latB;
    }

    public Double getLatDelta() {
        return latDelta;
    }

    public void setLatDelta(Double latDelta) {
        this.latDelta = latDelta;
    }

    public List<Predictor> getPredictorList() {
        return predictorList;
    }

    public void setPredictorList(List<Predictor> predictorList) {
        this.predictorList = predictorList;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public List<Predictand> getPredictandList() {
        return predictandList;
    }

    public void setPredictandList(List<Predictand> predictandList) {
        this.predictandList = predictandList;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Timeperiodicity getTimeperiodicity() {
        return timeperiodicity;
    }

    public void setTimeperiodicity(Timeperiodicity timeperiodicity) {
        this.timeperiodicity = timeperiodicity;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public Datasettype getDatasettype() {
        return datasettype;
    }

    public void setDatasettype(Datasettype datasettype) {
        this.datasettype = datasettype;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (datasetId != null ? datasetId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dataset)) {
            return false;
        }
        Dataset other = (Dataset) object;
        if ((this.datasetId == null && other.datasetId != null) || (this.datasetId != null && !this.datasetId.equals(other.datasetId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Dataset[ datasetId=" + datasetId + " ]";
    }
    
}
