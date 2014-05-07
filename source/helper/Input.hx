package helper;

import flixel.FlxG;
/**
 * A generic handler for several kinds of input. 
 * 
 * @author James Rowe
 */
enum Actions {
	JUMP;
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
	private static var move_left : Array<String>;
	private static var move_right : Array<String>;
	private static var move_up : Array<String>;
	private static var move_down : Array<String>;
	
	// TODO: Make the keys be bound in a menu state instead of hardcoded
	public static function checkAction(a : Actions) : Bool {
		#if (web || desktop)
		switch (a) {
			case JUMP:
				if (FlxG.keys.justPressed.Z) return true;
			case ATTACK:
				if (FlxG.keys.justPressed.X) return true;
			case SWITCH_LEFT:
				if (FlxG.keys.justPressed.A) return true;
			case SWITCH_RIGHT:
				if (FlxG.keys.justPressed.S) return true;
			case MOVE_UP:
				if (FlxG.keys.pressed.UP) return true;
			case MOVE_DOWN:
				if (FlxG.keys.pressed.DOWN) return true;
			case MOVE_LEFT:
				if (FlxG.keys.pressed.LEFT) return true;
			case MOVE_RIGHT:
				if (FlxG.keys.pressed.RIGHT) return true;
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