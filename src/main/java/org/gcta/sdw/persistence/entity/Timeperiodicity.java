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
@Table(name = "timeperiodicity")
@NamedQueries({
    @NamedQuery(name = "Timeperiodicity.findAll", query = "SELECT t FROM Timeperiodicity t")})
public class Timeperiodicity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "timeId", nullable = false, length = 25)
    private String timeId;
    @Column(name = "name", length = 45)
    private String name;
    @Column(name = "description", length = 250)
    private String description;
    @OneToMany(mappedBy = "timeperiodicity")
    private List<Dataset> datasetList;

    public Timeperiodicity() {
    }

    public Timeperiodicity(String timeId) {
        this.timeId = timeId;
    }

    public String getTimeId() {
        return timeId;
    }

    public void setTimeId(String timeId) {
        this.timeId = timeId;
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
        hash += (timeId != null ? timeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Timeperiodicity)) {
            return false;
        }
        Timeperiodicity other = (Timeperiodicity) object;
        if ((this.timeId == null && other.timeId != null) || (this.timeId != null && !this.timeId.equals(other.timeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Timeperiodicity[ timeId=" + timeId + " ]";
    }
    
}
