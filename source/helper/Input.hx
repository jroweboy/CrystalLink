package helper;

import flixel.FlxG;
import flixel.util.FlxSave;
import flixel.input.keyboard.FlxKey;
import flixel.input.keyboard.FlxKeyList;
/**
 * A generic handler for several kinds of input. 
 * 
 * @author James Rowe
 */
enum Actions {
	SPELL;
	ATTACK;
	SWITCH_LEFT;
	SWITCH_RIGHT;
	MOVE_UP;
	MOVE_DOWN;
	MOVE_LEFT;
	MOVE_RIGHT;
}
	
class Input
{
	private static var move_left : Int;
	private static var move_right : Int;
	private static var move_up : Int;
	private static var move_down : Int;
	private static var attack : Int;
	private static var spell : Int;
	private static var switch_left : Int;
	private static var switch_right : Int;
	private static var justPressed : FlxKeyList;
	private static var pressed : FlxKeyList;
	
	public static function init(config: FlxSave) {
		move_left = (config.data.left_key) ? config.data.left_key : FlxKey.A;
		move_right = (config.data.right_key) ? config.data.right_key : FlxKey.D;
		move_up = (config.data.up_key) ? config.data.up_key : FlxKey.W;
		move_down = (config.data.down_key) ? config.data.down_key : FlxKey.S;
		attack = (config.data.attack) ? config.data.attack : FlxKey.J;
		spell = (config.data.spell) ? config.data.spell : FlxKey.K;
		switch_left = (config.data.switch_left) ? config.data.switch_left : FlxKey.U;
		switch_right = (config.data.switch_right) ? config.data.switch_right : FlxKey.I;
		justPressed = new FlxKeyList(FlxKey.JUST_PRESSED);
		pressed = new FlxKeyList(FlxKey.PRESSED);
	}
	
	// TODO: Make the keys be bound in a menu state instead of hardcoded
	public static function checkAction(a : Actions) : Bool {
		#if (web || desktop)
		switch (a) {
			case SPELL:
				if (justPressed.check(spell)) return true;
			case ATTACK:
				if (justPressed.check(attack)) return true;
			case SWITCH_LEFT:
				if (justPressed.check(switch_left)) return true;
			case SWITCH_RIGHT:
				if (justPressed.check(switch_right)) return true;
			case MOVE_UP:
				if (pressed.check(move_up)) return true;
			case MOVE_DOWN:
				if (pressed.check(move_down)) return true;
			case MOVE_LEFT:
				if (pressed.check(move_left)) return true;
			case MOVE_RIGHT:
				if (pressed.check(move_right)) return true;
		}
		#end

		#if desktop
		//gamepadControls();
		#end

		#if mobile
		//touchControls();
		#end
		return false;
	}
}