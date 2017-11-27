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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "scenario")
@NamedQueries({
    @NamedQuery(name = "Scenario.findAll", query = "SELECT s FROM Scenario s")})
public class Scenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "scenarioId", nullable = false, length = 10)
    private String scenarioId;
    @Column(name = "name", length = 100)
    private String name;
    @Column(name = "description", length = 500)
    private String description;
    @OneToMany(mappedBy = "scenario")
    private List<Dataset> datasetList;

    public Scenario() {
    }

    public Scenario(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
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

    public List<Dataset> getDatasetList() {
        return datasetList;
    }

    public void setDatasetList(List<Dataset> datasetList) {
        this.datasetList = datasetList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (scenarioId != null ? scenarioId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Scenario)) {
            return false;
        }
        Scenario other = (Scenario) object;
        if ((this.scenarioId == null && other.scenarioId != null) || (this.scenarioId != null && !this.scenarioId.equals(other.scenarioId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Scenario[ scenarioId=" + scenarioId + " ]";
    }
    
}
