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
public class DatavariablePK implements Serializable {
    @Basic(optional = false)
    @Column(name = "datasetId")
    private int datasetId;
    @Basic(optional = false)
    @Column(name = "dataId")
    private int dataId;
    @Basic(optional = false)
    @Column(name = "variableId")
    private int variableId;

    public DatavariablePK() {
    }

    public DatavariablePK(int datasetId, int dataId, int variableId) {
        this.datasetId = datasetId;
        this.dataId = dataId;
        this.variableId = variableId;
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

    public int getVariableId() {
        return variableId;
    }

    public void setVariableId(int variableId) {
        this.variableId = variableId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) datasetId;
        hash += (int) dataId;
        hash += (int) variableId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatavariablePK)) {
            return false;
        }
        DatavariablePK other = (DatavariablePK) object;
        if (this.datasetId != other.datasetId) {
            return false;
        }
        if (this.dataId != other.dataId) {
            return false;
        }
        if (this.variableId != other.variableId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.DatavariablePK[ datasetId=" + datasetId + ", dataId=" + dataId + ", variableId=" + variableId + " ]";
    }
    
}
