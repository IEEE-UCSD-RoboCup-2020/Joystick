var direction = nipplejs.create({
  zone: document.getElementById('direction'),
  size: 175,
  mode: 'static',
  color: 'transparent',
  restOpacity: 1,
  position: {
    left: '80%',
    top: '50%'
  }
});

var spin = nipplejs.create({
  zone: document.getElementById('spin'),
  size: 175,
  mode: 'static',
  color: 'transparent',
  restOpacity: 1,
  position: {
    left: '20%',
    top: '50%'
  }
});

var shoot_mode = false;
var block = false;
var back = document.getElementById("spin").getElementsByClassName("back")[0];
var dist, angl;

function enter_shoot_mode() {
	if (!block && !shoot_mode && angl > 4.18879 && angl < 5.23599 && dist == 87.5) {
		back.className = "back shoot";
		shoot_mode = true;
	} // if still at bottom, enter shoot mode, otherwise do nothing
}

function unblock() {
	block = false;
}

spin.on('move', function (evt, data) {
	if (!shoot_mode && data.angle.degree > 240 && data.angle.degree < 300 && data.distance == 87.5) {
		setTimeout(enter_shoot_mode, 100) // wait 0.1 second
	}
	dist = data.distance
	angl = data.angle.radian

	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
	    left_dist: data.distance, 
	    left_angl: data.angle.radian,
	    righ_dist: -1,
	    righ_angl: -1,
	    shoot:     false,
	}));
});

spin.on('end', function (evt, data) {
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	
	if (shoot_mode) {
		xhr.send(JSON.stringify({
		    left_dist: dist, 
		    left_angl: angl,
		    righ_dist: -1,
		    shoot:     true,
		}));
		back.className = "back";
		shoot_mode = false;
		block = true;
		setTimeout(unblock, 100)
	} else {
		dist = 0
		angl = 0
		xhr.send(JSON.stringify({
		    left_dist: 0,
		    left_angl: 0,
		    righ_dist: -1,
		}));
	}
});

direction.on('move',function (evt, data){
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
	    left_dist: -1,
	    righ_dist: data.distance,
	    righ_angl: data.angle.radian,
	}));
});

direction.on('end',function (evt, data){
	var xhr = new XMLHttpRequest();
	xhr.open("POST", '/Joystick/Main', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.send(JSON.stringify({
	    left_dist: -1,
	    righ_dist: 0,
	    righ_angl: 0,
	}));
});
