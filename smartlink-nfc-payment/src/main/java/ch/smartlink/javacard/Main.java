package ch.smartlink.javacard;

import java.math.BigDecimal;

import javax.swing.*;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.impl.CommsChannel;
import org.osptalliance.cipurse.impl.Logger;

public class Main {

	public static void main(String[] args) {
		CardFrame cardFrame = new CardFrame();
		try {

			if(cardFrame.isUserAcceptFormatAndClearCardInfo()) {
				Account account = cardFrame.requestInput();
				if(account == null) {
					return;
				}
				String[] choices = CommsChannel.getPCSCTerminals();
				if (choices != null) {
					String readerName = (String) JOptionPane.showInputDialog(null,
							"Select Reader for the operation", "Reader Selection",
							JOptionPane.QUESTION_MESSAGE, null, choices, choices.length > 1 ? choices[0] : "");
					if (readerName != null && !readerName.trim().equals("")) {


						PaymentCardCreator paymentCardCreator = new PaymentCardCreator(new CommsChannel(readerName), new Logger());
						paymentCardCreator.installApplication();
						paymentCardCreator.initCardInfo(account);

						cardFrame.showSuccessMessage();

					}
				}
			}
		} catch (CipurseException e) {
			System.err.println(e.getMessage());
			cardFrame.showErrorMessage();
		}

		System.exit(0);
	}
}
