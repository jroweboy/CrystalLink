package entities.BaseClasses;

/**
 * Calculate and handle different varieties of damages
 * @author James Rowe
 */

enum DamageType {
	CHAOS;
	NEUTRAL;
	WIND;
	FIRE;
	WATER;
	THUNDER;
	LIGHT; // Wait do I really want a light sword? That'd be cool I suppose
}

class Damage {
	public var type:DamageType;
	public var amount:Int;
	public function new(Type_:DamageType, Amount: Int) {
		this.type = Type_;
		this.amount = Amount;
	}
}