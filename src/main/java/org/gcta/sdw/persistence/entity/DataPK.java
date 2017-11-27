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
public class DataPK implements Serializable {
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

    public DataPK() {
    }

    public DataPK(int datasetId, int dataId) {
        this.datasetId = datasetId;
        this.dataId = dataId;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) datasetId;
        hash += (int) dataId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DataPK)) {
            return false;
        }
        DataPK other = (DataPK) object;
        if (this.datasetId != other.datasetId) {
            return false;
        }
        if (this.dataId != other.dataId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.DataPK[ datasetId=" + datasetId + ", dataId=" + dataId + " ]";
    }
    
}
