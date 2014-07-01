package entities.BaseClasses;
import entities.BaseClasses.BaseSword.Sword;

/**
 * The sword model that the drawing class should contain
 * @author James Rowe
 */

enum Sword {
	WIND;
	FIRE;
	WATER;
	THUNDER;
	LIGHT;
}
class BaseSword
{
	public water:Damage;
	public wind:Damage;
	public fire:Damage;
	public thunder:Damage;
	public light:Damage;
	public sword:Sword;
	public function new(Sword_:Sword, ?Wind:DamageType, ?Fire:DamageType, ?Water:DamageType, ?Thunder:DamageType, ?Light:DamageType)
	{
		this.sword = Sword_;
		this.wind = (Wind == null) ? new Damage(DamageType.WIND, 0) : Wind;
		this.fire = (Fire == null) ? new Damage(DamageType.FIRE, 0) : Fire;
		this.water = (Water == null) ? new Damage(DamageType.WATER, 0) : Water;
		this.thunder = (Thunder == null) ? new Damage(DamageType.THUNDER, 0) : Thunder;
		this.light = (Light == null) ? new Damage(DamageType.LIGHT, 0) : Light;
	}
}