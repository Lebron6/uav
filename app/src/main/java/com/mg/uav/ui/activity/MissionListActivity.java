package com.mg.uav.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.entity.MissionList;
import com.mg.uav.tools.AppManager;
import com.mg.uav.tools.PreferenceUtils;
import com.mg.uav.tools.RecyclerViewHelper;
import com.mg.uav.ui.adapter.MissionListAdapter;
import com.orhanobut.logger.Logger;

import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionListActivity extends BaseActivity {

    RecyclerView recycle;
    private MissionListAdapter adapter;
    MissionList missionList;
    private LinearLayout layout_mission_cancel;

    public static void actionStartForResult(Activity context) {
        Intent intent = new Intent(context, MissionListActivity.class);
        context.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_list);
        initView();
        initData();
    }

    private void initData() {
        createRequest().flightSelected(PreferenceUtils.getInstance().getUserToken(), PreferenceUtils.getInstance().getFlyNumber()).enqueue(new Callback<MissionList>() {
            @Override
            public void onResponse(Call<MissionList> call, Response<MissionList> response) {
                if (response.body().getCode()==401){
                    PreferenceUtils.getInstance().loginOut();
                    AppManager.getAppManager().finishAllActivity();
                    LoginActivity.actionStart(MissionListActivity.this);
                    return;
                }
                missionList = response.body();
                adapter.setData(missionList);
                RecyclerViewHelper.initRecyclerViewG(MissionListActivity.this, recycle, adapter, 3);
            }

            @Override
            public void onFailure(Call<MissionList> call, Throwable t) {
                Toast.makeText(MissionListActivity.this,"网络异常:获取航线列表失败:"+t.toString(),Toast.LENGTH_SHORT).show();
                Logger.e("网络异常:获取航线列表失败:"+t.toString());
            }
        });
    }

    private void initView() {
        recycle = findViewById(R.id.recycle);
        adapter = new MissionListAdapter(this);
        adapter.setOnItemClickLitener(onItemClickLitener);
        layout_mission_cancel=findViewById(R.id.layout_mission_cancel);
        layout_mission_cancel.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.layout_mission_cancel:
                    finish();
                    break;
            }
        }
    };

    MissionListAdapter.OnItemClickLitener onItemClickLitener = new MissionListAdapter.OnItemClickLitener() {
        @Override
        public void onItemClick(View view, int position) {
            if (missionList!=null){
                Intent intent = new Intent();
                intent.putExtra("flightPathId", missionList.getData().get(position).getFlightPathId());
                setResult(RESULT_CODE, intent);
                finish();
            }
        }
    };

    @Override
    public boolean useEventBus() {
        return false;
    }
}
