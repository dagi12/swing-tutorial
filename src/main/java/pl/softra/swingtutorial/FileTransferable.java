package pl.softra.swingtutorial;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Objects;

/**
 * Created by erykmariankowski on 26.07.2018.
 */
public class FileTransferable implements Transferable {

    private final Object[] transferedObjects;
    private final DataFlavor localObjectFlavor;

    public FileTransferable(JComponent component, DataFlavor localObjectFlavor) {
        this.localObjectFlavor = localObjectFlavor;
        JList<?> source = (JList<?>) component;
        this.transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{localObjectFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(localObjectFlavor, flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
            return transferedObjects;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
