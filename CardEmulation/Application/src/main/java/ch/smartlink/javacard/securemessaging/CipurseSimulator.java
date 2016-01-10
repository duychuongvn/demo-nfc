package ch.smartlink.javacard.securemessaging;

import com.licel.jcardsim.base.SimulatorRuntime;

import ch.smartlink.javacard.HrsApplet;
import javacard.framework.Applet;
import javacard.framework.SystemException;

/**
 * Created by huynhduychuong on 1/10/2016.
 */
public class CipurseSimulator extends SimulatorRuntime {
    @Override
    public byte[] transmitCommand(byte[] command) throws SystemException {
        Applet applet = this.getApplet(this.getAID());
        byte[] commandTemporary = new byte[command.length];
        System.arraycopy(command, 0, commandTemporary, 0, command.length);
        byte[] response;
        boolean isSecureMessage = false;
        byte cla = commandTemporary[0];
        byte SMI = 0x00;
        if(cla == (byte)0x04) {
            SMI = commandTemporary[5];
            isSecureMessage = true;
        }
        response = super.transmitCommand(command);
        if(applet instanceof HrsApplet && isSecureMessage) {
            HrsApplet hrsApplet = (HrsApplet) applet;
            response = hrsApplet.wrapResponse(response, SMI);
        }

        return response;
    }
}
