package com.uliamar.minilock.ui;

import com.uliamar.minilock.minilocklib.MinilockFile;

public class MagicCodeReadedEvent {
  private final MinilockFile file;
  private final Boolean isMinilockFile;

  public MagicCodeReadedEvent(MinilockFile file, Boolean isMinilockFile) {
    this.file = file;
    this.isMinilockFile = isMinilockFile;
  }

  public MinilockFile getFile() {
    return file;
  }

  public Boolean getIsMinilockFile() {
    return isMinilockFile;
  }
}
