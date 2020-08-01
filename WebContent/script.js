var HALF_JOY_SIZE = 150;

var direction = nipplejs.create({
  zone: document.getElementById('direction'),
  size: HALF_JOY_SIZE * 2,
  mode: 'static',
  color: 'transparent',
  restOpacity: 1,
  position: {
    left: '75%',
    top: '50%'
  }
});

var spin = nipplejs.create({
  zone: document.getElementById('spin'),
  size: HALF_JOY_SIZE * 2,
  mode: 'static',
  color: 'transparent',
  restOpacity: 1,
  position: {
    left: '25%',
    top: '50%'
  }
});

var shoot_mode = false;
var block = false;
var dribbler = false;
var back = document.getElementById("spin").getElementsByClassName("back")[0];
var dist, angl;

function flip_dribbler() {
	drib = document.getElementById("dribbler");
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	if (dribbler) {
		dribbler = false;
		drib.innerHTML = "Drib Off"
		drib.className = "button updating";
		xhr.send('DribOff');
	} else {
		dribbler = true;
		drib.innerHTML = "Drib On"
		drib.className = "button";
		xhr.send('DribOn');
	}
}

function change_ip_port() {
	update = document.getElementById("update");
	update.innerHTML = "Updating...";
	update.className = "button updating";

	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', false);
	xhr.send(document.getElementById("ip").value + ',' + 
		     document.getElementById("port").value);
	if (xhr.status === 200) { // OK
		update.innerHTML = xhr.responseText;
		setTimeout(restore_button, 1000);
	}
}

function restore_button() {
	update = document.getElementById("update");
	update.innerHTML = "Update";
	update.className = "button";
}

function enter_shoot_mode() {
	if (!block && !shoot_mode && angl > 4.18879 && angl < 5.23599 && dist == HALF_JOY_SIZE) {
		back.className = "back shoot";
		shoot_mode = true;
		setTimeout(reset, 100);
	} // if still at bottom, enter shoot mode, otherwise do nothing
}

function reset() {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
		left_dist:  0, 
		left_angl:  0,
		right_dist: -1,
		right_angl: -1,
		shoot:      false,
	}));
}

spin.on('move', function (evt, data) {
	if (!shoot_mode && data.angle.degree > 240 && data.angle.degree < 300 && data.distance == HALF_JOY_SIZE) {
		setTimeout(enter_shoot_mode, 100) // wait 0.1 second
	}
	dist = data.distance
	angl = data.angle.radian

	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	if (!shoot_mode) {
		xhr.send(JSON.stringify({
			left_dist:  data.distance, 
			left_angl:  data.angle.radian,
			right_dist: -1,
			right_angl: -1,
			shoot:      false,
		}));
	}
});

spin.on('end', function (evt, data) {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	
	if (shoot_mode) {
		xhr.send(JSON.stringify({
		    left_dist: dist, 
		    left_angl: angl,
		    right_dist: -1,
			shoot:     true,
			shoot_mode: false,
		}));
		back.className = "back";
		shoot_mode = false;
		block = true;
		setTimeout(function(){ block = false; }, 100)
	} else {
		dist = 0
		angl = 0
		xhr.send(JSON.stringify({
		    left_dist: 0,
		    left_angl: 0,
			right_dist: -1,
		}));
	}
});

direction.on('move',function (evt, data){
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
	    left_dist: -1,
	    right_dist: data.distance,
	    right_angl: data.angle.radian,
	}));
});

direction.on('end',function (evt, data){
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
	    left_dist: -1,
	    right_dist: 0,
	    right_angl: 0,
	}));
});
