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
public class ParameterPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Basic(optional = false)
    @Column(name = "experimentId", nullable = false)
    private int experimentId;
    @Basic(optional = false)
    @Column(name = "parameterId", nullable = false, length = 10)
    private String parameterId;

    public ParameterPK() {
    }

    public ParameterPK(int experimentId, String parameterId) {
        this.experimentId = experimentId;
        this.parameterId = parameterId;
    }

    public int getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) experimentId;
        hash += (parameterId != null ? parameterId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ParameterPK)) {
            return false;
        }
        ParameterPK other = (ParameterPK) object;
        if (this.experimentId != other.experimentId) {
            return false;
        }
        if ((this.parameterId == null && other.parameterId != null) || (this.parameterId != null && !this.parameterId.equals(other.parameterId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.ParameterPK[ experimentId=" + experimentId + ", parameterId=" + parameterId + " ]";
    }
    
}
