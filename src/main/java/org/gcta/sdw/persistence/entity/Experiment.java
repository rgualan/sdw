/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "experiment")
@NamedQueries({
    @NamedQuery(name = "Experiment.findAll", query = "SELECT e FROM Experiment e")})
public class Experiment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "experimentId", nullable = false)
    private Integer experimentId;
    @Column(name = "creationDate")
    @Temporal(TemporalType.DATE)
    private Date creationDate;
    @Column(name = "name", length = 45)
    private String name;
    @Column(name = "description", length = 500)
    private String description;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "experiment")
    private Predictor predictor;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "experiment")
    private Predictand predictand;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "experiment")
    private Configuration configuration;

    public Experiment() {
    }

    public Experiment(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public void setPredictor(Predictor predictor) {
        this.predictor = predictor;
    }

    public Predictand getPredictand() {
        return predictand;
    }

    public void setPredictand(Predictand predictand) {
        this.predictand = predictand;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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
        if (!(object instanceof Experiment)) {
            return false;
        }
        Experiment other = (Experiment) object;
        if ((this.experimentId == null && other.experimentId != null) || (this.experimentId != null && !this.experimentId.equals(other.experimentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Experiment[ experimentId=" + experimentId + " ]";
    }
    
}
