/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Ronald
 */
@Embeddable
public class TimeseriePK implements Serializable {
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
    @Column(name = "variableId", nullable = false)
    private int variableId;
    @Basic(optional = false)
    @Column(name = "dateTime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    public TimeseriePK() {
    }

    public TimeseriePK(int datasetId, int dataId, int variableId, Date dateTime) {
        this.datasetId = datasetId;
        this.dataId = dataId;
        this.variableId = variableId;
        this.dateTime = dateTime;
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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) datasetId;
        hash += (int) dataId;
        hash += (int) variableId;
        hash += (dateTime != null ? dateTime.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TimeseriePK)) {
            return false;
        }
        TimeseriePK other = (TimeseriePK) object;
        if (this.datasetId != other.datasetId) {
            return false;
        }
        if (this.dataId != other.dataId) {
            return false;
        }
        if (this.variableId != other.variableId) {
            return false;
        }
        if ((this.dateTime == null && other.dateTime != null) || (this.dateTime != null && !this.dateTime.equals(other.dateTime))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.TimeseriePK[ datasetId=" + datasetId + ", dataId=" + dataId + ", variableId=" + variableId + ", dateTime=" + dateTime + " ]";
    }
    
}
