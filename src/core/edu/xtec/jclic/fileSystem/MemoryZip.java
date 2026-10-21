/*
 * File    : MemoryZip.java
 * Created : 09-oct-2026
 * By      : NoGaOS (https://github.com/celtric/nogaos-jclic-player)
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2026 Ricard Osorio
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package edu.xtec.jclic.fileSystem;

import edu.xtec.util.ResourceBridge;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.*;

/**
 * A {@link ZipFileSystem} over a zip file already held in memory, for hosts that
 * read the project themselves (from a database, a virtual drive, a download...)
 * and just hand its bytes to the player.
 * <p>
 * The file system is closed over its entries: a name that is not in the zip is
 * reported as missing, and is never looked up on the local disk or the network,
 * since the zip has no real location to resolve it against.
 *
 * @author NoGaOS
 * @version 26.10.09
 */
public class MemoryZip extends ZipFileSystem {

  /**
   * Creates a new MemoryZip.
   *
   * @param name Name of the zip, used as its full root and in messages
   * @param zip  The bytes of the zip file
   * @param rb   The ResourceBridge used by this file system
   * @throws Exception If the bytes can't be read as a zip file
   */
  public MemoryZip(String name, byte[] zip, ResourceBridge rb) throws Exception {
    super(rb);
    zipName = name;
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
    ArrayList<MemoryZipEntry> v = new ArrayList<MemoryZipEntry>();
    try {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = zis.read(buffer, 0, buffer.length)) > 0)
          baos.write(buffer, 0, bytesRead);
        v.add(new MemoryZipEntry(entry, baos.toByteArray()));
        zis.closeEntry();
      }
    } finally {
      zis.close();
    }
    entries = v.toArray(new MemoryZipEntry[v.size()]);
    loaded = true;
  }

  @Override
  public String getFullRoot() {
    return zipName;
  }

  @Override
  protected void open() throws Exception {
    // Nothing to open: all the entries are already in memory
  }

  @Override
  protected void changeBase(String newRoot, String newFileName) throws Exception {
    throw new Exception("A MemoryZip can't be moved to " + newRoot + newFileName);
  }

  @Override
  public byte[] getBytes(String fileName) throws IOException {
    return requireEntry(fileName).getBytes();
  }

  @Override
  public long getFileLength(String fileName) throws IOException {
    return requireEntry(fileName).getSize();
  }

  @Override
  public InputStream getInputStream(String fName) throws IOException {
    return requireEntry(fName).getInputStream();
  }

  @Override
  public java.awt.Image getImageFile(String fName) throws Exception {
    return java.awt.Toolkit.getDefaultToolkit().createImage(requireEntry(fName).getBytes());
  }

  @Override
  public Object getMediaDataSource(String fName) throws Exception {
    return new edu.xtec.util.ExtendedByteArrayInputStream(requireEntry(fName).getBytes(), fName);
  }

  @Override
  public boolean realFileExists(String fName) {
    return false;
  }

  private ExtendedZipEntry requireEntry(String fName) throws FileNotFoundException {
    ExtendedZipEntry entry = getEntry(fName);
    if (entry == null)
      throw new FileNotFoundException(fName + " is not in " + zipName);
    return entry;
  }

  protected class MemoryZipEntry extends ExtendedZipEntry {
    byte[] data;

    MemoryZipEntry(ZipEntry entry, byte[] setData) {
      super(entry);
      data = setData;
    }

    public byte[] getBytes() throws IOException {
      return data;
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(data);
    }
  }
}
