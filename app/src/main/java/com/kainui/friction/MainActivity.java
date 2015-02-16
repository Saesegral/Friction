package com.kainui.friction;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener,
		OnKeyListener {
	TextView tv;
	StringBuilder sb = new StringBuilder();
	public float angle = 0;
	ArrayList<Float> trials = new ArrayList<Float>();
	public float average = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		tv.setOnKeyListener(this);
		tv.setFocusableInTouchMode(true);
		tv.requestFocus();
		tv.setBackgroundColor(Color.DKGRAY);
		tv.setTextColor(Color.CYAN);
		setContentView(tv);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			Sensor accelerometer = manager.getSensorList(
					Sensor.TYPE_ACCELEROMETER).get(0);
			manager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	public float getAngle(SensorEvent event) {
		float dot = 0;
		for (int i = 0; i < 3; i++) {
			dot += event.values[i] * event.values[i];
		}

		return (float) Math.acos(event.values[2] / Math.sqrt(dot));
	}

	public float getCoSF(SensorEvent event) {
		return (float) Math.hypot(event.values[0], event.values[1])
				/ event.values[2];
	}

	private float getAverage() {
		average = 0;
		for (int i = 0; i < trials.size(); i++) {
			average += trials.get(i);
		}
		average /= trials.size();
		return average;
	}

	public float round(float n) {
		n = (int) (n * 100);
		n = ((float) n) / 100;
		return n;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				trials.add(angle);
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (trials.size() > 0)
					trials.remove(trials.size() - 1);
				break;
			}
		}
		return event.getKeyCode() != KeyEvent.KEYCODE_BACK;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		angle = getAngle(event);

		sb.setLength(0);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getHeight() / (8 + 2));
		sb.append("Help: Stand device upright\n");
		sb.append("Coef of Static Friction = " + round(getCoSF(event)) + "\n");
		sb.append("Current Angle (rad) = " + round(getAngle(event)) + "\n");
		sb.append("Current Angle (deg) = "
				+ round((float) (180 / Math.PI) * getAngle(event)) + "\n");
		sb.append(" -- Data Collected --       Trials = " + trials.size()
				+ "\n");
		sb.append("Avg Coef of Friction = "
				+ round((float) Math.tan(getAverage())) + "\n");
		sb.append("Avg Angle (rad) = " + round(getAverage()) + "\n");
		sb.append("Avg Angle (deg) = "
				+ round((float) (180 / Math.PI) * getAverage()) + "\n");

		if (trials.size() == 0 && angle > (Math.PI / 4)) {
			sb.setLength(0);
			tv.setTextSize(20);
			sb.append("To measure the coefficient of static friction between an object and your device's screen follow these easy steps: \n"
					+ "     1) Lay device down flat on table or surface.\n"
					+ "     2) Place small object like an eraser on your screen at your own risk.\n"
					+ "     3) Slowly tilt your phone at an angle until the object slides.\n"
					+ "     4) Right when the object starts to slide, press the volume up button to record data.\n"
					+ "If you mess up, press the volume down button to erase the last data recording.\n"
					+ "To record the coefficient of static friction between any two objects, hold the phone anywhere on a flat object so they have the same angle.\n"
					+ "To erase all stored trials, turn the phone upside down.");
		}

		if (getCoSF(event) < 0)
			trials.clear();

		tv.setText(sb.toString());
	}

	// Not important for this
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
