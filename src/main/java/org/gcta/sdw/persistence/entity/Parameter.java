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
@Table(name = "parameter")
@NamedQueries({
    @NamedQuery(name = "Parameter.findAll", query = "SELECT p FROM Parameter p")})
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ParameterPK parameterPK;
    @Column(name = "intValue")
    private Integer intValue;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "doubleValue", precision = 22)
    private Double doubleValue;
    @Column(name = "stringValue", length = 100)
    private String stringValue;
    @JoinColumn(name = "experimentId", referencedColumnName = "experimentId", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Configuration configuration;

    public Parameter() {
    }

    public Parameter(ParameterPK parameterPK) {
        this.parameterPK = parameterPK;
    }

    public Parameter(int experimentId, String parameterId) {
        this.parameterPK = new ParameterPK(experimentId, parameterId);
    }

    public ParameterPK getParameterPK() {
        return parameterPK;
    }

    public void setParameterPK(ParameterPK parameterPK) {
        this.parameterPK = parameterPK;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (parameterPK != null ? parameterPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Parameter)) {
            return false;
        }
        Parameter other = (Parameter) object;
        if ((this.parameterPK == null && other.parameterPK != null) || (this.parameterPK != null && !this.parameterPK.equals(other.parameterPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Parameter[ parameterPK=" + parameterPK + " ]";
    }
    
}
