/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gcta.sdw.persistence.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Ronald
 */
@Entity
@Table(name = "data")
@NamedQueries({
    @NamedQuery(name = "Data.findAll", query = "SELECT d FROM Data d")})
public class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DataPK dataPK;
    @Column(name = "name", length = 45)
    private String name;
    @Column(name = "originalFileName", length = 45)
    private String originalFileName;
    @Lob
    @Column(name = "file")
    private byte[] file;
    @Column(name = "filePath", length = 250)
    private String filePath;
    @Column(name = "fileTypeDescription", length = 45)
    private String fileTypeDescription;
    @Column(name = "location", length = 100)
    private String location;
    @Column(name = "dumpInfo", length = 5000)
    private String dumpInfo;
    @Column(name = "code", length = 45)
    private String code;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "height", precision = 22)
    private Double height;
    @Column(name = "longitude", precision = 22)
    private Double longitude;
    @Column(name = "latitude", precision = 22)
    private Double latitude;
    @ManyToMany(mappedBy = "dataList")
    private List<Predictand> predictandList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "data")
    private List<Datavariable> datavariableList;
    @JoinColumn(name = "datasetId", referencedColumnName = "datasetId", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Dataset dataset;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "data")
    private List<Dimension> dimensionList;

    public Data() {
    }

    public Data(DataPK dataPK) {
        this.dataPK = dataPK;
    }

    public Data(int datasetId, int dataId) {
        this.dataPK = new DataPK(datasetId, dataId);
    }

    public DataPK getDataPK() {
        return dataPK;
    }

    public void setDataPK(DataPK dataPK) {
        this.dataPK = dataPK;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileTypeDescription() {
        return fileTypeDescription;
    }

    public void setFileTypeDescription(String fileTypeDescription) {
        this.fileTypeDescription = fileTypeDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDumpInfo() {
        return dumpInfo;
    }

    public void setDumpInfo(String dumpInfo) {
        this.dumpInfo = dumpInfo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public List<Predictand> getPredictandList() {
        return predictandList;
    }

    public void setPredictandList(List<Predictand> predictandList) {
        this.predictandList = predictandList;
    }

    public List<Datavariable> getDatavariableList() {
        return datavariableList;
    }

    public void setDatavariableList(List<Datavariable> datavariableList) {
        this.datavariableList = datavariableList;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public List<Dimension> getDimensionList() {
        return dimensionList;
    }

    public void setDimensionList(List<Dimension> dimensionList) {
        this.dimensionList = dimensionList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dataPK != null ? dataPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Data)) {
            return false;
        }
        Data other = (Data) object;
        if ((this.dataPK == null && other.dataPK != null) || (this.dataPK != null && !this.dataPK.equals(other.dataPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.gcta.sdw.persistence.entity.Data[ dataPK=" + dataPK + " ]";
    }
    
}
