package ch.smartlink.smartticketdemo.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ch.smartlink.smartticketdemo.R;


/**
 * Created by caoky on 11/29/2015.
 */
public class CardFragment extends Fragment {
    private boolean isScanning;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity().getApplicationContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Scanning. Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showLog(String error){
        Toast.makeText(getActivity(), "Error:  " + error, Toast.LENGTH_LONG).show();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_card, container, false);
    }

    public boolean isScanning() {
        return isScanning;
    }

    protected void setIsScanning(boolean isScanning) {
        this.isScanning = isScanning;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }
}
