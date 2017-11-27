/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "predictor_has_predictors")
@NamedQueries({
    @NamedQuery(name = "PredictorHasPredictors.findAll", query = "SELECT p FROM PredictorHasPredictors p")})
public class PredictorHasPredictors implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PredictorHasPredictorsPK predictorHasPredictorsPK;
    @Column(name = "orderNum")
    private Integer orderNum;
    @JoinColumn(name = "idExperiment", referencedColumnName = "experimentId", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Predictor predictor;
    @JoinColumn(name = "stdVarId", referencedColumnName = "stdVarId", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Metavariableenum metavariableenum;

    public PredictorHasPredictors() {
    }

    public PredictorHasPredictors(PredictorHasPredictorsPK predictorHasPredictorsPK) {
        this.predictorHasPredictorsPK = predictorHasPredictorsPK;
    }

    public PredictorHasPredictors(int idExperiment, String stdVarId, long level) {
        this.predictorHasPredictorsPK = new PredictorHasPredictorsPK(idExperiment, stdVarId, level);
    }

    public PredictorHasPredictorsPK getPredictorHasPredictorsPK() {
        return predictorHasPredictorsPK;
    }

    public void setPredictorHasPredictorsPK(PredictorHasPredictorsPK predictorHasPredictorsPK) {
        this.predictorHasPredictorsPK = predictorHasPredictorsPK;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public void setPredictor(Predictor predictor) {
        this.predictor = predictor;
    }

    public Metavariableenum getMetavariableenum() {
        return metavariableenum;
    }

    public void setMetavariableenum(Metavariableenum metavariableenum) {
        this.metavariableenum = metavariableenum;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (predictorHasPredictorsPK != null ? predictorHasPredictorsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PredictorHasPredictors)) {
            return false;
        }
        PredictorHasPredictors other = (PredictorHasPredictors) object;
        if ((this.predictorHasPredictorsPK == null && other.predictorHasPredictorsPK != null) || (this.predictorHasPredictorsPK != null && !this.predictorHasPredictorsPK.equals(other.predictorHasPredictorsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.PredictorHasPredictors[ predictorHasPredictorsPK=" + predictorHasPredictorsPK + " ]";
    }
    
}
