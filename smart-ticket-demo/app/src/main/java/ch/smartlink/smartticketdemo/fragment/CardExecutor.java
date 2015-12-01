package ch.smartlink.smartticketdemo.fragment;

/**
 * Created by caoky on 11/29/2015.
 */
public class CardExecutor implements Runnable{
    private CardFragment cardFragment;
    public CardExecutor(CardFragment cardFragment) {
        this.cardFragment = cardFragment;
    }
    @Override
    public void run() {

        if(!cardFragment.isScanning()) {

            cardFragment.getProgressDialog().dismiss();
        }
    }
}
