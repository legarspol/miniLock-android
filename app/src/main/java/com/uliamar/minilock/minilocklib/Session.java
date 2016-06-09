package com.uliamar.minilock.minilocklib;

public class Session {
  private String sourceFileName;
  private Blake2s hashObject = new Blake2s(32, null);
  private Integer dataPostion;

  public String getSourceFileName() {
    return sourceFileName;
  }

  public void setSourceFileName(String sourceFileName) {
    this.sourceFileName = sourceFileName;
  }

  public Blake2s getHashObject() {
    return hashObject;
  }


  public void setDataPostion(Integer dataPostion) {
    this.dataPostion = dataPostion;
  }

  public Integer getDataPostion() {
    return dataPostion;
  }
}
