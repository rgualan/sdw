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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "datasettype")
@NamedQueries({
    @NamedQuery(name = "Datasettype.findAll", query = "SELECT d FROM Datasettype d")})
public class Datasettype implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "dsTypeId", nullable = false)
    private Integer dsTypeId;
    @Column(name = "name", length = 25)
    private String name;
    @Column(name = "description", length = 100)
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasettype")
    private List<Dataset> datasetList;

    public Datasettype() {
    }

    public Datasettype(Integer dsTypeId) {
        this.dsTypeId = dsTypeId;
    }

    public Integer getDsTypeId() {
        return dsTypeId;
    }

    public void setDsTypeId(Integer dsTypeId) {
        this.dsTypeId = dsTypeId;
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
        hash += (dsTypeId != null ? dsTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Datasettype)) {
            return false;
        }
        Datasettype other = (Datasettype) object;
        if ((this.dsTypeId == null && other.dsTypeId != null) || (this.dsTypeId != null && !this.dsTypeId.equals(other.dsTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Datasettype[ dsTypeId=" + dsTypeId + " ]";
    }
    
}
