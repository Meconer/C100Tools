/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package C100Tools;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 *
 * @author mats
 */
public class C100TransferHandler extends TransferHandler {

    public void setDmuProgram(C100Program c100Program) {
        this.c100Program = c100Program;
    }
    
    C100Program c100Program;

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }
    
    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if ( !info.isDrop() ) {
            return false;
        }
        
        Transferable dataToTransfer = info.getTransferable();
        List<File> fileList = null;
        try {
            fileList = (List) dataToTransfer.getTransferData(DataFlavor.javaFileListFlavor);
            System.out.println(fileList.size());
            System.out.println(fileList.get(0));
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(C100TransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if ( fileList != null ) {
            if (fileList.size() > 0 ) c100Program.readFile(  fileList.get(0).getAbsolutePath()  );
        }
        return true;
    }
    
     /**
     * Bundle up the selected items in a single list for export.
     * Each line is separated by a newline.
     * @return 
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<String> list = (JList)c;
        List<String> values = list.getSelectedValuesList();
        
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            buff.append(val == null ? "" : val.toString());
            if (i != values.size() - 1) {
                buff.append("\n");
            }
        }
        
        return new StringSelection(buff.toString());
    }
   
    
}
