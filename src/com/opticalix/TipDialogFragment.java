package com.opticalix;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.opticalix.widget_reminder.R;

/**
 * Created by Felix on 2015/6/12.
 */
public class TipDialogFragment extends DialogFragment {
    private TextView mTitleTv;
    private TextView mTipTv;
    private Runnable mRunnable;

    private int mLayout;
    private int mTitleId;
    private int mTipId;

    /**
     * @param res 1.layout 2.title id 3.tip id
     * @return
     */
    public static TipDialogFragment newInstance(int[] res) {
        TipDialogFragment TipDialogFragment = new TipDialogFragment();
        Bundle args = new Bundle();
        args.putIntArray("res", res);
        TipDialogFragment.setArguments(args);
        return TipDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        int[] reses = arguments.getIntArray("res");
        if (reses == null || reses.length == 0) {
            throw new RuntimeException("no correct res found");
        } else {
            mLayout = reses[0];
            mTitleId = reses[1];
            mTipId = reses[2];
            if (mLayout == 0 || mTipId == 0 || mTipId == 0) {
                throw new RuntimeException("no correct res found");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Step1 build Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(mLayout, null);
        mTitleTv = (TextView) view.findViewById(mTitleId);
        mTipTv = (TextView) view.findViewById(mTipId);

        builder.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mOnOkBtnClickListener != null)
                    mOnOkBtnClickListener.onOkClick();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false);
        AlertDialog alertDialog = builder.create();

        //Step2 custom Button
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //只有在Show之后才能getButton!
                Button okButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
                okButton.setTextColor(getResources().getColor(R.color.material_main_green));
            }
        });
        return alertDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    interface OnOkBtnClickListener {
        public void onOkClick();
    }

    private OnOkBtnClickListener mOnOkBtnClickListener;

    public OnOkBtnClickListener getOnOkBtnClickListener() {
        return mOnOkBtnClickListener;
    }

    public void setOnOkBtnClickListener(OnOkBtnClickListener onOkBtnClickListener) {
        mOnOkBtnClickListener = onOkBtnClickListener;
    }
}
