package com.mg.uav.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mg.uav.R;

import dji.ux.model.base.BaseWidgetAppearances;

/**
 * Override PreFlightStatusWidget with custom appearance
 */
public class CustomizedPreFlightWidget extends dji.ux.widget.PreFlightStatusWidget {

    private TextView tvStatus;

    public CustomizedPreFlightWidget(Context context) {
        this(context, null, 0);
    }

    public CustomizedPreFlightWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);

    }

    public CustomizedPreFlightWidget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    public void initView(Context context, AttributeSet attributeSet, int i) {
//        super.initView(context, attributeSet, i);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.customized_prd_flight, this);
        tvStatus = (TextView) view.findViewById(R.id.tv_status);
        tvStatus.setText("未连接");
        tvStatus.setTextColor(context.getResources().getColor(R.color.colorWhite));
    }

    @Override
    protected BaseWidgetAppearances getWidgetAppearances() {
        return null;
    }

    @Override
    public void onStatusChange(String s, StatusType statusType, boolean b) {
//        super.onStatusChange(s, statusType, b);
        tvStatus.setText(s);
//        switch (statusType) {
//            case GOOD:
//                tvStatus.setTextColor(context.getResources().getColor(R.color.colorTheme));
//                break;
//            case ERROR:
//                tvStatus.setTextColor(context.getResources().getColor(R.color.red));
//                break;
//            case OFFLINE:
//                tvStatus.setTextColor(context.getResources().getColor(R.color.red));
//                break;
//            case WARNING:
//                tvStatus.setTextColor(context.getResources().getColor(R.color.yellow));
//
//                break;
//        }
    }
}
