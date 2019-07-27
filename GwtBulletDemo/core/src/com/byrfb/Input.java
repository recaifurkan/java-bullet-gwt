package com.byrfb;

import com.badlogic.gdx.InputProcessor;
import com.bulletphysics.linearmath.DebugModesType;
import com.bulletphysics.test.bullet3dcontacttests.BulletTest;

public class Input implements InputProcessor {
	BulletTest bulletTest;

	DebugModesType[] types = DebugModesType.values();
	int index = 0;

	public Input(BulletTest bulletTest) {
		this.bulletTest = bulletTest;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean keyDown(int keycode) {
		
		if(keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {

//			ExitFrame frame = new ExitFrame();
//			frame.setVisible(true);
			
		}

		if (keycode == com.badlogic.gdx.Input.Keys.UP) {
			index++;
			if (index == types.length)
				index = 0;
			bulletTest.getDebugDrawer().setDebugMode(types[index].getDeger());
			
			
		}

		if (keycode == com.badlogic.gdx.Input.Keys.DOWN) {
			index--;
			if (index == -1)
				index = types.length - 1;
			bulletTest.getDebugDrawer().setDebugMode(types[index].getDeger());
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
