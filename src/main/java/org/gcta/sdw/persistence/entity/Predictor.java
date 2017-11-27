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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "predictor")
@NamedQueries({
    @NamedQuery(name = "Predictor.findAll", query = "SELECT p FROM Predictor p")})
public class Predictor implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "experimentId", nullable = false)
    private Integer experimentId;
    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "longStart", precision = 22)
    private Double longStart;
    @Column(name = "longEnd", precision = 22)
    private Double longEnd;
    @Column(name = "longRes", precision = 22)
    private Double longRes;
    @Column(name = "latStart", precision = 22)
    private Double latStart;
    @Column(name = "latEnd", precision = 22)
    private Double latEnd;
    @Column(name = "latRes", precision = 22)
    private Double latRes;
    @JoinColumn(name = "experimentId", referencedColumnName = "experimentId", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Experiment experiment;
    @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false)
    @ManyToOne(optional = false)
    private Dataset dataset;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "predictor")
    private List<PredictorHasPredictors> predictorHasPredictorsList;

    public Predictor() {
    }

    public Predictor(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getLongStart() {
        return longStart;
    }

    public void setLongStart(Double longStart) {
        this.longStart = longStart;
    }

    public Double getLongEnd() {
        return longEnd;
    }

    public void setLongEnd(Double longEnd) {
        this.longEnd = longEnd;
    }

    public Double getLongRes() {
        return longRes;
    }

    public void setLongRes(Double longRes) {
        this.longRes = longRes;
    }

    public Double getLatStart() {
        return latStart;
    }

    public void setLatStart(Double latStart) {
        this.latStart = latStart;
    }

    public Double getLatEnd() {
        return latEnd;
    }

    public void setLatEnd(Double latEnd) {
        this.latEnd = latEnd;
    }

    public Double getLatRes() {
        return latRes;
    }

    public void setLatRes(Double latRes) {
        this.latRes = latRes;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public List<PredictorHasPredictors> getPredictorHasPredictorsList() {
        return predictorHasPredictorsList;
    }

    public void setPredictorHasPredictorsList(List<PredictorHasPredictors> predictorHasPredictorsList) {
        this.predictorHasPredictorsList = predictorHasPredictorsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (experimentId != null ? experimentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Predictor)) {
            return false;
        }
        Predictor other = (Predictor) object;
        if ((this.experimentId == null && other.experimentId != null) || (this.experimentId != null && !this.experimentId.equals(other.experimentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Predictor[ experimentId=" + experimentId + " ]";
    }
    
}
