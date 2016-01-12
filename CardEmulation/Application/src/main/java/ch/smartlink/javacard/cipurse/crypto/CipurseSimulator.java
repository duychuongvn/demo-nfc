package ch.smartlink.javacard.cipurse.crypto;

import com.licel.jcardsim.base.SimulatorRuntime;

import ch.smartlink.javacard.applet.HrsApplet;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.SystemException;
import javacard.framework.Util;

/**
 * Created by huynhduychuong on 1/10/2016.
 */
public class CipurseSimulator extends SimulatorRuntime implements ISO7816{
    @Override
    public byte[] transmitCommand(byte[] command) throws SystemException {
        Applet applet = this.getApplet(this.getAID());
        byte[] response;
        if(applet instanceof HrsApplet) {
            HrsApplet hrsApplet = (HrsApplet) applet;
            byte[] commandTemporary = new byte[command.length];
            byte cla= command[OFFSET_CLA];
            byte secureCLA = 0x04;
            boolean isSecureMessage = false;
            System.arraycopy(command, 0, commandTemporary, 0, command.length);

            byte SMI = 0x00;
            if(cla == secureCLA) {
                SMI = commandTemporary[5];
                isSecureMessage = true;
            }
            if(isSecureMessage) {
                try {
                    byte[] unwrappedCommand = hrsApplet.unwrapAPDU(command, SMI);
                   // command =unwrappedCommand;
                    System.arraycopy(unwrappedCommand, 0, command, 0, unwrappedCommand.length);
//                    for(int i = unwrappedCommand.length; i<command.length -1;i++) {
//                        Util.setShort(command, (short)i, (short)0);
//                    }
                }catch (Exception isoException) {
                    response = new byte[2];
                    Util.setShort(response, (short) 0, SW_UNKNOWN);
                    return response;
                }
            }
            response = super.transmitCommand(command);
            if(isSecureMessage) {
                try {
                    response = hrsApplet.wrapResponse(response, SMI);
                }catch (Exception isoException) {
                    response = new byte[2];
                    Util.setShort(response, (short) 0, SW_UNKNOWN);
                }
            }
        } else {
            try {
                response = super.transmitCommand(command);
            }catch (Exception isoException) {
                response = new byte[2];
                Util.setShort(response, (short) 0, SW_UNKNOWN);
            }
        }
        return response;
    }
}
