package com.bulletphysics.linearmath;

public enum DebugModesType{
	NO_DEBUG(0),
	DRAW_WIREFRAME (1),
	DRAW_AABB(2),
	DRAW_FEATURES_TEXT(4),
	DRAW_CONTACT_POINTS(8),
	NO_DEACTIVATION(16),
	NO_HELP_TEXT(32),
	DRAW_TEXT(64),
	PROFILE_TIMINGS(128),
	ENABLE_SAT_COMPARISON(256),
	DISABLE_BULLET_LCP(512),
	ENABLE_CCD(1024),
	MAX_DEBUG_DRAW_MODE(1025),
	DrawConstraints((1 << 11)),
	DrawConstraintLimits((1 << 12));
	
	
	private int deger;
	
	DebugModesType(int deger) {
		this.deger = deger;
	}
	
	public int getDeger() {
		return this.deger;
	}
	
	public static String getName(int value) {
		for (DebugModesType type : DebugModesType.values()) {
			if(type.getDeger() == value) {
				return type.name();
			}
		}
		return null;
		
	}
	


	
}
