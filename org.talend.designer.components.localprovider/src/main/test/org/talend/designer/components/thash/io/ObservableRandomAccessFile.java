package org.talend.designer.components.thash.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Observable;

/**
 * DOC slanglois  class global comment. Detailled comment
 */
public class ObservableRandomAccessFile extends Observable implements Comparable<ObservableRandomAccessFile> {

    long position;

    RandomAccessFile raf = null;
    
    long fileSize;

    public byte[] read(long position) throws IOException {
        byte[] result = null;
        if (this.position != position) {
            raf.seek(position);
        }
        result = new byte[raf.readInt()];
        raf.read(result);
        setPosition((int)position + result.length + 4);
        return result;
    }

    public ObservableRandomAccessFile() {
    }

    public ObservableRandomAccessFile(RandomAccessFile raf, long fileSize, long position) {
        super();
        this.raf = raf;
        this.position = position;
        this.fileSize = fileSize;
    }

    public void positionChanged() {
        setChanged();
        notifyObservers();
    }

    public void setPosition(int position) {
        this.position = position;
        positionChanged();
    }

    @Override
    public int compareTo(ObservableRandomAccessFile o) {
        return (int) (this.position - o.position);
    }

    public String toString() {
        return String.valueOf(this.position);
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }

    }

}
