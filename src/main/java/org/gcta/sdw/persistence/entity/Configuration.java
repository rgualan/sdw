/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
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

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "configuration")
@NamedQueries({
    @NamedQuery(name = "Configuration.findAll", query = "SELECT c FROM Configuration c")})
public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "experimentId", nullable = false)
    private Integer experimentId;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "testPercentage", precision = 22)
    private Double testPercentage;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "configuration")
    private List<Parameter> parameterList;
    @JoinColumn(name = "dmethodId", referencedColumnName = "dmethodId", nullable = false)
    @ManyToOne(optional = false)
    private Downscalingmethod downscalingmethod;
    @JoinColumn(name = "experimentId", referencedColumnName = "experimentId", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Experiment experiment;

    public Configuration() {
    }

    public Configuration(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Double getTestPercentage() {
        return testPercentage;
    }

    public void setTestPercentage(Double testPercentage) {
        this.testPercentage = testPercentage;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    public Downscalingmethod getDownscalingmethod() {
        return downscalingmethod;
    }

    public void setDownscalingmethod(Downscalingmethod downscalingmethod) {
        this.downscalingmethod = downscalingmethod;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
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
        if (!(object instanceof Configuration)) {
            return false;
        }
        Configuration other = (Configuration) object;
        if ((this.experimentId == null && other.experimentId != null) || (this.experimentId != null && !this.experimentId.equals(other.experimentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Configuration[ experimentId=" + experimentId + " ]";
    }
    
}
