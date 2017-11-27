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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "metavariableenum")
@NamedQueries({
    @NamedQuery(name = "Metavariableenum.findAll", query = "SELECT m FROM Metavariableenum m")})
public class Metavariableenum implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "stdVarId", nullable = false, length = 10)
    private String stdVarId;
    @Column(name = "name", length = 45)
    private String name;
    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "isSuperficial")
    private Boolean isSuperficial;
    @OneToMany(mappedBy = "metavariableenum")
    private List<Datavariable> datavariableList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "metavariableenum")
    private List<Predictand> predictandList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "metavariableenum")
    private List<PredictorHasPredictors> predictorHasPredictorsList;

    public Metavariableenum() {
    }

    public Metavariableenum(String stdVarId) {
        this.stdVarId = stdVarId;
    }

    public String getStdVarId() {
        return stdVarId;
    }

    public void setStdVarId(String stdVarId) {
        this.stdVarId = stdVarId;
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

    public Boolean getIsSuperficial() {
        return isSuperficial;
    }

    public void setIsSuperficial(Boolean isSuperficial) {
        this.isSuperficial = isSuperficial;
    }

    public List<Datavariable> getDatavariableList() {
        return datavariableList;
    }

    public void setDatavariableList(List<Datavariable> datavariableList) {
        this.datavariableList = datavariableList;
    }

    public List<Predictand> getPredictandList() {
        return predictandList;
    }

    public void setPredictandList(List<Predictand> predictandList) {
        this.predictandList = predictandList;
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
        hash += (stdVarId != null ? stdVarId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Metavariableenum)) {
            return false;
        }
        Metavariableenum other = (Metavariableenum) object;
        if ((this.stdVarId == null && other.stdVarId != null) || (this.stdVarId != null && !this.stdVarId.equals(other.stdVarId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Metavariableenum[ stdVarId=" + stdVarId + " ]";
    }
    
}
