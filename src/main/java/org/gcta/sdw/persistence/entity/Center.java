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
@Table(name = "center")
@NamedQueries({
    @NamedQuery(name = "Center.findAll", query = "SELECT c FROM Center c")})
public class Center implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "centerId", nullable = false, length = 10)
    private String centerId;
    @Column(name = "name", length = 100)
    private String name;
    @OneToMany(mappedBy = "center")
    private List<Dataset> datasetList;

    public Center() {
    }

    public Center(String centerId) {
        this.centerId = centerId;
    }

    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        hash += (centerId != null ? centerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Center)) {
            return false;
        }
        Center other = (Center) object;
        if ((this.centerId == null && other.centerId != null) || (this.centerId != null && !this.centerId.equals(other.centerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Center[ centerId=" + centerId + " ]";
    }
    
}
