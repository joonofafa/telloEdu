package com.jdedu.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.jdedu.home.TelloLib.TelloCommand;

public class MainActivity extends AppCompatActivity {
    private TelloLib m_tlInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_tlInstance = new TelloLib();

        Thread thTemp = new Thread() {
            @Override
            public void run() {
                super.run();
                m_tlInstance.connectToDrone();
            }
        };

        thTemp.start();
        try {
            thTemp.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(this.getClass().getName(), "keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_W:
                m_tlInstance.sendCommand(TelloCommand.CMD_FORWARD, "20");
                break;

            case KeyEvent.KEYCODE_S:
                m_tlInstance.sendCommand(TelloCommand.CMD_BACK, "20");
                break;

            case KeyEvent.KEYCODE_A:
                m_tlInstance.sendCommand(TelloCommand.CMD_LEFT, "20");
                break;

            case KeyEvent.KEYCODE_D:
                m_tlInstance.sendCommand(TelloCommand.CMD_RIGHT, "20");
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                m_tlInstance.sendCommand(TelloCommand.CMD_UP, "20");
                break;

            case KeyEvent.KEYCODE_PAGE_DOWN:
                m_tlInstance.sendCommand(TelloCommand.CMD_DOWN, "20");
                break;

            case KeyEvent.KEYCODE_F9:
                m_tlInstance.sendCommand(TelloCommand.CMD_TAKE_OFF);
                break;

            case KeyEvent.KEYCODE_F10:
                m_tlInstance.sendCommand(TelloCommand.CMD_LAND);
                break;

            case KeyEvent.KEYCODE_F11:
                m_tlInstance.connectToDrone();
                break;

            case KeyEvent.KEYCODE_F12:
                m_tlInstance.sendCommand(TelloCommand.CMD_EMERGENCY);
                m_tlInstance.disconnectWithDrone();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
