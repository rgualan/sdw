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
@Table(name = "downscalingmethod")
@NamedQueries({
    @NamedQuery(name = "Downscalingmethod.findAll", query = "SELECT d FROM Downscalingmethod d")})
public class Downscalingmethod implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "dmethodId", nullable = false, length = 10)
    private String dmethodId;
    @Column(name = "name", length = 45)
    private String name;
    @Column(name = "description", length = 500)
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "downscalingmethod")
    private List<Configuration> configurationList;

    public Downscalingmethod() {
    }

    public Downscalingmethod(String dmethodId) {
        this.dmethodId = dmethodId;
    }

    public String getDmethodId() {
        return dmethodId;
    }

    public void setDmethodId(String dmethodId) {
        this.dmethodId = dmethodId;
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

    public List<Configuration> getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List<Configuration> configurationList) {
        this.configurationList = configurationList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dmethodId != null ? dmethodId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Downscalingmethod)) {
            return false;
        }
        Downscalingmethod other = (Downscalingmethod) object;
        if ((this.dmethodId == null && other.dmethodId != null) || (this.dmethodId != null && !this.dmethodId.equals(other.dmethodId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Downscalingmethod[ dmethodId=" + dmethodId + " ]";
    }
    
}
