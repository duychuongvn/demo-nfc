package ch.smartlink.javacard;

import java.math.BigDecimal;
import java.util.UUID;

import javax.swing.*;

import ch.smartlink.javacard.hrs.*;
import ch.smartlink.javacard.hrs.CardInfo;
import ch.smartlink.javacard.zeitcontrol.ZCScriptV3;
import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.impl.AES;
import org.osptalliance.cipurse.impl.CommsChannel;
import org.osptalliance.cipurse.impl.Logger;

public class Main {
//
//    private static void createCcpCard(String readerName) throws CipurseException {
//
//        CardInfo cardInfo = new CardInfo(UUID.randomUUID().toString(), new BigDecimal(1000000), "EUR");
//        CCPCardReader ccpCardCreator = new CCPCardReader(new CommsChannel(readerName), new Logger());
//
//    //    System.out.println("Is Card initialized: " + ccpCardCreator.isCardInitialized("wewte4 t4et"));
//        CardTransaction initTransaction = ccpCardCreator.initCard(cardInfo);
//        CardTransaction debitTransaction = ccpCardCreator.debit(new CardOperation("Geneva", "mPOS", new BigDecimal(1)));
//        CardTransaction creditTransaction = ccpCardCreator.credit(new CardOperation("Geneva", "mPOS", new BigDecimal(1)));
//        System.out.println("Init Transaction: " + new String(initTransaction.toBytes()));
//        System.out.println("Debit Transaction: " + new String(debitTransaction.toBytes()));
//        System.out.println("Credit Transaction: " + new String(creditTransaction.toBytes()));

//    }


    private static void readZCScriptV3(String readerName) throws CipurseException {
        ZCScriptV3 zcScriptV3 = new ZCScriptV3(new CommsChannel(readerName), new AES(), new Logger());
        System.out.println("=============== Read ADF 2============");
        zcScriptV3.readAdf1();
        System.out.println("=============== Read ADF 2============");
        zcScriptV3.readAdf2();
    }

    private static void createHrsCard(String readerName) throws CipurseException {
        HrsCardCreator hrsCardCreator = new HrsCardCreator(new CommsChannel(readerName), new AES(), new Logger());
        ch.smartlink.javacard.hrs.CardInfo cardInfo = new CardInfo("1234558455", "21", "201501", false);
        hrsCardCreator.storeRoomInfo(cardInfo);

        CardInfo data = hrsCardCreator.getCardInfo();
        System.out.println(String.format("Wallet [%s] - Door [%s] - Exp [%s] - Locked [%s]", data.getWalletId(), data.getDoorId(), data.getExpiryDate(), data.isLocked()  ));
        hrsCardCreator.restoreCard();
    }
    private static void createHrsOberthurCard(String readerName) throws CipurseException {
        HrsOberthurCardCreator hrsCardCreator = new HrsOberthurCardCreator(new CommsChannel(readerName), new AES(), new Logger());
       hrsCardCreator.restoreCard();
        ch.smartlink.javacard.hrs.CardInfo cardInfo = new CardInfo("1234558455", "21", "201501", false);
        hrsCardCreator.storeRoomInfo(cardInfo);

        CardInfo data = hrsCardCreator.getCardInfo();
        System.out.println(String.format("Wallet [%s] - Door [%s] - Exp [%s] - Locked [%s]", data.getWalletId(), data.getDoorId(), data.getExpiryDate(), data.isLocked()  ));
        hrsCardCreator.restoreCard();
    }
    private static void createPaymentCard(String readerName) throws CipurseException {

        //	if(cardFrame.isUserAcceptFormatAndClearCardInfo()) {
        //	Account account = cardFrame.requestInput();
//
//				if(account == null) {
//					return;
//				}
        Account account = new Account();
        account.setCardNumber("1000200030004000");
        account.setExpiryDate("1220");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.TEN);


        PaymentCardCreator paymentCardCreator = new PaymentCardCreator(new CommsChannel(readerName), new Logger());
        paymentCardCreator.installApplication();
        paymentCardCreator.initCardInfo(account);


    }

    private static void createCardSM(String readerName) throws CipurseException {
        CardSMCreator cardSMCreator = new CardSMCreator(new CommsChannel(readerName), new Logger());
        cardSMCreator.setupCard();
    }

    public static void main(String[] args) {
        CardFrame cardFrame = new CardFrame();
        try {


            String[] choices = CommsChannel.getPCSCTerminals();
            if (choices != null) {
                String readerName = (String) JOptionPane.showInputDialog(null,
                        "Select Reader for the operation", "Reader Selection",
                        JOptionPane.QUESTION_MESSAGE, null, choices, choices.length > 1 ? choices[0] : "");
                if (readerName != null && !readerName.trim().equals("")) {

               //     createCcpCard(readerName);
                  //  createCardSM(readerName);
                    createHrsCard(readerName);
                 //   readZCScriptV3(readerName);
                 //   createHrsOberthurCard(readerName);
                    cardFrame.showSuccessMessage();
                }
            }
        } catch (CipurseException e) {
            System.err.println(e.getMessage());
            cardFrame.showErrorMessage(e.getMessage());
        }

        System.exit(1);
    }
}
