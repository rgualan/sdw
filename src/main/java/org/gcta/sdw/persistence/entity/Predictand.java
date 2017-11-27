/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "predictand")
@NamedQueries({
    @NamedQuery(name = "Predictand.findAll", query = "SELECT p FROM Predictand p")})
public class Predictand implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "experimentId", nullable = false)
    private Integer experimentId;
    @JoinTable(name = "predictand_has_station", joinColumns = {
        @JoinColumn(name = "experimentId", referencedColumnName = "experimentId", nullable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false),
        @JoinColumn(name = "dataId", referencedColumnName = "dataId", nullable = false)})
    @ManyToMany
    private List<Data> dataList;
    @JoinColumn(name = "stdVarId", referencedColumnName = "stdVarId", nullable = false)
    @ManyToOne(optional = false)
    private Metavariableenum metavariableenum;
    @JoinColumn(name = "experimentId", referencedColumnName = "experimentId", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Experiment experiment;
    @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false)
    @ManyToOne(optional = false)
    private Dataset dataset;

    public Predictand() {
    }

    public Predictand(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public Metavariableenum getMetavariableenum() {
        return metavariableenum;
    }

    public void setMetavariableenum(Metavariableenum metavariableenum) {
        this.metavariableenum = metavariableenum;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (experimentId != null ? experimentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Predictand)) {
            return false;
        }
        Predictand other = (Predictand) object;
        if ((this.experimentId == null && other.experimentId != null) || (this.experimentId != null && !this.experimentId.equals(other.experimentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Predictand[ experimentId=" + experimentId + " ]";
    }
    
}
