package com.example.stubservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.aaaa.ICalc;
import com.example.aaaa.MusicData;
import com.example.stubservice.MusicListAdpater.ViewHolder;


public class MainActivity extends Activity implements OnClickListener, OnItemClickListener{
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.Call_Service).setOnClickListener(this);
        ((ListView)findViewById(R.id.List)).setAdapter(new MusicListAdpater(this));
        ((ListView)findViewById(R.id.List)).setOnItemClickListener(this);
        findViewById(R.id.Next).setOnClickListener(this);
        findViewById(R.id.Preview).setOnClickListener(this);
        findViewById(R.id.StartAndPause).setOnClickListener(this);
        findViewById(R.id.Stop).setOnClickListener(this);
        
        bindService(new Intent("com.example.aaaa.CalcService"), 
        		mSerConnection, Context.BIND_AUTO_CREATE);
    }

    private ICalc mICalc = null;
	private ServiceConnection mSerConnection =  new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			try {
				mICalc.stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.i("service" , "onServiceDisconnected() : " + e.getMessage());
			}
			mICalc = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i("service" , "onServiceConnected() : " + Process.myPid());
			mICalc = ICalc.Stub.asInterface(service);
			Toast.makeText(getApplicationContext(), "Bind", Toast.LENGTH_LONG).show();
			setList();
		}
	};
	
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	unbindService(mSerConnection);
    	super.onDestroy();
    }
    
    protected void setList() {
		// TODO Auto-generated method stub
    	MusicListAdpater adapter = (MusicListAdpater) ((ListView)findViewById(R.id.List)).getAdapter();
    	
    	try {
			adapter.setList(mICalc.getList());
			adapter.notifyDataSetChanged();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Log.e("ERROR", e.getMessage());
		}
	}

	@Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	finish();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try {
			switch(v.getId()){
				case R.id.Call_Service:
					if(mICalc != null)
						setList();
					break;
				case R.id.Next:
					mICalc.next();
					break;
				case R.id.Preview:
					mICalc.prev();
					break;
				case R.id.Stop:
					((ToggleButton)findViewById(R.id.StartAndPause)).setChecked(true);
					mICalc.stop();
					break;
				case R.id.StartAndPause:
					if(((ToggleButton)v).isChecked()){//start
						mICalc.pause();
					}else{
						mICalc.start("");
					}
					break;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Log.i("service" , e.getMessage());
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		MusicListAdpater.ViewHolder obj = (MusicListAdpater.ViewHolder) view.getTag();
		if(obj != null){
			MusicData data = (MusicData) obj.data;
			
			try {
				mICalc.start(data.getData());
				((ToggleButton)findViewById(R.id.StartAndPause)).setChecked(false);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.i("service" , e.getMessage());
			}
		}
	}
}
