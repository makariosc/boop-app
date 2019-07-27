package com.makichung.boop;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nimrod on 12/9/2017.
 */

public class UserHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.friendNameField) TextView friendNameField;
    @BindView(R.id.toField) TextView toField;
    @BindView(R.id.fromField) TextView fromField;
    @BindView(R.id.friendItem) RelativeLayout friendItem;

    public UserHolder(View userView) {
        super(userView);
        ButterKnife.bind(this, userView);
    }

}
