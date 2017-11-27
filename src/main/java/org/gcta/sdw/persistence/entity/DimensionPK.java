/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Ronald
 */
@Embeddable
public class DimensionPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Basic(optional = false)
    @Column(name = "datasetId", nullable = false)
    private int datasetId;
    @Basic(optional = false)
    @Column(name = "dataId", nullable = false)
    private int dataId;
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    public DimensionPK() {
    }

    public DimensionPK(int datasetId, int dataId, String name) {
        this.datasetId = datasetId;
        this.dataId = dataId;
        this.name = name;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) datasetId;
        hash += (int) dataId;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DimensionPK)) {
            return false;
        }
        DimensionPK other = (DimensionPK) object;
        if (this.datasetId != other.datasetId) {
            return false;
        }
        if (this.dataId != other.dataId) {
            return false;
        }
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.DimensionPK[ datasetId=" + datasetId + ", dataId=" + dataId + ", name=" + name + " ]";
    }
    
}
