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
public class PredictorHasPredictorsPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Basic(optional = false)
    @Column(name = "idExperiment", nullable = false)
    private int idExperiment;
    @Basic(optional = false)
    @Column(name = "stdVarId", nullable = false, length = 10)
    private String stdVarId;
    @Basic(optional = false)
    @Column(name = "level", nullable = false)
    private long level;

    public PredictorHasPredictorsPK() {
    }

    public PredictorHasPredictorsPK(int idExperiment, String stdVarId, long level) {
        this.idExperiment = idExperiment;
        this.stdVarId = stdVarId;
        this.level = level;
    }

    public int getIdExperiment() {
        return idExperiment;
    }

    public void setIdExperiment(int idExperiment) {
        this.idExperiment = idExperiment;
    }

    public String getStdVarId() {
        return stdVarId;
    }

    public void setStdVarId(String stdVarId) {
        this.stdVarId = stdVarId;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idExperiment;
        hash += (stdVarId != null ? stdVarId.hashCode() : 0);
        hash += (int) level;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PredictorHasPredictorsPK)) {
            return false;
        }
        PredictorHasPredictorsPK other = (PredictorHasPredictorsPK) object;
        if (this.idExperiment != other.idExperiment) {
            return false;
        }
        if ((this.stdVarId == null && other.stdVarId != null) || (this.stdVarId != null && !this.stdVarId.equals(other.stdVarId))) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.PredictorHasPredictorsPK[ idExperiment=" + idExperiment + ", stdVarId=" + stdVarId + ", level=" + level + " ]";
    }
    
}
