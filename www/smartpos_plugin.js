var cordova = require('cordova');
var exec = require('cordova/exec');

var SmartPOSPlugin = function() {
	
	this.print = function(data, success_cb, error_cb) {
		exec(success_cb, error_cb, "SmartPOSPlugin", "print", [data]);
	}

};

var smartPOSPlugin = new SmartPOSPlugin();
module.exports = smartPOSPlugin;